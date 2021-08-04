package com.chutneytesting.admin.domain.gitbackup;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface ChutneyContentProvider {

    String provider();

    ChutneyContentCategory category();

    Stream<ChutneyContent> getContent();

    default Path providerFolder(Path workingDirectory) {
        return workingDirectory
            .resolve(category().name().toLowerCase())
            .resolve(provider());
    }

    void importDefaultFolder(Path workingDirectory);

}
