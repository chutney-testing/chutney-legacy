package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.SCENARIO;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.server.core.scenario.AggregatedRepository;
import com.chutneytesting.server.core.scenario.ScenarioNotFoundException;
import com.chutneytesting.server.core.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.scenario.TestCaseMetadataImpl;
import com.chutneytesting.tools.file.FileUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ChutneyScenarioContent implements ChutneyContentProvider {

    private final ObjectMapper mapper;
    private final AggregatedRepository<GwtTestCase> repository;

    public ChutneyScenarioContent(AggregatedRepository<GwtTestCase> repository, @Qualifier("gitObjectMapper") ObjectMapper mapper) {
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
            .map(TestCaseMetadata::id)
            .map(repository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
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

    static boolean isComposableFrontId(String frontId) {
        return frontId.contains("-");
    }

    @Override
    public void importDefaultFolder(Path workingDirectory) {
        importFolder(providerFolder(workingDirectory));
    }

    public void importFolder(Path folderPath) {
        List<Path> scenarios = FileUtils.listFiles(folderPath);
        scenarios.forEach(this::importFile);
    }

    public void importFile(Path filePath) {
        if (Files.exists(filePath)) {
            try {
                byte[] bytes = Files.readAllBytes(filePath);
                try {
                    GwtTestCase testCase = mapper.readValue(bytes, GwtTestCase.class);
                    GwtTestCase tc = manageVersionConsistency(testCase);
                    repository.save(tc);
                } catch (IOException e) {
                    throw new UnsupportedOperationException("Cannot deserialize scenario file : " + filePath, e);
                }
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot read scenario file : " + filePath, e);
            }
        }
    }

    private GwtTestCase manageVersionConsistency(GwtTestCase testCase) {
        Integer lastVersion = 1;
        try {
            lastVersion = repository.lastVersion(testCase.id()).get();// TODO ugly
        }
        catch (ScenarioNotFoundException e) {
            // lastVersion = 1;
        }
        TestCaseMetadataImpl meta = TestCaseMetadataImpl.TestCaseMetadataBuilder.from(testCase.metadata).withVersion(lastVersion).build();
        return GwtTestCase.builder().from(testCase).withMetadata(meta).build();
    }
}
