package com.chutneytesting.admin.infra.gitbackup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class ChutneyGlobalVarContentTest {

    @Test
    void should_return_global_var_content() {
        // Given
        GlobalvarRepository repositoryMock = mock(GlobalvarRepository.class);
        Set<String> fileNames = new LinkedHashSet<>(2);
        fileNames.addAll(Arrays.asList("aaa", "zzz"));
        when(repositoryMock.list()).thenReturn(fileNames);

        String aaa =
            "{\n" +
            "  aaa:\n" +
            "  {\n" +
            "    SanGoku:\n" +
            "    {\n" +
            "      login: Kakarot\n" +
            "    }\n" +
            "  }\n" +
            "}";

        String zzz =
            "{\n" +
            "  zzz:\n" +
            "  {\n" +
            "    test:\n" +
            "    {\n" +
            "      titi: toto\n" +
            "    }\n" +
            "  }\n" +
            "}";

        when(repositoryMock.getFileContent("aaa")).thenReturn(aaa);
        when(repositoryMock.getFileContent("zzz")).thenReturn(zzz);

        // When
        ChutneyGlobalVarContent sut = new ChutneyGlobalVarContent(repositoryMock);
        List<ChutneyContent> actual = sut.getContent().collect(Collectors.toList());

        // Then
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).content).isEqualToIgnoringNewLines(aaa);
        assertThat(actual.get(1).content).isEqualToIgnoringNewLines(zzz);
    }

}
