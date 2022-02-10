package com.chutneytesting.task.function;

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
    void should_generate_file_at_given_path_with_random_content() throws IOException {
        // Given
        String destination = temporaryFolder.resolve("generated_file.txt").toString();
        int fileSize = 1024;

        Path expectedFile = temporaryFolder.resolve(destination);

        // When
        String result = new Generate().file(destination, fileSize);

        // Then
        assertThat(result).isEqualTo(expectedFile.toString());
        assertThat(Files.exists(expectedFile)).isTrue();
        assertThat(Files.size(expectedFile)).isEqualTo(fileSize);
    }

    @Test
    void should_generate_file_with_random_content() throws IOException {
        // When
        String result = new Generate().file();

        // Then
        assertThat(result).isNotBlank();
        assertThat(Files.exists(Paths.get(result))).isTrue();
        assertThat(Files.size(Paths.get(result))).isEqualTo(1024);
    }

}
