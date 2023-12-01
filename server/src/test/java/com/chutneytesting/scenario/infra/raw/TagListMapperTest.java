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

package com.chutneytesting.scenario.infra.raw;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TagListMapperTest {

    @Test
    public void null_string_should_produce_empty_list() throws Exception {
        List<String> actual = TagListMapper.tagsStringToList(null);
        assertThat(actual).isEmpty();
    }

    @Test
    public void null_list_should_produce_empty_string() throws Exception {
        String actual = TagListMapper.tagsListToString(null);
        assertThat(actual).isEqualTo("");
    }

    @Test
    public void string_should_be_split_on_comma_separator() throws Exception {
        List<String> actual = TagListMapper.tagsStringToList("  T1  , T2   ,  T3  ");
        assertThat(actual).containsExactly("T1", "T2", "T3");
    }

    @Test
    public void tags_should_be_joined_with_comma_separator() throws Exception {
        String actual = TagListMapper.tagsListToString(Arrays.asList("  T1", " T2 "));
        assertThat(actual).isEqualTo("T1,T2");
    }
}
