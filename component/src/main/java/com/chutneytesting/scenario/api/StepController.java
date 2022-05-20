package com.chutneytesting.scenario.api;

import static com.chutneytesting.scenario.api.ComposableStepMapper.fromDto;

import com.chutneytesting.scenario.api.dto.ComposableStepDto;
import com.chutneytesting.scenario.api.dto.ParentsStepDto;
import com.chutneytesting.scenario.domain.ComposableStep;
import com.chutneytesting.scenario.domain.ComposableStepRepository;
import com.chutneytesting.tools.ImmutablePaginatedDto;
import com.chutneytesting.tools.ImmutablePaginationRequestParametersDto;
import com.chutneytesting.tools.ImmutableSortRequestParametersDto;
import com.chutneytesting.tools.PaginatedDto;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    private final ComposableStepRepository composableStepRepository;

    public StepController(ComposableStepRepository composableStepRepository) {
        this.composableStepRepository = composableStepRepository;
    }

    @PreAuthorize("hasAuthority('COMPONENT_WRITE')")
    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public String save(@RequestBody ComposableStepDto step) {
        return composableStepRepository.save(fromDto(step));
    }

    @PreAuthorize("hasAuthority('COMPONENT_WRITE')")
    @DeleteMapping(path = "/{stepId}")
    public void deleteById(@PathVariable String stepId) {
        composableStepRepository.deleteById(stepId);
    }

    @PreAuthorize("hasAuthority('COMPONENT_READ') or hasAuthority('SCENARIO_WRITE')")
    @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ComposableStepDto> findAll() {
        return composableStepRepository.findAll()
            .stream()
            .map(ComposableStepMapper::toDto)
            .sorted(ComposableStepDto.stepDtoComparator)
            .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('COMPONENT_READ')")
    @GetMapping(path = "/{stepId}/parents", produces = MediaType.APPLICATION_JSON_VALUE)
    public ParentsStepDto findParents(@PathVariable String stepId) {
        return ParentStepMapper.toDto(composableStepRepository.findParents(stepId));
    }

    static final String FIND_STEPS_NAME_DEFAULT_VALUE = "";
    static final String FIND_STEPS_USAGE_DEFAULT_VALUE = "";
    static final String FIND_STEPS_START_DEFAULT_VALUE = "1";
    static final String FIND_STEPS_LIMIT_DEFAULT_VALUE = "25";

    @PreAuthorize("hasAuthority('COMPONENT_READ')")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public PaginatedDto<ComposableStepDto> findSteps(@RequestParam(defaultValue = FIND_STEPS_NAME_DEFAULT_VALUE) String name,
                                                     @RequestParam(defaultValue = FIND_STEPS_USAGE_DEFAULT_VALUE) String usage,
                                                     @RequestParam(defaultValue = FIND_STEPS_START_DEFAULT_VALUE) Long start,
                                                     @RequestParam(defaultValue = FIND_STEPS_LIMIT_DEFAULT_VALUE) Long limit,
                                                     @RequestParam(required = false) String sort,
                                                     @RequestParam(required = false) String desc) {

        PaginatedDto<ComposableStep> composableStepsPaginatedDto =
            composableStepRepository.find(
                ImmutablePaginationRequestParametersDto.builder()
                    .start(start)
                    .limit(limit)
                    .build(),
                ImmutableSortRequestParametersDto.builder()
                    .sort(sort)
                    .desc(desc)
                    .build(),
                ComposableStep.builder()
                    .withName(name)
                    .build()
            );

        return ImmutablePaginatedDto.<ComposableStepDto>builder()
            .totalCount(composableStepsPaginatedDto.totalCount())
            .addAllData(
                composableStepsPaginatedDto.data().stream()
                    .map(ComposableStepMapper::toDto)
                    .collect(Collectors.toList())
            )
            .build();
    }

    @PreAuthorize("hasAuthority('COMPONENT_READ')")
    @GetMapping(path = "/{stepId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ComposableStepDto findById(@PathVariable String stepId) {
        return ComposableStepMapper.toDto(
            composableStepRepository.findById(stepId)
        );
    }
}
