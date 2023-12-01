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

package com.chutneytesting.design.domain.editionlock;

import static com.chutneytesting.design.domain.editionlock.TestCaseEdition.byEditor;
import static com.chutneytesting.design.domain.editionlock.TestCaseEdition.byId;
import static java.time.Instant.now;

import com.chutneytesting.server.core.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.server.core.domain.scenario.TestCaseRepository;
import java.util.List;

public class TestCaseEditionsService {

  private final TestCaseEditions testCaseEditions;
  private final TestCaseRepository testCaseRepository;

  public TestCaseEditionsService(TestCaseEditions testCaseEditions, TestCaseRepository testCaseRepository) {
    this.testCaseEditions = testCaseEditions;
    this.testCaseRepository = testCaseRepository;
  }

    public List<TestCaseEdition> getTestCaseEditions(String testCaseId) {
        return testCaseEditions.findBy(byId(testCaseId));
    }

    public TestCaseEdition editTestCase(String testCaseId, String user) {
        List<TestCaseEdition> edition = testCaseEditions.findBy(byId(testCaseId).and(byEditor(user)));
        if (!edition.isEmpty()) {
            return edition.get(0);
        }

        TestCaseEdition testCaseEdition = new TestCaseEdition(
            testCaseRepository.findById(testCaseId).orElseThrow(() -> new ScenarioNotFoundException(testCaseId)).metadata(),
            now(),
            user
        );

        if (testCaseEditions.add(testCaseEdition)) {
            return testCaseEdition;
        }

        throw new IllegalStateException("Cannot lock scenario edition");
    }

    public void endTestCaseEdition(String testCaseId, String user) {
        testCaseEditions.findBy(byId(testCaseId).and(byEditor(user)))
            .forEach(testCaseEditions::remove);
    }
}
