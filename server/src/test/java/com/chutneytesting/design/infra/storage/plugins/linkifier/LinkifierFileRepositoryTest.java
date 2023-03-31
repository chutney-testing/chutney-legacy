package com.chutneytesting.design.infra.storage.plugins.linkifier;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.domain.plugins.linkifier.Linkifier;
import com.chutneytesting.design.domain.plugins.linkifier.Linkifiers;
import com.chutneytesting.tools.file.FileUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LinkifierFileRepositoryTest {

    private static Linkifiers sut;
    private static Path LINKIFIER_FILE;

    @BeforeAll
    public static void setUp(@TempDir Path temporaryFolder) {
        String tmpConfDir = temporaryFolder.toFile().getAbsolutePath();

        sut = new LinkifierFileRepository(tmpConfDir);
        LINKIFIER_FILE = Paths.get(tmpConfDir + "/plugins/linkifiers.json");
    }

    @Test
    public void should_save_a_linkifier() {
        // Given
        Linkifier linkifier = new Linkifier("#pattern", "http://link/%s", "fake_id");
        String expected =
            """
                {
                  "fake_id" : {
                    "pattern" : "#pattern",
                    "link" : "http://link/%s"
                  }
                }""";

        // When
        sut.add(linkifier);

        // Then
        String actualContent = FileUtils.readContent(LINKIFIER_FILE);

        assertThat(actualContent).isEqualToIgnoringNewLines(expected);
    }

    @Test
    public void should_add_a_linkifier() {
        // Given
        FileUtils.writeContent(LINKIFIER_FILE,
            """
                {
                  "fake_id" : {
                    "pattern" : "#pattern",
                    "link" : "http://link/%s"
                  }
                }"""
        );
        Linkifier linkifier = new Linkifier("#2", "link_2", "fake_id_2");
        String expected =
            """
                {
                  "fake_id" : {
                    "pattern" : "#pattern",
                    "link" : "http://link/%s"
                  },
                  "fake_id_2" : {
                    "pattern" : "#2",
                    "link" : "link_2"
                  }
                }""";

        // When
        sut.add(linkifier);

        // Then
        String actualContent = FileUtils.readContent(LINKIFIER_FILE);

        assertThat(actualContent).isEqualToIgnoringNewLines(expected);
    }

    @Test
    public void should_remove_a_linkifier() {
        // Given
        FileUtils.writeContent(LINKIFIER_FILE,
            """
                {
                  "fake_id" : {
                    "pattern" : "#pattern",
                    "link" : "http://link/%s"
                  }
                }"""
        );

        // When
        sut.remove("fake_id");

        // Then
        String actualContent = FileUtils.readContent(LINKIFIER_FILE);
        assertThat(actualContent).isEqualToIgnoringNewLines("{ }");
    }

    @Test
    public void should_read_all_linkifiers() {
        // Given
        Linkifier linkifier_1 = new Linkifier("#pat", "http://azerty/%s", "fake_id");
        Linkifier linkifier_2 = new Linkifier("#2", "link_2", "fake_id_2");

        sut.add(linkifier_1);
        sut.add(linkifier_2);

        // When
        List<Linkifier> linkifiers = sut.getAll();

        // Then
        assertThat(linkifiers).hasSize(2);
        assertThat(linkifiers).contains(linkifier_1, linkifier_2);
    }

}
