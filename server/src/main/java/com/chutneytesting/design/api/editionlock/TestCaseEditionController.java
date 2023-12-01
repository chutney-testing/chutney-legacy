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

package com.chutneytesting.design.api.editionlock;

import static java.util.stream.Collectors.toList;

import com.chutneytesting.design.domain.editionlock.TestCaseEdition;
import com.chutneytesting.design.domain.editionlock.TestCaseEditionsService;
import com.chutneytesting.security.infra.SpringUserService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final SpringUserService userService;

    public TestCaseEditionController(TestCaseEditionsService testCaseEditionsService, SpringUserService userService) {
        this.testCaseEditionsService = testCaseEditionsService;
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ')")
    @GetMapping(path = "/{testCaseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TestCaseEditionDto> testCasesEditions(@PathVariable("testCaseId") String testCaseId) {
        return testCaseEditionsService.getTestCaseEditions(testCaseId).stream()
            .map(TestCaseEditionController::toDto)
            .collect(toList());
    }

    @PreAuthorize("hasAuthority('SCENARIO_WRITE')")
    @PostMapping(path = "/{testCaseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TestCaseEditionDto editTestCase(@PathVariable("testCaseId") String testCaseId) {
        return toDto(testCaseEditionsService.editTestCase(testCaseId, userService.currentUser().getId()));
    }

    @PreAuthorize("hasAuthority('SCENARIO_WRITE')")
    @DeleteMapping(path = "/{testCaseId}")
    public void endTestCaseEdition(@PathVariable("testCaseId") String testCaseId) {
        testCaseEditionsService.endTestCaseEdition(testCaseId, userService.currentUser().getId());
    }

    private static TestCaseEditionDto toDto(TestCaseEdition tcEdition) {
        return ImmutableTestCaseEditionDto.builder()
            .testCaseId(tcEdition.testCaseMetadata.id())
            .testCaseVersion(tcEdition.testCaseMetadata.version())
            .editionUser(tcEdition.editor)
            .editionStartDate(tcEdition.startDate)
            .build();
    }
}
