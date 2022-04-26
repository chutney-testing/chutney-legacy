package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.SCENARIO;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.scenario.domain.ScenarioNotFoundException;
import com.chutneytesting.scenario.domain.TestCaseMetadata;
import com.chutneytesting.scenario.domain.TestCaseMetadataImpl;
import com.chutneytesting.scenario.domain.TestCaseRepository;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.tools.file.FileUtils;
import com.chutneytesting.tools.orient.ComposableIdUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
            lastVersion = repository.lastVersion(testCase.id());
        }
        catch (ScenarioNotFoundException e) {
            // lastVersion = 1;
        }
        TestCaseMetadataImpl meta = TestCaseMetadataImpl.TestCaseMetadataBuilder.from(testCase.metadata).withVersion(lastVersion).build();
        return GwtTestCase.builder().from(testCase).withMetadata(meta).build();
    }
}
