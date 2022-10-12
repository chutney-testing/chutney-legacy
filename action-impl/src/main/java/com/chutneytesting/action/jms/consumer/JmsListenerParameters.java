package com.chutneytesting.action.jms.consumer;

import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.durationValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.validation.Validator;

public class JmsListenerParameters {

    private static final String DESTINATION = "destination";
    private static final String MESSAGE_SELECTOR = "selector";
    private static final String MESSAGE_BODY_SELECTOR = "bodySelector";
    private static final String BROWSER_MAX_DEPTH = "browserMaxDepth";
    private static final int DEFAULT_BROWSER_MAX_DEPTH = 30;
    private static final String TIMEOUT = "timeOut";
    private static final String DEFAULT_TIMEOUT = "500 ms";

    public final String destination;
    public final String selector;
    public final String bodySelector;
    public final Integer browserMaxDepth;
    public final String timeout;

    public JmsListenerParameters(@Input(DESTINATION) String destination,
                                 @Input(MESSAGE_SELECTOR) String selector,
                                 @Input(MESSAGE_BODY_SELECTOR) String bodySelector,
                                 @Input(BROWSER_MAX_DEPTH) Integer browserMaxDepth,
                                 @Input(TIMEOUT) String timeout) {
        this.destination = destination;
        this.selector = selector;
        this.bodySelector = bodySelector;
        this.browserMaxDepth = ofNullable(browserMaxDepth).orElse(DEFAULT_BROWSER_MAX_DEPTH);
        this.timeout = ofNullable(timeout).orElse(DEFAULT_TIMEOUT);
    }

    public static Validator[] validateJmsListenerParameters(JmsListenerParameters listenerJmsParameters) {
        return new Validator[]{
            notBlankStringValidation(listenerJmsParameters.destination, "destination"),
            durationValidation(listenerJmsParameters.timeout, "timeout")
        };
    }
}
