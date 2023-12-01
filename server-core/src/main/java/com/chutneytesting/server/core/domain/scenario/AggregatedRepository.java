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

package com.chutneytesting.server.core.domain.scenario;

import java.util.List;
import java.util.Optional;

public interface AggregatedRepository<T extends TestCase> {

    String save(T scenario);

    Optional<T> findById(String testCaseId);

    Optional<TestCaseMetadata> findMetadataById(String testCaseId);

    List<TestCaseMetadata> findAll();

    void removeById(String testCaseId);

    Optional<Integer> lastVersion(String testCaseId);

    List<TestCaseMetadata> search(String textFilter);

    Optional<TestCase> findExecutableById(String testCaseId);
}
