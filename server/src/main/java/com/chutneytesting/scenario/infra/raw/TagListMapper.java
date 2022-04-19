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
