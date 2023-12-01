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
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.server.core.domain.tools.ui.ImmutableKeyValue;
import com.chutneytesting.server.core.domain.tools.ui.KeyValue;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class DataSetMapperTest {

    // TODO issue https://github.com/chutney-testing/chutney/issues/532 for more details
    @Test
    @Disabled
    public void should_keep_column_order() {
        KeyValue[] constants = {keyOf("key1", "v1"), keyOf("key2", "v2"), keyOf("key3", "v3"), keyOf("key4", "v4")};
        List<KeyValue> datatable = List.of(keyOf("col1", "v1"), keyOf("col2", "v2"), keyOf("col3", "v3"), keyOf("col4", "v4"));
        DataSetDto dataSetDto = ImmutableDataSetDto.builder()
            .name("name")
            .addConstants(constants)
            .addDatatable(datatable)
            .build();

        DataSetDto dataset = toDto(fromDto(dataSetDto));

        assertThat(dataset.constants()).isEqualTo(Arrays.asList(constants));
        assertThat(dataset.datatable()).isEqualTo(datatable);
    }

    private KeyValue keyOf(String key, String value) {
        return ImmutableKeyValue.builder().key(key).value(value).build();
    }

}
