/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
