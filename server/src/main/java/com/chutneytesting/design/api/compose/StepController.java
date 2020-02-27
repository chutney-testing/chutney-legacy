package com.chutneytesting.design.api.compose;

import static com.chutneytesting.design.api.compose.mapper.ComposableTestCaseMapper.toFrontId;
import static com.chutneytesting.design.api.compose.mapper.FunctionalStepMapper.fromDto;
import static com.chutneytesting.design.api.compose.mapper.ComposableTestCaseMapper.fromFrontId;

import com.chutneytesting.design.api.compose.dto.FunctionalStepDto;
import com.chutneytesting.design.api.compose.dto.ParentsStepDto;
import com.chutneytesting.design.api.compose.mapper.FunctionalStepMapper;
import com.chutneytesting.design.api.compose.mapper.ParentStepMapper;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.compose.StepRepository;
import com.chutneytesting.design.domain.compose.StepUsage;
import com.chutneytesting.tools.ImmutablePaginatedDto;
import com.chutneytesting.tools.ImmutablePaginationRequestParametersDto;
import com.chutneytesting.tools.ImmutableSortRequestParametersDto;
import com.chutneytesting.tools.PaginatedDto;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(StepController.BASE_URL)
@CrossOrigin(origins = "*")
public class StepController {

    static final String BASE_URL = "/api/steps/v1";

    private StepRepository stepRepository;

    public StepController(StepRepository stepRepository) {
        this.stepRepository = stepRepository;
    }

    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String save(@RequestBody FunctionalStepDto step) {
        return toFrontId(stepRepository.save(fromDto(step)));
    }

    @PostMapping(path = "/delete")
    public void deleteById(@RequestBody String stepId) {
        stepRepository.deleteById(fromFrontId(stepId));
    }

    @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<FunctionalStepDto> findAll() {
        return stepRepository.findAll()
            .stream()
            .map(FunctionalStepMapper::toDto)
            .sorted(FunctionalStepDto.stepDtoComparator)
            .collect(Collectors.toList());
    }

    @GetMapping(path = "/{stepId}/parents", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ParentsStepDto findParents(@PathVariable String stepId) {
        return ParentStepMapper.toDto(stepRepository.findParents(fromFrontId(stepId)));
    }

    static final String FIND_STEPS_NAME_DEFAULT_VALUE = "";
    static final String FIND_STEPS_USAGE_DEFAULT_VALUE = "";
    static final String FIND_STEPS_START_DEFAULT_VALUE = "1";
    static final String FIND_STEPS_LIMIT_DEFAULT_VALUE = "25";

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PaginatedDto<FunctionalStepDto> findSteps(@RequestParam(defaultValue = FIND_STEPS_NAME_DEFAULT_VALUE) String name,
                                                     @RequestParam(defaultValue = FIND_STEPS_USAGE_DEFAULT_VALUE) String usage,
                                                     @RequestParam(defaultValue = FIND_STEPS_START_DEFAULT_VALUE) Long start,
                                                     @RequestParam(defaultValue = FIND_STEPS_LIMIT_DEFAULT_VALUE) Long limit,
                                                     @RequestParam(required = false) String sort,
                                                     @RequestParam(required = false) String desc) {

        PaginatedDto<FunctionalStep> functionalStepsPaginatedDto =
            stepRepository.find(
                ImmutablePaginationRequestParametersDto.builder()
                    .start(start)
                    .limit(limit)
                    .build(),
                ImmutableSortRequestParametersDto.builder()
                    .sort(sort)
                    .desc(desc)
                    .build(),
                FunctionalStep.builder()
                    .withName(name)
                    .withUsage(StepUsage.fromName(usage))
                    .withSteps(Collections.emptyList())
                    .build()
            );

        return ImmutablePaginatedDto.<FunctionalStepDto>builder()
            .totalCount(functionalStepsPaginatedDto.totalCount())
            .addAllData(
                functionalStepsPaginatedDto.data().stream()
                    .map(FunctionalStepMapper::toDto)
                    .collect(Collectors.toList())
            )
            .build();
    }

    @GetMapping(path = "/{stepId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public FunctionalStepDto findById(@PathVariable String stepId) {
        return FunctionalStepMapper.toDto(
            stepRepository.findById(fromFrontId(stepId))
        );
    }

    @PostMapping(path = "/search/name", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<FunctionalStepDto> findIdenticalStepsByName(@RequestBody String name) {
        List<FunctionalStep> foundFSteps = stepRepository.queryByName(name);

        return foundFSteps.stream()
            .map(FunctionalStepMapper::toDto)
            .collect(Collectors.toList());
    }

}
