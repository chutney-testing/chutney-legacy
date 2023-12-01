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

package com.chutneytesting.action.function;

import static com.chutneytesting.action.function.Generate.DEFAULT_FILE_SIZE;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class GenerateFunctionTest {

    @TempDir
    static Path temporaryFolder;

    @Test
    public void generate_function_produce_a_generate_object() {
        Generate generate = GenerateFunction.generate();
        assertThat(generate).isNotNull();
    }

    @Test
    public void generate_standard_uuid() {
        assertThat(new Generate().uuid()).matches("\\p{XDigit}{8}(?:-\\p{XDigit}{4}){3}-\\p{XDigit}{12}");
    }

    @Test
    public void generate_long() {
        assertThat(new Generate().randomLong()).matches("[-]?\\p{XDigit}+");
    }

    @Test
    void generate_id_with_prefix() {
        assertThat(new Generate().id("prefix-", 1)).matches("^prefix-\\w$");
    }

    @Test
    void generate_id_with_suffix() {
        assertThat(new Generate().id(1, "-suffix")).matches("^\\w-suffix$");
    }

    @Test
    void generate_id_with_prefix_suffix_and_given_length() {
        assertThat(new Generate().id("pre-", 5, "-suf")).matches("^pre-\\w{5}-suf$");
    }

    @Test
    void should_generate_file_with_random_content() throws IOException {
        // When
        String result = new Generate().file();

        // Then
        assertGeneratedFile(result, Paths.get(result), DEFAULT_FILE_SIZE);
    }

    @Test
    void should_generate_file_at_given_path_with_random_content() throws IOException {
        // Given
        String destination = temporaryFolder.resolve("generated_file.txt").toString();
        int givenFileSize = 1024*10;

        Path expectedFile = temporaryFolder.resolve(destination);

        // When
        String result = new Generate().file(destination, givenFileSize);

        // Then
        assertGeneratedFile(result, expectedFile, givenFileSize);
    }

    @Test
    void should_generate_file_not_exceeding_maximum_file_size() throws IOException {
        // Given
        String destination = temporaryFolder.resolve("generated_file.txt").toString();
        int oneMegaBytes = 1024*1024; // 1MB
        int maxFileSize = 1024*10; // 10KB

        Path expectedFile = temporaryFolder.resolve(destination);

        // When
        String result = new Generate().file(destination, oneMegaBytes, maxFileSize);

        // Then
        assertGeneratedFile(result, expectedFile, maxFileSize);
    }

    private void assertGeneratedFile(String result, Path expectedFile, int expectedFileSize) throws IOException {
        assertThat(result).isNotBlank();
        assertThat(Files.exists(expectedFile)).isTrue();
        assertThat(Files.size(expectedFile)).isEqualTo(expectedFileSize);
    }

}
