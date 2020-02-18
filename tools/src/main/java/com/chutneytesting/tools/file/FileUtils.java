package com.chutneytesting.tools.file;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

public class FileUtils {

    private FileUtils(){}

    public static void initFolder(Path storeFolderPath) throws UncheckedIOException {
        try {
            Files.createDirectories(storeFolderPath);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create configuration directory: " + storeFolderPath);
        }
        Path testPath = storeFolderPath.resolve("test");
        if (!Files.exists(testPath)) {
            try {
                Files.createFile(storeFolderPath.resolve("test"));
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to write in configuration directory: " + storeFolderPath, e);
            }
        }
        try {
            Files.delete(testPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to delete in configuration directory: " + storeFolderPath, e);
        }
    }

    public static String getNameWithoutExtension(Path path) {
        String fileName = path.getFileName().toString();
        if (fileName.indexOf(".") > 0) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }

    public static <R> R doOnListFiles(Path path, Function<Stream<Path>, R> exec) throws UncheckedIOException {
        try (Stream<Path> stream = Files.list(path)) {
            return exec.apply(stream);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to do something on files of path: " + path, e);
        }
    }

    public static String readAllBytes(Path path) throws UncheckedIOException {
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read variable file: " + path, e);
        }
    }
}
