package com.chutneytesting.admin.domain;

public interface HomePageRepository extends Backupable {

    HomePage load();
    HomePage save(HomePage homePage);

}
