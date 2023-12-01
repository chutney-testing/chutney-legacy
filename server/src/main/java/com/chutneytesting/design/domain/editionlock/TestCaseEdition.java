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

import static java.util.Objects.requireNonNull;

import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Predicate;

public class TestCaseEdition {

    public final TestCaseMetadata testCaseMetadata;
    public final Instant startDate;
    public final String editor;

    public TestCaseEdition(TestCaseMetadata testCaseMetadata, Instant startDate, String editor) {
        requireNonNull(testCaseMetadata);
        requireNonNull(startDate);
        requireNonNull(editor);

        this.testCaseMetadata = testCaseMetadata;
        this.startDate = startDate;
        this.editor = editor;
    }

    public static Predicate<TestCaseEdition> byId(String testCaseId) {
        return tce -> tce.testCaseMetadata.id().equals(testCaseId);
    }

    public static Predicate<TestCaseEdition> byEditor(String editor) {
        return tce -> tce.editor.equals(editor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCaseEdition that = (TestCaseEdition) o;
        return Objects.equals(testCaseMetadata, that.testCaseMetadata) &&
            Objects.equals(startDate, that.startDate) &&
            Objects.equals(editor, that.editor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testCaseMetadata, startDate, editor);
    }

    @Override
    public String toString() {
        return "TestCaseEdition{" +
            "testCaseMetadata=" + testCaseMetadata +
            ", startDate=" + startDate +
            ", editor='" + editor + '\'' +
            '}';
    }
}
