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

package com.chutneytesting.action.spi.time;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public enum DurationUnit {
    NANOS(TimeUnit.NANOSECONDS, 0.00001, "ns"),
    MICROS(TimeUnit.MICROSECONDS, 0.001, "\u00b5s", "\u03bcs"),
    MILLIS(TimeUnit.MILLISECONDS, 1, "ms"),
    SECONDS(TimeUnit.SECONDS, 1000, "s", "sec"),
    MINUTES(TimeUnit.MINUTES, 1000 * 60, "m", "min"),
    HOURS(TimeUnit.HOURS, 1000 * 60 * 60, "h", "hour", "hours", "hour(s)"),
    DAYS(TimeUnit.DAYS, 1000 * 60 * 60 * 24, "d", "day", "days", "day(s)");

    public final TimeUnit timeUnit;
    final double toMilliFactor;
    final Set<String> labels;
    final String mostRelevantLabel;

    DurationUnit(TimeUnit timeUnit, double toMilliFactor, String... labels) {
        this.timeUnit = timeUnit;
        this.toMilliFactor = toMilliFactor;
        this.labels = new HashSet<>(Arrays.asList(labels));
        mostRelevantLabel = labels[labels.length - 1];
    }

    public static DurationUnit parse(String label) {
        String unitText = label.trim().toLowerCase();
        return Arrays
            .stream(values())
            .filter(d -> d.labels.contains(unitText))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown time unit " + label + "; expected values : " + regex()));
    }

    @Override
    public String toString() {
        return mostRelevantLabel;
    }

    public static String regex() {
        List<String> allLabels = Arrays
            .stream(values())
            .flatMap(durationUnit -> durationUnit.labels.stream())
            .collect(Collectors.toList());
        return "(" + String.join("|", allLabels) + ")";
    }

}
