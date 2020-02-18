package com.chutneytesting.design.infra.storage.scenario.git.json.versionned;

import static com.chutneytesting.tools.Try.exec;
import static com.chutneytesting.tools.Try.unsafe;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.chutneytesting.tools.Try;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JsonMapper<T> {

    private final CurrentJsonMapper<T> currentMapper;
    private final Map<String, VersionnedJsonReader<T>> readers;
    private final ObjectMapper jacksonMapper;

    @SafeVarargs
    public JsonMapper(String version, Class<T> type, ObjectMapper jacksonMapper, VersionnedJsonReader<T>... readers) {
        this(new SimpleCurrentJsonMapper<>(type, version, jacksonMapper), jacksonMapper, readers);
    }

    @SafeVarargs
    public JsonMapper(CurrentJsonMapper<T> currentMapper, ObjectMapper jacksonMapper, VersionnedJsonReader<T>... readers) {
        this.currentMapper = currentMapper;
        this.jacksonMapper = jacksonMapper;
        List<VersionnedJsonReader<T>> readersArray = new ArrayList<>(Arrays.asList(readers));
        readersArray.add(currentMapper);
        this.readers = readersArray.stream()
            .collect(Collectors.toMap(VersionnedJsonReader::version, Function.identity()));
    }

    public T read(Reader reader) {
        return read(reader, null);
    }

    @SuppressWarnings("resource")
    public T read(Reader reader, Function<String, T> recoveringReader) {
        try (Scanner sc = new Scanner(reader)) {
            String content = sc.useDelimiter("\\Z").next();

            Try<T> parseExec = exec(() -> parseJson(content));
            if (recoveringReader != null)
                parseExec = parseExec.tryToRecover(Exception.class, e -> recoveringReader.apply(content));
            return parseExec.runtime();
        }
    }

    private T parseJson(String content) throws IOException {
        JsonNode jsonNode = jacksonMapper.readTree(content);
        VersionnedJson versionnedJson = VersionnedJson.of(jsonNode);
        return findJsonMapper(versionnedJson.version)
            .orElseThrow(() -> new NoMapperForJsonVersionException(versionnedJson.version))
            .readNode(versionnedJson.data);
    }

    private Optional<VersionnedJsonReader<T>> findJsonMapper(String version) {
        return ofNullable(readers.get(version));
    }

    public String write(T toWrite) {
        return unsafe(() ->
            jacksonMapper.writeValueAsString(
                new VersionnedJson(currentMapper.version(), currentMapper.toNode(toWrite)).toNode()));
    }


}
