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

package com.chutneytesting.admin.domain;

public interface DBVacuum {

    /**
     * Try to compact database
     */
    VacuumReport vacuum();

    /**
     * Compute current database size in bytes
     *
     * @return The size in bytes
     */
    long size();

    record VacuumReport(Long beforeSize, Long afterSize) {
    }
}
