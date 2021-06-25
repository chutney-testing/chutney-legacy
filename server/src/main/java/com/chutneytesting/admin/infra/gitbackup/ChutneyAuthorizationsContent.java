package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.CONF;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.security.api.AuthorizationMapper;
import com.chutneytesting.security.infra.JsonFileAuthorizations;
import com.chutneytesting.tools.Try;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ChutneyAuthorizationsContent implements ChutneyContentProvider {

    private final JsonFileAuthorizations jsonFileAuthorizations;
    private final ObjectMapper mapper;

    public ChutneyAuthorizationsContent(
        JsonFileAuthorizations jsonFileAuthorizations,
        @Qualifier("gitObjectMapper") ObjectMapper mapper
    ) {
        this.jsonFileAuthorizations = jsonFileAuthorizations;
        this.mapper = mapper;
    }

    @Override
    public String provider() {
        return "authorizations";
    }

    @Override
    public ChutneyContentCategory category() {
        return CONF;
    }

    @Override
    public Stream<ChutneyContent> getContent() {
        return Try.unsafe(() -> {
            String content = mapper.writeValueAsString(
                AuthorizationMapper.toDto(jsonFileAuthorizations.read())
            );
            return Stream.of(
                ChutneyContent.builder()
                    .withName(provider())
                    .withProvider(provider())
                    .withCategory(category())
//                  .withFormat("json")
                    .withContent(content)
                    .build()
            );
        });
    }
}
