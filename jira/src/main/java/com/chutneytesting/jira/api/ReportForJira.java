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

package com.chutneytesting.jira.api;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.time.Instant;
import java.util.List;

public class ReportForJira {

    public final Instant startDate;
    public final Long duration;
    public final String status;
    public final Step rootStep;
    public final String environment;

    public ReportForJira(Instant startDate, Long duration, String status, Step rootStep, String environment) {
        this.startDate = startDate;
        this.duration = duration;
        this.status = status;
        this.rootStep = rootStep;
        this.environment = environment;
    }

    public static class Step {
        public final String name;
        public final List<String> errors;
        public final List<Step> steps;

        public Step(String name, List<String> errors, List<Step> steps) {
            this.name = name;
            this.errors = ofNullable(errors).orElse(emptyList());
            this.steps = ofNullable(steps).orElse(emptyList());
        }
    }
}
