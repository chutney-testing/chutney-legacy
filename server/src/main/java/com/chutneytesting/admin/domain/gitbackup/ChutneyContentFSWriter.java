package com.chutneytesting.admin.domain.gitbackup;

import com.chutneytesting.tools.file.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChutneyContentFSWriter {

    private static final Matcher ILLEGAL_CHARACTER_MATCHER = Pattern.compile("[\\s\\\\/\"<>|:*?%,;=~#&+]").matcher("");

    static long writeChutneyContent(Path workingDirectory, Set<ChutneyContentProvider> contentProviders) {
        return contentProviders.stream()
            .peek(cp -> FileUtils.initFolder(
                workingDirectory
                    .resolve(cp.category().name().toLowerCase())
                    .resolve(cp.provider())))
            .flatMap(ChutneyContentProvider::getContent)
            .peek(c -> {
                try {
                    Path path = workingDirectory
                        .resolve(c.category.name().toLowerCase())
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
