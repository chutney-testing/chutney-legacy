package com.chutneytesting.design.api.dataset;

import static com.chutneytesting.design.api.dataset.DataSetMapper.fromDto;
import static com.chutneytesting.design.api.dataset.DataSetMapper.toDto;
import static com.chutneytesting.tools.ui.OrientUtils.fromFrontId;
import static com.chutneytesting.tools.ui.OrientUtils.toFrontId;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetHistoryRepository;
import com.chutneytesting.design.domain.dataset.DataSetRepository;
import java.util.List;
import java.util.stream.Collectors;
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
@RequestMapping(DataSetController.BASE_URL)
@CrossOrigin(origins = "*")
public class DataSetController {

    public static final String BASE_URL = "/api/v1/datasets";

    private DataSetRepository dataSetRepository;
    private DataSetHistoryRepository dataSetHistoryRepository;

    public DataSetController(DataSetRepository dataSetRepository, DataSetHistoryRepository dataSetHistoryRepository) {
        this.dataSetRepository = dataSetRepository;
        this.dataSetHistoryRepository = dataSetHistoryRepository;
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<DataSetDto> findAll() {
        return dataSetRepository.findAll().entrySet()
            .stream()
            .map(e -> toDto(e.getKey(), e.getValue()))
            .sorted(DataSetDto.dataSetComparator)
            .collect(Collectors.toList());
    }

    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String save(@RequestBody DataSetDto dataSetDto) {
        DataSet oldDataSet = dataSetDto.id().map(id -> dataSetRepository.findById(fromFrontId(id))).orElse(null);

        DataSet newDataSet = fromDto(dataSetDto);
        String newDataSetId = dataSetRepository.save(newDataSet);
        newDataSet = DataSet.builder().fromDataSet(newDataSet).withId(newDataSetId).build();
        dataSetHistoryRepository.addVersion(newDataSet, oldDataSet);
        return toFrontId(newDataSetId);
    }

    @DeleteMapping(path = "/{dataSetId}")
    public void deleteById(@PathVariable String dataSetId) {
        String dataSetBackId = fromFrontId(dataSetId);
        dataSetRepository.removeById(dataSetBackId);
        dataSetHistoryRepository.removeHistory(dataSetBackId);
    }

    @GetMapping(path = "/{dataSetId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public DataSetDto findById(@PathVariable String dataSetId) {
        return toDto(
            dataSetRepository.findById(fromFrontId(dataSetId))
        );
    }

    @GetMapping(path = "/{dataSetId}/versions/last", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Integer lastVersionNumber(@PathVariable String dataSetId) {
        return dataSetHistoryRepository.lastVersion(fromFrontId(dataSetId));
    }

    @GetMapping(path = "/{dataSetId}/versions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Integer> allVersionNumbers(@PathVariable String dataSetId) {
        return dataSetHistoryRepository.allVersionNumbers(fromFrontId(dataSetId));
    }

    @GetMapping(path = {"/{dataSetId}/{version}", "/{dataSetId}/versions/{version}"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public DataSetDto version(@PathVariable String dataSetId, @PathVariable Integer version) {
        return toDto(dataSetHistoryRepository.version(fromFrontId(dataSetId), version));
    }
}
