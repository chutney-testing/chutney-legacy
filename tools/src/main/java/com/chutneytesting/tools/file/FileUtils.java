package com.chutneytesting.tools.file;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    private FileUtils() {}

    public static void initFolder(Path folderPath) throws UncheckedIOException {
        try {
            Files.createDirectories(folderPath);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create directory: " + folderPath);
        }
        Path testPath = folderPath.resolve("test");
        if (!Files.exists(testPath)) {
            try {
                Files.createFile(folderPath.resolve("test"));
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to write in directory: " + folderPath, e);
            }
        }

        deleteFolder(testPath);
    }

    public static void deleteFolder(Path folderPath) {
        try {
            Files.delete(folderPath);
        } catch (NoSuchFileException e) {
            // do nothing
        } catch (DirectoryNotEmptyException e) {
            cleanFolder(folderPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to delete directory: " + folderPath, e);
        }
    }

    public static void cleanFolder(Path folderPath) {
        stream(ofNullable(folderPath.toFile().listFiles()).orElse(new File[0]))
            .forEach(file -> {
                if (file.isDirectory()) {
                    cleanFolder(file.toPath());
                }
                file.delete();
            });
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

    public static List<Path> listFiles(Path folder) {
        return FileUtils.doOnListFiles(folder, (pathStream) ->
            pathStream
                .filter(Files::isRegularFile)
                .collect(Collectors.toList())
        );
    }

    public static String readContent(Path path) throws UncheckedIOException {
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read variable file: " + path, e);
        }
    }

    public static void writeContent(Path path, String content) throws UncheckedIOException {
        try (FileOutputStream outputStream = new FileOutputStream(path.toString())) {
            byte[] strToBytes = content.getBytes();
            outputStream.write(strToBytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write into file: " + path, e);
        }
    }
}
