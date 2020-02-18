package com.chutneytesting.design.api.compose;

import static com.chutneytesting.design.api.compose.mapper.ComposableTestCaseMapper.fromDto;
import static com.chutneytesting.design.api.compose.mapper.ComposableTestCaseMapper.fromFrontId;
import static com.chutneytesting.design.api.compose.mapper.ComposableTestCaseMapper.toDto;
import static com.chutneytesting.design.api.compose.mapper.ComposableTestCaseMapper.toFrontId;

import com.chutneytesting.design.api.compose.dto.ComposableTestCaseDto;
import com.chutneytesting.design.domain.compose.ComposableTestCaseRepository;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import java.util.Optional;
import org.springframework.http.MediaType;
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

    public ComponentEditionController(ComposableTestCaseRepository composableTestCaseRepository, TestCaseRepository testCaseRepository) {
        this.composableTestCaseRepository = composableTestCaseRepository;
        this.testCaseRepository = testCaseRepository;
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String saveTestCase(@RequestBody ComposableTestCaseDto composableTestCaseDto) {
        return toFrontId( composableTestCaseRepository.save(fromDto(composableTestCaseDto)));
    }

    @GetMapping(path = "/{testCaseId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ComposableTestCaseDto getTestCase(@PathVariable("testCaseId") String testCaseId) {
        return toDto(composableTestCaseRepository.findById(fromFrontId(Optional.of(testCaseId))));
    }

    @DeleteMapping(path = "/{testCaseId}")
    public void removeScenarioById(@PathVariable("testCaseId") String testCaseId) {
        String testCaseBackId = fromFrontId(Optional.of(testCaseId));
        // TODO - Use Campaignrepository to delete potential association and executions
        testCaseRepository.removeById(testCaseBackId);
        composableTestCaseRepository.removeById(testCaseBackId);
    }
}
