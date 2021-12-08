package com.chutneytesting.design.api.scenario.compose;

import static com.chutneytesting.design.api.scenario.compose.mapper.ComposableTestCaseMapper.fromDto;
import static com.chutneytesting.design.api.scenario.compose.mapper.ComposableTestCaseMapper.toDto;
import static com.chutneytesting.tools.orient.ComposableIdUtils.fromFrontId;
import static com.chutneytesting.tools.orient.ComposableIdUtils.toFrontId;
import static java.time.Instant.now;

import com.chutneytesting.design.api.scenario.compose.dto.ComposableTestCaseDto;
import com.chutneytesting.design.api.scenario.compose.mapper.ExecutableComposableTestCaseMapper;
import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.chutneytesting.design.domain.scenario.compose.ComposableTestCase;
import com.chutneytesting.design.domain.scenario.compose.ComposableTestCaseRepository;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.compiler.TestCasePreProcessors;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedTestCase;
import com.chutneytesting.security.infra.SpringUserService;
import com.chutneytesting.tools.ui.KeyValue;
import java.util.List;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(ComponentEditionController.BASE_URL)
public class ComponentEditionController {

    static final String BASE_URL = "/api/scenario/component-edition";

    private final ComposableTestCaseRepository composableTestCaseRepository;
    private final TestCaseRepository testCaseRepository;
    private final SpringUserService userService;
    private final TestCasePreProcessors testCasePreProcessors;

    public ComponentEditionController(ComposableTestCaseRepository composableTestCaseRepository, TestCaseRepository testCaseRepository, SpringUserService userService, TestCasePreProcessors testCasePreProcessors) {
        this.composableTestCaseRepository = composableTestCaseRepository;
        this.testCaseRepository = testCaseRepository;
        this.userService = userService;
        this.testCasePreProcessors = testCasePreProcessors;
    }

    @PreAuthorize("hasAuthority('SCENARIO_WRITE')")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String saveTestCase(@RequestBody ComposableTestCaseDto composableTestCaseDto) {
        ComposableTestCase composableTestCase = fromDto(composableTestCaseDto);
        composableTestCase = new ComposableTestCase(
            composableTestCase.id,
            TestCaseMetadataImpl.TestCaseMetadataBuilder.from(composableTestCase.metadata)
                .withUpdateDate(now())
                .withAuthor(userService.currentUser().getId())
                .build(),
            composableTestCase.composableScenario
        );
        return toFrontId(composableTestCaseRepository.save(composableTestCase));
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ')")
    @GetMapping(path = "/{testCaseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ComposableTestCaseDto getTestCase(@PathVariable("testCaseId") String testCaseId) {
        return toDto(composableTestCaseRepository.findById(fromFrontId(Optional.of(testCaseId))));
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_WRITE')")
    @GetMapping(path = "/{testCaseId}/executable/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<KeyValue> getTestCaseExecutionParameters(@PathVariable("testCaseId") String testCaseId) {
        return toDto(composableTestCaseRepository.findById(fromFrontId(Optional.of(testCaseId)))).executionParameters();
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ')")
    @GetMapping(path = "/{testCaseId}/executable", produces = MediaType.APPLICATION_JSON_VALUE)
    public ComposableTestCaseDto getExecutableTestCase(@PathVariable("testCaseId") String testCaseId) {
        TestCase testCase = testCaseRepository.findById(fromFrontId(Optional.of(testCaseId)));
        ExecutableComposedTestCase result = testCasePreProcessors.apply(new ExecutionRequest(testCase, "env", "userId"));

        return ExecutableComposableTestCaseMapper.toDto(result);
    }

    @PreAuthorize("hasAuthority('SCENARIO_WRITE')")
    @DeleteMapping(path = "/{testCaseId}")
    public void removeScenarioById(@PathVariable("testCaseId") String testCaseId) {
        String testCaseBackId = fromFrontId(Optional.of(testCaseId));
        // TODO - Use Campaignrepository to delete potential association and executions
        testCaseRepository.removeById(testCaseBackId);
        composableTestCaseRepository.removeById(testCaseBackId);
    }
}
