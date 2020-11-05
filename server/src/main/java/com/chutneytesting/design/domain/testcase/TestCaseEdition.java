package com.chutneytesting.design.domain.testcase;

import static java.util.Objects.requireNonNull;

import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
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
