package com.chutneytesting;

import static com.chutneytesting.tools.Streams.identity;
import static java.util.Collections.singletonList;

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
import com.chutneytesting.task.domain.DefaultTaskTemplateRegistry;
import com.chutneytesting.task.domain.TaskTemplateLoader;
import com.chutneytesting.task.domain.TaskTemplateLoaders;
import com.chutneytesting.task.domain.TaskTemplateParserV2;
import com.chutneytesting.task.domain.TaskTemplateRegistry;
import com.chutneytesting.task.infra.DefaultTaskTemplateLoader;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.tools.ThrowingFunction;
import com.chutneytesting.tools.loader.ExtensionLoaders;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

public class ExecutionConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionConfiguration.class);

    private final TaskTemplateRegistry taskTemplateRegistry;
    private final Reporter reporter;
    private final ExecutionEngine executionEngine;
    private final TestEngine embeddedTestEngine;

    private final SpelFunctions spelFunctions;
    private final Set<StepExecutionStrategy> stepExecutionStrategies;

    private final Long reporterTTL;

    public ExecutionConfiguration() {
        this(5L);
    }

    public ExecutionConfiguration(Long reporterTTL) {
        this.reporterTTL = reporterTTL;

        TaskTemplateLoader taskTemplateLoaderV2 = createTaskTemplateLoaderV2();
        spelFunctions = createSpelFunctions();
        stepExecutionStrategies = createStepExecutionStrategies();

        taskTemplateRegistry = new DefaultTaskTemplateRegistry(new TaskTemplateLoaders(singletonList(taskTemplateLoaderV2)));
        reporter = createReporter();
        executionEngine = createExecutionEngine();
        embeddedTestEngine = createEmbeddedTestEngine();
    }

    public TaskTemplateRegistry taskTemplateRegistry() {
        return taskTemplateRegistry;
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

    private TaskTemplateLoader createTaskTemplateLoaderV2() {
        return new DefaultTaskTemplateLoader<>(
            "chutney.tasks",
            Task.class,
            new TaskTemplateParserV2());
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

    private ExecutionEngine createExecutionEngine() {
        return new DefaultExecutionEngine(
            new StepDataEvaluator(spelFunctions),
            new StepExecutionStrategies(stepExecutionStrategies),
            new DelegationService(new DefaultStepExecutor(taskTemplateRegistry), new HttpClient()),
            reporter);
    }

    private TestEngine createEmbeddedTestEngine() {
        return new EmbeddedTestEngine(executionEngine, reporter, new ExecutionManager());
    }

    @SuppressWarnings("unchecked")
    private static <T> T instantiate(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        return (T) clazz.newInstance();
    }
}
