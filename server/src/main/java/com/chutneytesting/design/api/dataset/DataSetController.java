package com.chutneytesting.design.api.dataset;

import static com.chutneytesting.design.api.dataset.DataSetMapper.fromDto;
import static com.chutneytesting.design.api.dataset.DataSetMapper.toDto;
import static com.chutneytesting.tools.ui.ComposableIdUtils.fromFrontId;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetHistoryRepository;
import com.chutneytesting.design.domain.dataset.DataSetNotFoundException;
import com.chutneytesting.design.domain.dataset.DataSetRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(DataSetController.BASE_URL)
@CrossOrigin(origins = "*")
public class DataSetController {

    public static final String BASE_URL = "/api/v1/datasets";

    private final DataSetRepository dataSetRepository;
    private final DataSetHistoryRepository dataSetHistoryRepository;

    public DataSetController(DataSetRepository dataSetRepository, DataSetHistoryRepository dataSetHistoryRepository) {
        this.dataSetRepository = dataSetRepository;
        this.dataSetHistoryRepository = dataSetHistoryRepository;
    }

    @PreAuthorize("hasAuthority('DATASET_READ')")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DataSetDto> findAll() {
        return dataSetRepository.findAll()
            .stream()
            .map(ds -> toDto(ds, lastVersionNumber(ds.id)))
            .sorted(DataSetDto.dataSetComparator)
            .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('DATASET_WRITE')")
    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public DataSetDto save(@RequestBody DataSetDto dataSetDto) {
        DataSet newDataSet = fromDto(dataSetDto);
        String newDataSetId = dataSetRepository.save(newDataSet);
        newDataSet = DataSet.builder().fromDataSet(newDataSet).withId(newDataSetId).build();
        Optional<Pair<String, Integer>> savedVersion = dataSetHistoryRepository.addVersion(newDataSet);
        return toDto(newDataSet, savedVersion.map(Pair::getRight).orElseGet(() -> lastVersionNumber(newDataSetId)));
    }

    @PreAuthorize("hasAuthority('DATASET_WRITE')")
    @PutMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public DataSetDto update(@RequestBody DataSetDto dataSetDto) {
        final DataSet dataSetToUpdate = fromDto(dataSetDto);
        return ofNullable(dataSetToUpdate.id)
            .map(id -> {
                Optional<Pair<String, Integer>> savedVersion = dataSetHistoryRepository.addVersion(dataSetToUpdate);
                if (savedVersion.isPresent()) {
                    dataSetRepository.save(dataSetToUpdate);
                    return toDto(dataSetToUpdate, savedVersion.get().getRight());
                }
                return findById(id);
            })
            .orElseThrow(() -> new DataSetNotFoundException(null));
    }

    @PreAuthorize("hasAuthority('DATASET_WRITE')")
    @DeleteMapping(path = "/{dataSetId}")
    public void deleteById(@PathVariable String dataSetId) {
        String dataSetBackId = fromFrontId(dataSetId);
        dataSetRepository.removeById(dataSetBackId);
        dataSetHistoryRepository.removeHistory(dataSetBackId);
    }

    @PreAuthorize("hasAuthority('DATASET_READ')")
    @GetMapping(path = "/{dataSetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DataSetDto findById(@PathVariable String dataSetId) {
        return toDto(
            dataSetRepository.findById(fromFrontId(dataSetId)),
            lastVersionNumber(dataSetId)
        );
    }

    @PreAuthorize("hasAuthority('DATASET_READ')")
    @GetMapping(path = "/{dataSetId}/versions/last", produces = MediaType.APPLICATION_JSON_VALUE)
    public Integer lastVersionNumber(@PathVariable String dataSetId) {
        return dataSetHistoryRepository.lastVersion(fromFrontId(dataSetId));
    }

    @PreAuthorize("hasAuthority('DATASET_READ')")
    @GetMapping(path = "/{dataSetId}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DataSetDto> allVersionNumbers(@PathVariable String dataSetId) {
        return dataSetHistoryRepository.allVersions(fromFrontId(dataSetId)).entrySet().stream()
            .map(e -> toDto(e.getValue(), e.getKey()))
            .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('DATASET_READ')")
    @GetMapping(path = {"/{dataSetId}/{version}", "/{dataSetId}/versions/{version}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataSetDto version(@PathVariable String dataSetId, @PathVariable Integer version) {
        return toDto(dataSetHistoryRepository.version(fromFrontId(dataSetId), version), version);
    }
}
