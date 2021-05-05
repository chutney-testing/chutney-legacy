package com.chutneytesting.design.api.editionlock;

import static com.chutneytesting.tools.ui.ComposableIdUtils.fromFrontId;
import static com.chutneytesting.tools.ui.ComposableIdUtils.toFrontId;
import static java.util.stream.Collectors.toList;

import com.chutneytesting.design.domain.editionlock.TestCaseEdition;
import com.chutneytesting.design.domain.editionlock.TestCaseEditionsService;
import com.chutneytesting.security.domain.UserService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(TestCaseEditionController.BASE_URL)
public class TestCaseEditionController {

    static final String BASE_URL = "/api/v1/editions/testcases";

    private final TestCaseEditionsService testCaseEditionsService;
    private final UserService userService;

    public TestCaseEditionController(TestCaseEditionsService testCaseEditionsService, UserService userService) {
        this.testCaseEditionsService = testCaseEditionsService;
        this.userService = userService;
    }

    @GetMapping(path = "/{testCaseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TestCaseEditionDto> testCasesEditions(@PathVariable("testCaseId") String testCaseId) {
        return testCaseEditionsService.getTestCaseEditions(fromFrontId(testCaseId)).stream()
            .map(TestCaseEditionController::toDto)
            .collect(toList());
    }

    @PostMapping(path = "/{testCaseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TestCaseEditionDto editTestCase(@PathVariable("testCaseId") String testCaseId) {
        return toDto(testCaseEditionsService.editTestCase(fromFrontId(testCaseId), userService.getCurrentUser().getId()));
    }

    @DeleteMapping(path = "/{testCaseId}")
    public void endTestCaseEdition(@PathVariable("testCaseId") String testCaseId) {
        testCaseEditionsService.endTestCaseEdition(fromFrontId(testCaseId), userService.getCurrentUser().getId());
    }

    private static TestCaseEditionDto toDto(TestCaseEdition tcEdition) {
        return ImmutableTestCaseEditionDto.builder()
            .testCaseId(toFrontId(tcEdition.testCaseMetadata.id()))
            .testCaseVersion(tcEdition.testCaseMetadata.version())
            .editionUser(tcEdition.editor)
            .editionStartDate(tcEdition.startDate)
            .build();
    }
}
