package com.chutneytesting.engine.domain.execution.engine;

import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.engine.parameterResolver.ContextParameterResolver;
import com.chutneytesting.engine.domain.execution.engine.parameterResolver.DelegateLogger;
import com.chutneytesting.engine.domain.execution.engine.parameterResolver.InputParameterResolver;
import com.chutneytesting.engine.domain.execution.engine.parameterResolver.TypedValueParameterResolver;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.engine.step.StepContext;
import com.chutneytesting.task.domain.TaskTemplate;
import com.chutneytesting.task.domain.TaskTemplateRegistry;
import com.chutneytesting.task.domain.parameter.ParameterResolver;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.spi.injectable.TasksConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.LoggerFactory;

public class DefaultStepExecutor implements StepExecutor {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DefaultStepExecutor.class);

    private final TaskTemplateRegistry taskTemplateRegistry;

    public DefaultStepExecutor(TaskTemplateRegistry taskTemplateRegistry) {
        this.taskTemplateRegistry = taskTemplateRegistry;
    }

    @Override
    public void execute(ScenarioExecution scenarioExecution, StepContext stepContext, Target targetServer, Step step) {
        String type = step.type();

        Optional<TaskTemplate> matchedTask = taskTemplateRegistry.getByIdentifier(type);

        if (matchedTask.isPresent()) {
            List<ParameterResolver> parameterResolvers = gatherResolvers(scenarioExecution, stepContext, targetServer, step);

            TaskExecutionResult executionResult;
            try {
                Task task = matchedTask.get().create(parameterResolvers);
                List<String> errors = task.validateInputs();
                if (errors.isEmpty()) {
                    executionResult = task.execute();
                    updateStepFromTaskResult(step, executionResult);
                    updateStepContextFromTaskResult(stepContext, executionResult);
                } else {
                    step.failure(errors.toArray(new String[0]));
                }
            } catch (Exception e) {
                LOGGER.error("Cannot execute step: ", e);
                step.failure("Task [" + type + "] failed: " + ofNullable(e.getMessage()).orElse(e.toString()));
            }
        } else if (type.isEmpty()) {
            step.success();
        } else {
            step.failure("Task [" + type + "] not found");
        }

    }

    private void updateStepContextFromTaskResult(StepContext stepContext, TaskExecutionResult executionResult) {
        if (executionResult.status == TaskExecutionResult.Status.Success) {
            stepContext.addStepOutputs(executionResult.outputs);
            stepContext.getScenarioContext().putAll(executionResult.outputs);
        }
    }

    private void updateStepFromTaskResult(Step step, TaskExecutionResult executionResult) {
        if (executionResult.status == TaskExecutionResult.Status.Success) {
            step.success();
        } else {
            step.failure();
        }
    }

    private List<ParameterResolver> gatherResolvers(ScenarioExecution scenarioExecution, StepContext stepContext, Target target, Step step) {
        List<ParameterResolver> parameterResolvers = new ArrayList<>();
        parameterResolvers.add(new InputParameterResolver(stepContext.getEvaluatedInputs()));
        parameterResolvers.add(new TypedValueParameterResolver<>(Target.class, target));
        parameterResolvers.add(new TypedValueParameterResolver<>(Logger.class, new DelegateLogger(step::addInformation, step::failure)));
        parameterResolvers.add(new TypedValueParameterResolver<>(StepDefinition.class, step.definition()));
        parameterResolvers.add(new TypedValueParameterResolver<>(FinallyActionRegistry.class, scenarioExecution::registerFinallyAction));
        parameterResolvers.add(new TypedValueParameterResolver<>(TasksConfiguration.class, scenarioExecution.getTasksConfiguration()));
        parameterResolvers.add(new ContextParameterResolver(stepContext.getScenarioContext()));
        return parameterResolvers;
    }
}
