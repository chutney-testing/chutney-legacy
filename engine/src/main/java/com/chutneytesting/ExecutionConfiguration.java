/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting;

import static com.chutneytesting.tools.Streams.identity;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

import com.chutneytesting.action.domain.ActionTemplateLoader;
import com.chutneytesting.action.domain.ActionTemplateLoaders;
import com.chutneytesting.action.domain.ActionTemplateParserV2;
import com.chutneytesting.action.domain.ActionTemplateRegistry;
import com.chutneytesting.action.domain.DefaultActionTemplateRegistry;
import com.chutneytesting.action.infra.DefaultActionTemplateLoader;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.injectable.ActionsConfiguration;
import com.chutneytesting.engine.api.execution.EmbeddedTestEngine;
import com.chutneytesting.engine.api.execution.TestEngine;
import com.chutneytesting.engine.domain.delegation.DelegationService;
import com.chutneytesting.engine.domain.execution.ExecutionEngine;
import com.chutneytesting.engine.domain.execution.ExecutionManager;
import com.chutneytesting.engine.domain.execution.engine.DefaultExecutionEngine;
import com.chutneytesting.engine.domain.execution.engine.DefaultStepExecutor;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.evaluation.SpelFunctionCallback;
import com.chutneytesting.engine.domain.execution.evaluation.SpelFunctions;
import com.chutneytesting.engine.domain.execution.strategies.StepExecutionStrategies;
import com.chutneytesting.engine.domain.execution.strategies.StepExecutionStrategy;
import com.chutneytesting.engine.domain.report.Reporter;
import com.chutneytesting.engine.infrastructure.delegation.HttpClient;
import com.chutneytesting.tools.ThrowingFunction;
import com.chutneytesting.tools.loader.ExtensionLoaders;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

public class ExecutionConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionConfiguration.class);

    private final ActionTemplateRegistry actionTemplateRegistry;
    private final Reporter reporter;
    private final ExecutionEngine executionEngine;
    private final TestEngine embeddedTestEngine;

    private final SpelFunctions spelFunctions;
    private final Set<StepExecutionStrategy> stepExecutionStrategies;

    private final Long reporterTTL;

    public ExecutionConfiguration() {
        this(5L, Executors.newFixedThreadPool(10), emptyMap(), null, null);
    }

    public ExecutionConfiguration(Long reporterTTL, ExecutorService actionExecutor, Map<String, String> actionsConfiguration, String user, String password) {
        this.reporterTTL = reporterTTL;

        ActionTemplateLoader actionTemplateLoaderV2 = createActionTemplateLoaderV2();
        spelFunctions = createSpelFunctions();
        stepExecutionStrategies = createStepExecutionStrategies();

        actionTemplateRegistry = new DefaultActionTemplateRegistry(new ActionTemplateLoaders(singletonList(actionTemplateLoaderV2)));
        reporter = createReporter();
        executionEngine = createExecutionEngine(actionExecutor, user, password);
        embeddedTestEngine = createEmbeddedTestEngine(new EngineActionsConfiguration(actionsConfiguration));
    }

    public ActionTemplateRegistry actionTemplateRegistry() {
        return actionTemplateRegistry;
    }

    public TestEngine embeddedTestEngine() {
        return embeddedTestEngine;
    }

    public Set<StepExecutionStrategy> stepExecutionStrategies() {
        return stepExecutionStrategies;
    }

    public Reporter reporter() {
        return reporter;
    }

    public ExecutionEngine executionEngine() {
        return executionEngine;
    }

    private ActionTemplateLoader createActionTemplateLoaderV2() {
        return new DefaultActionTemplateLoader<>(
            "chutney.actions",
            Action.class,
            new ActionTemplateParserV2());
    }

    private SpelFunctions createSpelFunctions() {
        SpelFunctionCallback spelFunctionCallback = new SpelFunctionCallback();
        ExtensionLoaders
            .classpathToClass("META-INF/extension/chutney.functions")
            .load().forEach(c -> ReflectionUtils.doWithMethods(c, spelFunctionCallback));

        return spelFunctionCallback.getSpelFunctions();
    }

    private Set<StepExecutionStrategy> createStepExecutionStrategies() {
        return ExtensionLoaders
            .classpathToClass("META-INF/extension/chutney.strategies")
            .load()
            .stream()
            .map(ThrowingFunction.toUnchecked(ExecutionConfiguration::<StepExecutionStrategy>instantiate))
            .map(identity(c -> LOGGER.debug("Loading strategy: " + c.getType() + " (" + c.getClass().getSimpleName() + ")")))
            .collect(Collectors.toSet());
    }

    private Reporter createReporter() {
        return new Reporter(reporterTTL);
    }

    private ExecutionEngine createExecutionEngine(ExecutorService actionExecutor, String user, String password) {
        return new DefaultExecutionEngine(
            new StepDataEvaluator(spelFunctions),
            new StepExecutionStrategies(stepExecutionStrategies),
            new DelegationService(new DefaultStepExecutor(actionTemplateRegistry), new HttpClient(user, password)),
            reporter,
            actionExecutor);
    }

    private TestEngine createEmbeddedTestEngine(ActionsConfiguration actionsConfiguration) {
        return new EmbeddedTestEngine(executionEngine, reporter, new ExecutionManager(), actionsConfiguration);
    }

    @SuppressWarnings("unchecked")
    private static <T> T instantiate(Class<?> clazz) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        return (T) clazz.getDeclaredConstructor().newInstance();
    }
}
