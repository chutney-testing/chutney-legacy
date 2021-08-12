package com.chutneytesting.admin.infra.gitbackup;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.design.domain.campaign.CampaignRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ChutneyCampaignContent implements ChutneyContentProvider {

    private final CampaignRepository repository;
    private final ObjectMapper mapper;

    public ChutneyCampaignContent(CampaignRepository repository, @Qualifier("gitObjectMapper") ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public String provider() {
        return "campaign";
    }

    @Override
    public ChutneyContentCategory category() {
        return ChutneyContentCategory.CAMPAIGN;
    }

    @Override
    public Stream<ChutneyContent> getContent() {
        return repository.findAll().stream()
            .map(c -> {
            ChutneyContent.ChutneyContentBuilder builder = ChutneyContent.builder()
                .withProvider(provider())
                .withCategory(category())
                .withName("[" + c.id + "]-" + c.title);
            try {
                builder
                    .withContent(mapper.writeValueAsString(c))
                    .withFormat("json");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            return builder.build();
        });
    }

}
