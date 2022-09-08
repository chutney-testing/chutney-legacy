package com.chutneytesting.action.spi.time;

import java.util.Optional;

public interface DurationParser {

    Optional<Duration> parse(String literalDuration);

    String description();
}
