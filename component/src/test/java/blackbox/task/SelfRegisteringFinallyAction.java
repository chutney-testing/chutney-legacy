package blackbox.task;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;

/**
 * Action registering itself as {@link FinallyAction}.
 * <p>
 * Used in a scenario, this action proves that there is no infinite-loop when a {@link FinallyAction} registers another
 * {@link FinallyAction} with the same identifier
 */
public class SelfRegisteringFinallyAction implements Action {

    private final FinallyActionRegistry finallyActionRegistry;

    public SelfRegisteringFinallyAction(FinallyActionRegistry finallyActionRegistry) {
        this.finallyActionRegistry = finallyActionRegistry;
    }

    @Override
    public ActionExecutionResult execute() {
        finallyActionRegistry.registerFinallyAction(FinallyAction.Builder.forAction("self-registering-finally", SelfRegisteringFinallyAction.class).build());
        return ActionExecutionResult.ok();
    }
}
