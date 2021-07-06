package com.chutneytesting.design.api.scenario.v2_0;

import static com.chutneytesting.tools.ui.ComposableIdUtils.fromFrontId;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;

import com.chutneytesting.design.api.scenario.v2_0.dto.GwtTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.RawTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.TestCaseIndexDto;
import com.chutneytesting.design.api.scenario.v2_0.mapper.GwtTestCaseMapper;
import com.chutneytesting.design.api.scenario.v2_0.mapper.RawTestCaseMapper;
import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import com.chutneytesting.execution.api.ExecutionSummaryDto;
import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import com.chutneytesting.security.infra.SpringUserService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/scenario/v2")
public class GwtTestCaseController {

    private final TestCaseRepository testCaseRepository;

    private final ExecutionHistoryRepository executionHistoryRepository;
    private final SpringUserService userService;

    public GwtTestCaseController(TestCaseRepository testCaseRepository,
                                 ExecutionHistoryRepository executionHistoryRepository,
                                 SpringUserService userService) {
        this.testCaseRepository = testCaseRepository;
        this.executionHistoryRepository = executionHistoryRepository;
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ')")
    @GetMapping(path = "/{testCaseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public GwtTestCaseDto getTestCase(@PathVariable("testCaseId") String testCaseId) {
        return GwtTestCaseMapper.toDto(testCaseRepository.findById(testCaseId));
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ')")
    @GetMapping(path = "/{testCaseId}/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    public TestCaseIndexDto testCaseMetaData(@PathVariable("testCaseId") String testCaseId) {
        TestCase testCase = testCaseRepository.findById(fromFrontId(testCaseId));
        return TestCaseIndexDto.from(testCase.metadata(), emptyList());
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ') or hasAuthority('CAMPAIGN_WRITE')")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TestCaseIndexDto> getTestCases(@RequestParam( name = "textFilter", required = false) String textFilter) {

        List<TestCaseMetadata> testCases = isNullOrEmpty(textFilter) ? testCaseRepository.findAll() : testCaseRepository.search(textFilter);
        return testCases.stream()
            .map((tc) -> {
                List<ExecutionSummaryDto> executions = ExecutionSummaryDto.toDto(
                    executionHistoryRepository.getExecutions(
                        fromFrontId(Optional.of(tc.id()))));
                return TestCaseIndexDto.from(tc, executions);
            })
            .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('SCENARIO_WRITE')")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String saveTestCase(@RequestBody GwtTestCaseDto testCase) {
        return saveOrUpdate(testCase);
    }

    @PreAuthorize("hasAuthority('SCENARIO_WRITE')")
    @PatchMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateTestCase(@RequestBody GwtTestCaseDto testCase) {
        return saveOrUpdate(testCase);
    }

    private String saveOrUpdate(GwtTestCaseDto testCase) {
        GwtTestCase gwtTestCase = GwtTestCaseMapper.fromDto(testCase);
        return gwtTestCaseSave(gwtTestCase);
    }

    @PreAuthorize("hasAuthority('SCENARIO_WRITE')")
    @DeleteMapping(path = "/{testCaseId}")
    public void removeScenarioById(@PathVariable("testCaseId") String testCaseId) {
        testCaseRepository.removeById(testCaseId);
    }

    /*
     * RAW Edition
     *
     * */

    @PreAuthorize("hasAuthority('SCENARIO_WRITE')")
    @PostMapping(path = "/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String saveTestCase(@RequestBody RawTestCaseDto rawTestCaseDto) {
        GwtTestCase gwtTestCase = RawTestCaseMapper.fromDto(rawTestCaseDto);
        return gwtTestCaseSave(gwtTestCase);
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ')")
    @GetMapping(path = "/raw/{testCaseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RawTestCaseDto getTestCaseById(@PathVariable("testCaseId") String testCaseId) {
        return RawTestCaseMapper.toDto(testCaseRepository.findById(testCaseId));
    }

    private String gwtTestCaseSave(GwtTestCase gwtTestCase) {
        gwtTestCase = GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.TestCaseMetadataBuilder.from(gwtTestCase.metadata)
                .withUpdateDate(now())
                .withAuthor(userService.currentUser().getId())
                .build())
            .withScenario(gwtTestCase.scenario)
            .withExecutionParameters(gwtTestCase.executionParameters)
            .build();
        return testCaseRepository.save(gwtTestCase);
    }
}
