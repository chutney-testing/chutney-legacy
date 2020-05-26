package com.chutneytesting.design.infra.storage.environment;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class EnvironmentRepositoryTest {

    @Test
    public void jackson_deserialize_of_environment_ok() throws IOException {

        // GIVEN
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new GuavaModule());

        final String TARGET_NAME = "MGN";
        final String TARGET_URL = "http://localhost:9060";
        final String target = "{\"name\":\"" + TARGET_NAME + "\",\"url\":\"" + TARGET_URL + "\",\"properties\":{},\"security\":{}}";

        final String ENV_NAME = "GLOBAL";
        final String ENV_DESCRIPTION = "Environnement global";
        final String env = "{\"name\":\"" + ENV_NAME + "\",\"description\":\"" + ENV_DESCRIPTION + "\",\"targets\":[{\"name\":\""+ TARGET_NAME +"\",\"url\":\""+ TARGET_URL +"\",\"properties\":{},\"security\":{}}]}";

        // WHEN
        mapper.readValue(target, JsonTarget.class);
        JsonEnvironment environmentDto = mapper.readValue(env, JsonEnvironment.class);

        // THEN
        assertThat(environmentDto.name).isEqualTo(ENV_NAME);
        assertThat(environmentDto.description).isEqualTo(ENV_DESCRIPTION);
        assertThat(environmentDto.targets.get(0).name).isEqualTo(TARGET_NAME);
        assertThat(environmentDto.targets.get(0).url).isEqualTo(TARGET_URL);
        assertThat(environmentDto.targets.get(0).properties).isEmpty();
        assertThat(environmentDto.targets.get(0).security.credential).isNull();
    }
}
