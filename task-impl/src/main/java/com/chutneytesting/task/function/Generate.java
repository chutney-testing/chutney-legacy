package com.chutneytesting.task.function;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Generate {
    private static final Random RANDOM_GENERATOR = new Random();
    private static final String DEFAULT_DIR = System.getProperty("java.io.tmpdir") + FileSystems.getDefault().getSeparator();
    private static final int ONE_HUNDRED_MEGA_BYTES = 1024 * 1024 * 100; // 100MB
    static final int DEFAULT_FILE_SIZE = 1024; // 1KB

    public String uuid() {
        return UUID.randomUUID().toString();
    }

    public String randomLong() { return String.valueOf(RANDOM_GENERATOR.nextLong()); }

    public String randomInt(int bound) { return String.valueOf(RANDOM_GENERATOR.nextInt(bound)); }

    public String id(String prefix, int length) { return id(prefix, length, ""); }

    public String id(int length, String suffix) { return id("", length, suffix); }

    public String id(String prefix, int length, String suffix) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        int random = ThreadLocalRandom.current().nextInt(0, uuid.length() - length);
        return prefix + uuid.substring(random, random+length) + suffix;
    }

    public String file() throws IOException {
        return this.file(DEFAULT_FILE_SIZE);
    }

    public String file(int fileSize) throws IOException {
        return this.file(DEFAULT_DIR + "chutney" + this.uuid(), fileSize);
    }

    public String file(String destination, int fileSize) throws IOException {
        return this.file(destination, fileSize, ONE_HUNDRED_MEGA_BYTES);
    }

    String file(String destination, int fileSize, int maxFileSize) throws IOException {
        if (fileSize > maxFileSize) {
            fileSize = maxFileSize;
        }

        byte[] randomContent = new byte[fileSize];
        RANDOM_GENERATOR.nextBytes(randomContent);

        File file = new File(destination);
        file.getParentFile().mkdirs();
        try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            out.write(randomContent);
        }
        return file.getCanonicalPath();
    }
}
