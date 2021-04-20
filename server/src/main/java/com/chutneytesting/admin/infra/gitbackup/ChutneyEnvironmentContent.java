package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.CONF;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.environment.api.EnvironmentApi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ChutneyEnvironmentContent implements ChutneyContentProvider {

    private final EnvironmentApi environmentApi;
    private final ObjectMapper mapper;

    public ChutneyEnvironmentContent(@Qualifier("environmentEmbeddedApplication") EnvironmentApi environmentApi, @Qualifier("gitObjectMapper") ObjectMapper mapper) {
        this.environmentApi = environmentApi;
        this.mapper = mapper;
    }

    @Override
    public String provider() {
        return "env";
    }

    @Override
    public ChutneyContentCategory category() {
        return CONF;
    }

    @Override
    public Stream<ChutneyContent> getContent() {
        return environmentApi.listEnvironments().stream()
            .map(env -> {
                ChutneyContent.ChutneyContentBuilder builder = ChutneyContent.builder()
                    .withProvider(provider())
                    .withCategory(category())
                    .withName(env.name);
                try {
                    builder
                        .withContent(mapper.writeValueAsString(env))
                        .withFormat("json");
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                return builder.build();
            });
    }
}
