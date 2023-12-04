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

package com.chutneytesting.scenario.domain.gwt;

import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import java.util.Objects;

public class GwtTestCase implements TestCase {

    public final TestCaseMetadataImpl metadata;
    public final GwtScenario scenario;

    private GwtTestCase(TestCaseMetadataImpl metadata, GwtScenario scenario) {
        this.metadata = metadata;
        this.scenario = scenario;
    }

    @Override
    public TestCaseMetadata metadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "GwtTestCase{" +
            "metadata=" + metadata +
            ", scenario=" + scenario +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GwtTestCase that = (GwtTestCase) o;
        return metadata.equals(that.metadata) &&
            scenario.equals(that.scenario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, scenario);
    }

    public static GwtTestCaseBuilder builder() {
        return new GwtTestCaseBuilder();
    }

    public static class GwtTestCaseBuilder {

        private TestCaseMetadataImpl metadata;
        private GwtScenario scenario;

        private GwtTestCaseBuilder() {}

        public GwtTestCase build() {
            return new GwtTestCase(
                metadata,
                scenario
            );
        }

        public GwtTestCaseBuilder withMetadata(TestCaseMetadataImpl metadata) {
            this.metadata = metadata;
            return this;
        }

        public GwtTestCaseBuilder withScenario(GwtScenario scenario) {
            this.scenario = scenario;
            return this;
        }

        public GwtTestCaseBuilder from(GwtTestCase testCase) {
            withMetadata(testCase.metadata);
            withScenario(testCase.scenario);
            return this;
        }
    }
}
