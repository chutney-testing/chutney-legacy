package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.CONF;

import com.chutneytesting.admin.domain.HomePage;
import com.chutneytesting.admin.domain.HomePageRepository;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class ChutneyHomepageContent implements ChutneyContentProvider {

    private final HomePageRepository repository;

    public ChutneyHomepageContent(HomePageRepository repository) {
        this.repository = repository;
    }

    @Override
    public String provider() {
        return "homepage";
    }

    @Override
    public ChutneyContentCategory category() {
        return CONF;
    }

    @Override
    public Stream<ChutneyContent> getContent() {

        return Stream.of(ChutneyContent.builder()
            .withProvider(provider())
            .withCategory(category())
            .withName("homepage")
            .withFormat("adoc")
            .withContent(repository.load().content)
            .build()
        );

    }

    @Override
    public void importDefaultFolder(Path workingDirectory) {
        Path filePath = providerFolder(workingDirectory).resolve(provider() + ".adoc");
        importFile(filePath);
    }

    public void importFile(Path filePath) {
        if (Files.exists(filePath)) {
            try {
                String content = new String(Files.readAllBytes(filePath));
                repository.save(new HomePage(content));
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot read global var file : " + filePath, e);
            }
        }
    }
}
