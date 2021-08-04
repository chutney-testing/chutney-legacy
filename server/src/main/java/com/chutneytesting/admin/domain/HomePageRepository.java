package com.chutneytesting.admin.domain;

import java.nio.file.Path;

public interface HomePageRepository extends Backupable {

    HomePage load();

    HomePage load(Path filePath);

    HomePage save(HomePage homePage);

}
