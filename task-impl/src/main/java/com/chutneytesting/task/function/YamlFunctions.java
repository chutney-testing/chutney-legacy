package com.chutneytesting.task.function;

import static java.util.Objects.requireNonNull;

import com.chutneytesting.task.spi.SpelFunction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.jayway.jsonpath.JsonPath;

public class YamlFunctions {

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
    static final ObjectMapper jsonMapper = new ObjectMapper();

    @SpelFunction
    public static Object yamlPath(String document, String yamlPath) throws JsonProcessingException {
        Object yamlAsObject = yamlMapper.readValue(document, Object.class);
        String json = jsonMapper.writeValueAsString(yamlAsObject);
        return JsonPath.parse(json).read(yamlPath);
    }

    @SpelFunction
    public static String toYaml(Object obj) throws JsonProcessingException {
        return yamlMapper.writeValueAsString(requireNonNull(obj));
    }
}
