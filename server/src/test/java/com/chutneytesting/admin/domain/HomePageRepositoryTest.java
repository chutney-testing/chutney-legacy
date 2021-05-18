package com.chutneytesting.admin.domain;



import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.admin.infra.storage.JsonHomePageRepository;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class HomePageRepositoryTest {


    private static final String HOME_PAGE_NAME = "home-page.json";

    @Test
    public void create_and_get_home_page() throws IOException {
        // G
        Path homePagePath = Paths.get(org.assertj.core.util.Files.temporaryFolderPath(), HOME_PAGE_NAME);
        homePagePath.toFile().createNewFile();
        JsonHomePageRepository sut = new JsonHomePageRepository(homePagePath.getParent().toString());

        // W
        HomePage homePage = new HomePage("content of the home page");
        sut.save(homePage);
        HomePage savedHomePage = sut.load();

        // T
        assertThat(savedHomePage.content).isEqualTo("content of the home page");
    }
}
