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

package com.chutneytesting.campaign.domain;

public enum Frequency {

    HOURLY("Hourly"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    EMPTY("");

    public final String label;

    Frequency(String label) {
        this.label = label;
    }

    public static Frequency toFrequency(String label) {
        for (Frequency frequency : values()) {
            if (frequency.label.equals(label)) return frequency;
        }
        return EMPTY;
    }

    @Override
    public String toString() {
        return label;
    }
}
