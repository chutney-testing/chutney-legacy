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

package com.chutneytesting.dataset.api;

import static com.chutneytesting.dataset.api.DataSetMapper.fromDto;
import static com.chutneytesting.dataset.api.DataSetMapper.toDto;

import com.chutneytesting.dataset.domain.DatasetService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(DataSetController.BASE_URL)
@CrossOrigin(origins = "*")
public class DataSetController {

    public static final String BASE_URL = "/api/v1/datasets";

    private final DatasetService datasetService;

    public DataSetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @PreAuthorize("hasAuthority('DATASET_READ') or hasAuthority('SCENARIO_WRITE') or hasAuthority('CAMPAIGN_WRITE')")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DataSetDto> findAll() {
        return datasetService.findAll()
            .stream()
            .map(DataSetMapper::toDto)
            .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('DATASET_WRITE')")
    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public DataSetDto save(@RequestBody DataSetDto datasetDto) {
        hasNoDuplicatedHeaders(datasetDto);
        return toDto(datasetService.save(fromDto(datasetDto)));
    }

    @PreAuthorize("hasAuthority('DATASET_WRITE')")
    @PutMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public DataSetDto update(@RequestBody DataSetDto dataSetDto, @RequestParam Optional<String> oldId) {
        hasNoDuplicatedHeaders(dataSetDto);
        return toDto(datasetService.update(oldId, fromDto(dataSetDto)));
    }

    @PreAuthorize("hasAuthority('DATASET_WRITE')")
    @DeleteMapping(path = "/{datasetName}")
    public void deleteById(@PathVariable String datasetName) {
        datasetService.remove(datasetName);
    }

    @PreAuthorize("hasAuthority('DATASET_READ')")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DataSetDto findById(@PathVariable String id) {
        return toDto(datasetService.findById(id));
    }

    static void hasNoDuplicatedHeaders(DataSetDto dataset) {
        List<String> duplicates = dataset.duplicatedHeaders();
        if (!duplicates.isEmpty()) {
            throw new IllegalArgumentException( duplicates.size() + " column(s) have duplicated headers: [" + String.join(", ", duplicates) +"]");
        }
    }

}
