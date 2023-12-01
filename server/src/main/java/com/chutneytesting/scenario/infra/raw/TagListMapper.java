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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public final class TagListMapper {
    private static final String TAGS_SEPARATOR = ",";

    public static List<String> tagsStringToList(String tags) {
        String separatorRegEx = "\\s*" + TAGS_SEPARATOR + "\\s*";
        return Stream.of(Optional.ofNullable(tags)
            .orElse("").split(separatorRegEx))
            .filter(StringUtils::isNotBlank)
            .map(String::trim)
            .collect(Collectors.toList());
    }

    public static String tagsListToString(Collection<String> tags) {
        return Optional.ofNullable(tags)
            .map(Collection::stream)
            .orElse(Stream.empty())
            .filter(StringUtils::isNotBlank)
            .map(String::trim)
            .collect(Collectors.joining(TAGS_SEPARATOR));
    }

}
