package com.chutneytesting.admin.domain;

import com.chutneytesting.server.core.admin.Backupable;
import java.nio.file.Path;

public interface HomePageRepository extends Backupable {

    HomePage load();

    HomePage load(Path filePath);

    HomePage save(HomePage homePage);

}
