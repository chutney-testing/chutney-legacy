package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.TEST_DATA;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class ChutneyGlobalVarContent implements ChutneyContentProvider {

    private final GlobalvarRepository repository;

    public ChutneyGlobalVarContent(GlobalvarRepository repository) {
        this.repository = repository;
    }

    @Override
    public String provider() {
        return "global_var";
    }

    @Override
    public ChutneyContentCategory category() {
        return TEST_DATA;
    }

    @Override
    public Stream<ChutneyContent> getContent() {
        return repository.list().stream()
            .map(name ->
                ChutneyContent.builder()
                    .withProvider(provider())
                    .withCategory(category())
                    .withName(name)
                    .withContent(repository.getFileContent(name))
                    .build()
            );
    }

}
