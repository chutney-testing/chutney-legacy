package com.chutneytesting.design.domain.editionlock;

import static com.chutneytesting.design.domain.editionlock.TestCaseEdition.byEditor;
import static com.chutneytesting.design.domain.editionlock.TestCaseEdition.byId;
import static java.time.Instant.now;

import com.chutneytesting.scenario.domain.AggregatedRepository;
import com.chutneytesting.scenario.domain.ScenarioNotFoundException;
import com.chutneytesting.scenario.domain.TestCaseRepository;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import java.util.List;

public class TestCaseEditionsService {

    private final TestCaseEditions testCaseEditions;
    private final AggregatedRepository<GwtTestCase> testCaseRepository;

    public TestCaseEditionsService(TestCaseEditions testCaseEditions, AggregatedRepository<GwtTestCase> testCaseRepository) {
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
