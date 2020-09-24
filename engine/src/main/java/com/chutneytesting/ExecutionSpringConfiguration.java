package com.chutneytesting;

import static com.chutneytesting.tools.Streams.identity;

import com.chutneytesting.engine.api.execution.EmbeddedTestEngine;
import com.chutneytesting.engine.api.execution.TestEngine;
import com.chutneytesting.engine.domain.delegation.DelegationClient;
import com.chutneytesting.engine.domain.delegation.DelegationService;
import com.chutneytesting.engine.domain.execution.ExecutionEngine;
import com.chutneytesting.engine.domain.execution.ExecutionManager;
import com.chutneytesting.engine.domain.execution.engine.DefaultExecutionEngine;
import com.chutneytesting.engine.domain.execution.engine.DefaultStepExecutor;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.ReflectionUtils;

@Configuration
public class ExecutionSpringConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionSpringConfiguration.class);

    @Bean
    @Order(value = Ordered.HIGHEST_PRECEDENCE)
    TaskTemplateLoader taskTemplateLoaderV2() {
        return new DefaultTaskTemplateLoader<>(
            "chutney.tasks",
            Task.class,
            new TaskTemplateParserV2());
    }

    @Bean
    TaskTemplateRegistry taskTemplateRegistry(List<TaskTemplateLoader> taskTemplateLoaders) {
        return new DefaultTaskTemplateRegistry(new TaskTemplateLoaders(taskTemplateLoaders));
    }

    @Bean
    SpelFunctions functions() {
        SpelFunctionCallback spelFunctionCallback = new SpelFunctionCallback();
        ExtensionLoaders
            .classpathToClass("META-INF/extension/chutney.functions")
            .load().forEach(c -> ReflectionUtils.doWithMethods(c, spelFunctionCallback));

        return spelFunctionCallback.getSpelFunctions();
    }

    @Bean
    public Set<StepExecutionStrategy> stepExecutionStrategies() {
        return ExtensionLoaders
            .classpathToClass("META-INF/extension/chutney.strategies")
            .load()
            .stream()
            .map(ThrowingFunction.toUnchecked(ExecutionSpringConfiguration::<StepExecutionStrategy>instantiate))
            .map(identity(c -> LOGGER.debug("Loading strategy: " + c.getType() + " (" + c.getClass().getSimpleName() + ")")))
            .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private static <T> T instantiate(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        return (T) clazz.newInstance();
    }

    @Bean
    StepExecutionStrategies stepExecutionStrategyResolver(Set<StepExecutionStrategy> strategies) {
        return new StepExecutionStrategies(strategies);
    }

    @Bean
    StepDataEvaluator stepDataEvaluator(SpelFunctions spelFunctions) {
        return new StepDataEvaluator(spelFunctions);
    }

    @Bean
    ExecutionEngine executionEngine(StepDataEvaluator evaluator,
                                    StepExecutionStrategies stepExecutionStrategies,
                                    DelegationService delegationService,
                                    Reporter reporter) {
        return new DefaultExecutionEngine(evaluator, stepExecutionStrategies, delegationService, reporter);
    }

    @Bean
    TestEngine embeddedTestEngine(ExecutionEngine executionEngine, Reporter reporter, ExecutionManager executionManager) {
        return new EmbeddedTestEngine(executionEngine, reporter, executionManager);
    }

    @Bean
    DelegationService delegationService(StepExecutor executor,
                                        DelegationClient delegationClient) {
        return new DelegationService(executor, delegationClient);
    }

    @Bean
    DelegationClient delegationClient() {
        return new HttpClient();
    }

    @Bean
    Reporter reporter(@Value("${chutney.engine.reporter.publisher.ttl:5}") long reportRetention) {
        return new Reporter(reportRetention);
    }

    @Bean
    ExecutionManager executionManager() {
        return new ExecutionManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public StepExecutor stepExecutor(TaskTemplateRegistry taskTemplateRegistry) {
        return new DefaultStepExecutor(taskTemplateRegistry);
    }
}
