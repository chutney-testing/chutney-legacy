package com.chutneytesting.admin.domain.gitbackup;

import java.nio.file.Path;
import java.util.Set;

public class ChutneyContentFSReader {

    static void readChutneyContent(Path workingDirectory, Set<ChutneyContentProvider> contentProviders) {
        contentProviders.forEach(cp -> cp.importDefaultFolder(workingDirectory));
    }

}
