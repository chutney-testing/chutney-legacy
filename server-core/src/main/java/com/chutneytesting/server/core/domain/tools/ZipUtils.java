package com.chutneytesting.server.core.domain.tools;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;

public final class ZipUtils {

    public static void compressFile(File file, String zipEntityName, ZipOutputStream out) throws IOException {
        ZipEntry entry = new ZipEntry(zipEntityName);
        out.putNextEntry(entry);

        try (FileInputStream in = new FileInputStream(file)) {
            IOUtils.copy(in, out);
        }
    }

    public static void compressDirectoryToZipfile(Path rootDir, Path sourceDir, ZipOutputStream out) throws IOException {
        Path dir = rootDir.resolve(sourceDir);
        if (Files.exists(dir)) {
            for (File file : requireNonNull(dir.toFile().listFiles())) {
                Path source = sourceDir.resolve(file.getName());
                if (file.isDirectory()) {
                    compressDirectoryToZipfile(rootDir, source, out);
                } else {
                    compressFile(rootDir.resolve(source).toFile(), source.toString(), out);
                }
            }
        } else {
            throw new FileNotFoundException(dir.toString());
        }
    }
}
