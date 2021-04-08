package com.chutneytesting.admin.domain.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.CAMPAIGN;
import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.CONF;
import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.SCENARIO;
import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.TEST_DATA;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;

import com.chutneytesting.tools.file.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChutneyContentFSWriter {

    private static final Matcher ILLEGAL_CHARACTER_MATCHER = Pattern.compile("[\\s\\\\/\"<>|:*?%,;=~#&+]").matcher("");
    private static final Map<ChutneyContentCategory, Path> folderStructure = Map.of(
        SCENARIO, Path.of("scenarios"),
        CAMPAIGN, Path.of("campaigns"),
        TEST_DATA, Path.of("test_data"),
        CONF, Path.of("conf")
    );

    static void cleanWorkingFolder(Path workingDirectory) {
        stream(ofNullable(workingDirectory.toFile().listFiles()).orElse(new File[0]))
            .forEach(file -> {
                if (!".git".equals(file.getName())) {
                    if (file.isDirectory()) {
                        cleanWorkingFolder(file.toPath());
                    }
                    file.delete();
                }
            });
    }

    public static long writeChutneyContent(Path workingDirectory, Set<ChutneyContentProvider> contentProviders) {
        return contentProviders.stream()
            .peek(cp -> FileUtils.initFolder(
                workingDirectory
                    .resolve(folderStructure.get(cp.category()))
                    .resolve(cp.provider())))
            .flatMap(ChutneyContentProvider::getContent)
            .peek(c -> {
                try {
                    Path path = workingDirectory
                        .resolve(folderStructure.get(c.category))
                        .resolve(c.provider)
                        .resolve(safeFileName(c.name, c.format));
                    Files.write(path, c.content.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }).count();
    }

    static String safeFileName(String filename, String format) {
        String safeName = ILLEGAL_CHARACTER_MATCHER.reset(filename).replaceAll("_");
        safeName = truncate(safeName, format);
        return (safeName + "." + format).toLowerCase();
    }

    private static String truncate(String safeName, String format) {
        if (safeName.length() + format.length() + 1 > 255) {
            return safeName.substring(0, 255 - (format.length() + 1));
        }

        return safeName;
    }

}
