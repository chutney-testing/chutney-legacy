package com.chutneytesting.dataset.infra;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Map;

class DatasetDto {

    @JsonIgnore public final String id;
    public final String name;
    public final String description;
    public final List<String> tags;
    public final Map<String, String> constants;
    public final List<Map<String, String>> datatable;

    DatasetDto(String name, String description, List<String> tags, Map<String, String> constants, List<Map<String, String>> datatable) {
        this.id = name.replaceAll(" ", "_");
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.constants = constants;
        this.datatable = datatable;
    }

}
