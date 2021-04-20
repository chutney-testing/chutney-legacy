package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.CONF;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.design.domain.plugins.linkifier.Linkifiers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ChutneyLinkifierPluginContent implements ChutneyContentProvider {

    private final Linkifiers linkifiers;
    private final ObjectMapper mapper;

    public ChutneyLinkifierPluginContent(Linkifiers linkifiers, @Qualifier("gitObjectMapper") ObjectMapper mapper) {
        this.linkifiers = linkifiers;
        this.mapper = mapper;
    }

    @Override
    public String provider() {
        return "linkifiers";
    }

    @Override
    public ChutneyContentCategory category() {
        return CONF;
    }

    @Override
    public Stream<ChutneyContent> getContent() {
        try {
            return Stream.of(
                ChutneyContent.builder()
                    .withProvider(provider())
                    .withCategory(category())
                    .withName("linkifiers")
                    .withFormat("json")
                    .withContent(mapper.writeValueAsString(linkifiers.getAll()))
                    .build()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
