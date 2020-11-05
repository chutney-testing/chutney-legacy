package com.chutneytesting.design.domain.testcase;

import static com.chutneytesting.design.domain.testcase.TestCaseEdition.byEditor;
import static com.chutneytesting.design.domain.testcase.TestCaseEdition.byId;
import static java.time.Instant.now;

import com.chutneytesting.design.domain.scenario.TestCaseRepository;
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
            testCaseRepository.findById(testCaseId).metadata(),
            now(),
            user
        );

        if (testCaseEditions.add(testCaseEdition)) {
            return testCaseEdition;
        }

        throw new IllegalStateException("Cannot add edition whithout exception !!");
    }

    public void endTestCaseEdition(String testCaseId, String user) {
        testCaseEditions.findBy(byId(testCaseId).and(byEditor(user)))
            .forEach(testCaseEditions::remove);
    }
}
