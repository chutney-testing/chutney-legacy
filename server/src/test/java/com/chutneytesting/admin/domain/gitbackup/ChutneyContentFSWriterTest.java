package com.chutneytesting.admin.domain.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.CONF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.admin.infra.gitbackup.ChutneyGlobalVarContent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ChutneyContentFSWriterTest {

    private final ChutneyContentProvider providerMock = mock(ChutneyGlobalVarContent.class);
    private final Set<ChutneyContentProvider> contentProviders = Set.of(providerMock);

    @TempDir
    static Path temporaryFolder;

    @Test
    void should_write_file_in_category_then_provider_name_folder() throws IOException {
        // Given
        ChutneyContent content = ChutneyContent.builder()
            .withName("fake")
            .withFormat("test")
            .withContent("content")
            .withCategory(CONF)
            .withProvider("mock")
            .build();
        when(providerMock.getContent()).thenReturn(Stream.of(content));
        when(providerMock.category()).thenReturn(CONF);
        when(providerMock.provider()).thenReturn("mock");

        // When
        long count = ChutneyContentFSWriter.writeChutneyContent(temporaryFolder, contentProviders);

        // Then
        assertThat(count).isEqualTo(1L);
        Path actualFile = temporaryFolder
            .resolve("conf")
            .resolve(content.provider)
            .resolve(content.name + "." + content.format);

        assertThat(actualFile.toFile().exists()).isTrue();
        assertThat(new String(Files.readAllBytes(actualFile))).isEqualTo(content.content);
    }

    @Test
    void should_replace_illegal_filename_characters_by_underscores() {
        // Given
        String illegals = "\"\\/<>|:*?%,;=~#&+ "; // [", \, /, <, >, |, :, *, ?, %, ,, ;, =, ~, #, &, +, ]

        // When
        String actual = ChutneyContentFSWriter.safeFileName(illegals, "safer");

        // Then
        assertThat(actual).isEqualTo("__________________.safer");
    }

    @Test
    void should_keep_filenames_under_255_characters() {
        // Given
        String tooLong = RandomStringUtils.randomAlphabetic(300);

        // When
        String actual = ChutneyContentFSWriter.safeFileName(tooLong, "truncated");

        // Then
        assertThat(actual.length()).isEqualTo(255);
    }

    @Test
    void should_lowercase_filenames() {
        // Given
        String uppercased = "ABCDEFG";

        // When
        String actual = ChutneyContentFSWriter.safeFileName(uppercased, "lower");

        // Then
        assertThat(actual).isEqualTo(uppercased.toLowerCase() + ".lower");
    }

}
