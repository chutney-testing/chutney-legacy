package com.chutneytesting.design.api.scenario.v2_0;

import static com.chutneytesting.tools.ui.ComposableIdUtils.fromFrontId;
import static com.chutneytesting.tools.ui.ComposableIdUtils.toFrontId;

import com.chutneytesting.design.api.scenario.v2_0.dto.GwtTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.RawTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.TestCaseIndexDto;
import com.chutneytesting.design.api.scenario.v2_0.mapper.GwtTestCaseMapper;
import com.chutneytesting.design.api.scenario.v2_0.mapper.RawTestCaseMapper;
import com.chutneytesting.design.domain.compose.ComposableTestCaseRepository;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.chutneytesting.execution.api.ExecutionSummaryDto;
import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/scenario/v2")
public class GwtTestCaseController {

    private final TestCaseRepository testCaseRepository;

    private final ExecutionHistoryRepository executionHistoryRepository;
    private final ComposableTestCaseRepository composableTestCaseRepository;

    public GwtTestCaseController(TestCaseRepository testCaseRepository,
                                 ExecutionHistoryRepository executionHistoryRepository, ComposableTestCaseRepository composableTestCaseRepository) {
        this.testCaseRepository = testCaseRepository;
        this.executionHistoryRepository = executionHistoryRepository;
        this.composableTestCaseRepository = composableTestCaseRepository;
    }

    @GetMapping(path = "/{testCaseId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public GwtTestCaseDto getTestCase(@PathVariable("testCaseId") String testCaseId) {
        return GwtTestCaseMapper.toDto(testCaseRepository.findById(testCaseId));
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<TestCaseIndexDto> getTestCases() {
        List<TestCaseMetadata> testCases = testCaseRepository.findAll();
        testCases.addAll(findAllComposableTestCase());
        return testCases.stream()
            .map((tc) -> {
                List<ExecutionSummaryDto> executions = ExecutionSummaryDto.toDto(
                    executionHistoryRepository.getExecutions(
                        fromFrontId(Optional.of(tc.id()))));
                return TestCaseIndexDto.from(tc, executions);
            })
            .collect(Collectors.toList());
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String saveTestCase(@RequestBody GwtTestCaseDto testCase) {
        return saveOrUpdate(testCase);
    }

    @PatchMapping(path = "", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String updateTestCase(@RequestBody GwtTestCaseDto testCase) {
        return saveOrUpdate(testCase);
    }

    private String saveOrUpdate(GwtTestCaseDto testCase) {
        return testCaseRepository.save(GwtTestCaseMapper.fromDto(testCase));
    }

    @DeleteMapping(path = "/{testCaseId}")
    public void removeScenarioById(@PathVariable("testCaseId") String testCaseId) {
        testCaseRepository.removeById(testCaseId);
    }

    private List<TestCaseMetadata> findAllComposableTestCase() {
        return composableTestCaseRepository.findAll().stream()
            .map(testCaseMetadata -> TestCaseMetadataImpl.TestCaseMetadataBuilder.from(testCaseMetadata)
                .withId(toFrontId(testCaseMetadata.id()))
                .build())
            .collect(Collectors.toList());
    }

    /*
     * RAW Edition
     *
     * */

    @PostMapping(path = "/raw", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String saveTestCase(@RequestBody RawTestCaseDto rawTestCaseDto) {
        return testCaseRepository.save(RawTestCaseMapper.fromDto(rawTestCaseDto));
    }

    @GetMapping(path = "/raw/{testCaseId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public RawTestCaseDto getTestCaseById(@PathVariable("testCaseId") String testCaseId) {
        return RawTestCaseMapper.toDto(testCaseRepository.findById(testCaseId));
    }

}
