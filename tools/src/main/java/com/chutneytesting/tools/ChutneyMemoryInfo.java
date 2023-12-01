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

public class ChutneyMemoryInfo {

    private static final long MAX_MEMORY = Runtime.getRuntime().maxMemory(); // Fixed at startup
    private static final int MINIMUM_MEMORY_PERCENTAGE_REQUIRED = 5;
    private static final long MINIMUM_MEMORY_REQUIRED = (MAX_MEMORY / 100) * MINIMUM_MEMORY_PERCENTAGE_REQUIRED;

    public static long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public static boolean hasEnoughAvailableMemory() {
        return availableMemory() > MINIMUM_MEMORY_REQUIRED;
    }

    public static long maxMemory() {
        return MAX_MEMORY;
    }

    private static long availableMemory() {
        return MAX_MEMORY - usedMemory();
    }
}
