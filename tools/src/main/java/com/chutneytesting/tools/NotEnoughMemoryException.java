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

package com.chutneytesting.tools;

import java.text.DecimalFormat;

public class NotEnoughMemoryException extends RuntimeException {

    public NotEnoughMemoryException(long usedMemory, long maxMemory, String customMsg) {
        super(
            "Running step was stopped to prevent application crash. "
                + toMegaByte(usedMemory) + "MB memory used of " + toMegaByte(maxMemory) + "MB max."
                + "\n" + "Current step may not be the cause."
                + "\n" + customMsg
        );
    }

    private static String toMegaByte(long value) {
        return new DecimalFormat("#.##").format(value / (double)(1024 * 1024));
    }

}
