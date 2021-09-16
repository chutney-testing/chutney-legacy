package blackbox.task;

import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.FinallyActionRegistry;

/**
 * Task registering itself as {@link FinallyAction}.
 * <p>
 * Used in a scenario, this task proves that there is no infinite-loop when a {@link FinallyAction} registers another
 * {@link FinallyAction} with the same identifier
 */
public class SelfRegisteringFinallyTask implements Task {

    private final FinallyActionRegistry finallyActionRegistry;

    public SelfRegisteringFinallyTask(FinallyActionRegistry finallyActionRegistry) {
        this.finallyActionRegistry = finallyActionRegistry;
    }

    @Override
    public TaskExecutionResult execute() {
        finallyActionRegistry.registerFinallyAction(FinallyAction.Builder.forAction("self-registering-finally", SelfRegisteringFinallyTask.class).build());
        return TaskExecutionResult.ok();
    }
}
