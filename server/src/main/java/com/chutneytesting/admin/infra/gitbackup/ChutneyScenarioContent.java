package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.SCENARIO;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.chutneytesting.tools.ui.ComposableIdUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ChutneyScenarioContent implements ChutneyContentProvider {

    private final ObjectMapper mapper;
    private final TestCaseRepository repository;

    public ChutneyScenarioContent(TestCaseRepository repository, @Qualifier("gitObjectMapper") ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public String provider() {
        return "gwt";
    }

    @Override
    public ChutneyContentCategory category() {
        return SCENARIO;
    }

    @Override
    public Stream<ChutneyContent> getContent() {
        return repository.findAll().stream()
            .filter(metadata -> !ComposableIdUtils.isComposableFrontId(metadata.id()))
            .map(TestCaseMetadata::id)
            .map(repository::findById)
            .map(t -> {
                ChutneyContent.ChutneyContentBuilder builder = ChutneyContent.builder()
                    .withProvider(provider())
                    .withCategory(category())
                    .withName("[" + t.id() + "]-" + t.metadata().title());
                try {
                    builder
                        .withFormat("json")
                        .withContent(mapper.writeValueAsString(t));
                } catch (JsonProcessingException e) {
                   throw new RuntimeException(e.getMessage(), e);
                }
                return builder.build();

            });
    }
}
