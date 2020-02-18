package com.chutneytesting.task.jms.consumer;

import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.time.Duration;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class JmsListenerParameters {

    private static final String DESTINATION = "destination";
    private static final String MESSAGE_SELECTOR = "selector";
    private static final String MESSAGE_BODY_SELECTOR = "bodySelector";
    private static final String BROWSER_MAX_DEPTH = "browserMaxDepth";
    private static final int DEFAULT_BROWSER_MAX_DEPTH = 30;
    private static final String TIMEOUT = "timeOut";

    public final String destination;
    public final String selector;
    public final String bodySelector;
    public final Integer browserMaxDepth;
    public final Long timeout;

    public JmsListenerParameters(@Input(DESTINATION) String destination,
                                 @Input(MESSAGE_SELECTOR)String selector,
                                 @Input(MESSAGE_BODY_SELECTOR) String bodySelector,
                                 @Input(BROWSER_MAX_DEPTH) Integer browserMaxDepth,
                                 @Input(TIMEOUT) String timeout) {
        this.destination = destination;
        this.selector = selector;
        this.bodySelector = bodySelector;
        this.browserMaxDepth = Optional.ofNullable(browserMaxDepth)
            .map(String::valueOf)
            .map(Integer::parseInt)
            .orElse(DEFAULT_BROWSER_MAX_DEPTH);

        this.timeout = Optional.ofNullable(timeout)
            .filter(StringUtils::isNotBlank)
            .map(Duration::parse)
            .map(Duration::toMilliseconds)
            .orElse(500L);
    }
}
