package com.chutneytesting.tools;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import org.springframework.core.io.Resource;

public final class IoUtils {

    private IoUtils() {}

    public static String toString(Resource resource) {
        try(InputStreamReader isr = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8)) {
            return CharStreams.toString(isr);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean isHidden(final Path path, final Path rootPath) {
        boolean isHidden = path.toFile().isHidden() || path.toFile().getName().startsWith(".");
        Path p = path;
        while (!isHidden && p.getParent() != null && !p.getParent().equals(rootPath)) {
            isHidden = p.getParent().toFile().isHidden() || p.getParent().toFile().getName().startsWith(".");
            p = p.getParent();
        }
        return isHidden;
    }

    public static boolean isHidden(Path path) {
        return isHidden(path, path.getRoot());
    }

}
