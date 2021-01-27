package com.chutneytesting.design.infra.storage.scenario.git.json.versionned;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.design.infra.storage.scenario.jdbc.TestCaseData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.Reader;
import java.io.StringReader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

public class JsonMapperTest {

    private final ObjectMapper jacksonMapper = new ObjectMapper();
    @SuppressWarnings("unchecked")
    VersionnedJsonReader<String> oldmapper = Mockito.mock(VersionnedJsonReader.class);
    @SuppressWarnings("unchecked")
    CurrentJsonMapper<String> currentMapper = Mockito.mock(CurrentJsonMapper.class);
    @SuppressWarnings("unchecked")
    VersionnedJsonReader<String> noVersionMapper = Mockito.mock(VersionnedJsonReader.class);

    JsonMapper<String> mapper;

    @BeforeEach
    public void setUp() {
        Mockito.when(currentMapper.version()).thenReturn("2");
        Mockito.when(oldmapper.version()).thenReturn("1");
        Mockito.when(noVersionMapper.version()).thenReturn(null);
        mapper = new JsonMapper<>("2", String.class, jacksonMapper, oldmapper, noVersionMapper);
    }

    @Test
    public void read_with_right_mapper() {
        Mockito.when(oldmapper.readNode(new TextNode("data"))).thenReturn("result");

        String result = mapper.read(createReader("1", "data"));

        Assertions.assertThat(result).isEqualTo("result");
    }

    @Test
    public void read_with_old_mapper_with_simpleCurrent() {
        Mockito.when(oldmapper.readNode(new TextNode("data"))).thenReturn("result");

        String result = mapper.read(createReader("1", "data"));

        Assertions.assertThat(result).isEqualTo("result");
    }

    @Test
    public void read_with_current_mapper_with_simpleCurrent() {
        String result = mapper.read(createReader("2", "data"));

        Assertions.assertThat(result).isEqualTo("data");
    }

    @Test
    public void read_without_version_send_all_to_noVersion() {
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
        node.set("data", new TextNode("data"));

        Mockito.when(noVersionMapper.readNode(node)).thenReturn("result");

        String result = mapper.read(new StringReader("{\"data\":\"data\"}"));

        Assertions.assertThat(result).isEqualTo("result");
    }

    @Test
    public void read_without_data_send_all_to_noVersion() {
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
        node.set("version", new TextNode("1"));

        Mockito.when(noVersionMapper.readNode(node)).thenReturn("result");

        String result = mapper.read(new StringReader("{\"version\":\"1\"}"));
        Assertions.assertThat(result).isEqualTo("result");
    }

    @Test
    public void read_version_not_supported() {
        assertThatThrownBy(() -> mapper.read(createReader("3", "data")))
            .isInstanceOf(NoMapperForJsonVersionException.class);
    }

    @Test
    public void read_can_provide_a_content_reader() {
        String result = mapper.read(new StringReader("this is not json !"), content -> "the default result");

        Assertions.assertThat(result).isEqualTo("the default result");
    }

    @Test
    public void write_current_version() {
        String result = mapper.write("input");

        Assertions.assertThat(result).isEqualTo("{\"version\":\"2\",\"data\":\"input\"}");
    }

    @Test
    public void read_without_version_should_use_recovering_reader() {
        mapper = new JsonMapper<>("2", String.class, jacksonMapper, oldmapper);

        String result = mapper.read(new StringReader("\"data\""), e -> "error");

        Assertions.assertThat(result).isEqualTo("error");
    }

    @Test
    public void read_without_tags_should_default_to_empty_list() {
        JsonMapper<TestCaseData> mapper = new JsonMapper<>("2", TestCaseData.class, jacksonMapper);

        TestCaseData result = mapper.read(createReaderJson("2", "{\"id\": \"1\", \"title\": \"title\"}"));

        Assertions.assertThat(result.tags).isEmpty();
    }

    private Reader createReader(String version, String data) {
        return createReaderJson(version, "\""+data+"\"");
    }

    private Reader createReaderJson(String version, String json) {
        return new StringReader(String.format("{\"version\":\"%s\",\"data\":%s}", version, json));
    }
}
