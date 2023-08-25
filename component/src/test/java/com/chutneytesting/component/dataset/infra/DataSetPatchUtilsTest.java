package com.chutneytesting.component.dataset.infra;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.server.core.domain.dataset.DataSet;
import com.github.difflib.patch.PatchFailedException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

public class DataSetPatchUtilsTest {


    @Test
    public void should_map_dataset_as_string() throws IOException, URISyntaxException {
        // Given
        DataSet dataSet = testDataSet();

        // When
        List<String> rawValues = DataSetPatchUtils.stringLines(DataSetPatchUtils.dataSetValues(dataSet, false));
        List<String> rawDataSetText = Files.readAllLines(rawTestResources(), UTF_8);
        // Then
        assertThat(rawValues).containsExactlyElementsOf(rawDataSetText);

        // When
        List<String> prettyValues = DataSetPatchUtils.stringLines(DataSetPatchUtils.dataSetValues(dataSet, true));
        List<String> prettyDataSetText = Files.readAllLines(prettyTestResources(), UTF_8);
        // Then
        assertThat(prettyValues).containsExactlyElementsOf(prettyDataSetText);
    }

    @Test
    public void should_map_string_as_dataset_values() throws IOException, URISyntaxException {
        // Given
        DataSet dataSet = testDataSet();
        String raw = Files.readString(rawTestResources());
        String pretty = Files.readString(prettyTestResources());

        // When
        Pair<Map<String, String>, List<Map<String, String>>> rawValues = DataSetPatchUtils.extractValues(raw);
        Pair<Map<String, String>, List<Map<String, String>>> prettyValues = DataSetPatchUtils.extractValues(pretty);

        // Then
        assertThat(rawValues.getLeft()).containsExactlyEntriesOf(dataSet.constants);
        assertThat(prettyValues.getLeft()).containsExactlyEntriesOf(dataSet.constants);
        assertThat(rawValues.getRight()).containsExactlyElementsOf(dataSet.datatable);
        assertThat(prettyValues.getRight()).containsExactlyElementsOf(dataSet.datatable);
    }

    @Test
    public void should_map_string_as_dataset_values_for_unvalued_constants() { // todo - clarify behavior
        // Given
        String dataset = "key |\n";

        // When
        Pair<Map<String, String>, List<Map<String, String>>> values = DataSetPatchUtils.extractValues(dataset);

        // Then
        assertThat(values.getLeft()).containsExactly(
            entry("key", "")
        );
    }

    @Test
    public void should_create_then_apply_diff_with_dataSet_create_with_dataSetValues() throws PatchFailedException {
        // Given
        String original = "";
        String finalDataset = "a | c\n";

        // When
        String version1 = DataSetPatchUtils.dataSetValues(DataSet.builder().withConstants(Map.of("a", "b")).build(), false);
        String patch1 = DataSetPatchUtils.unifiedDiff(version1, original);
        String revised1Patched = DataSetPatchUtils.patchString(original, DataSetPatch.builder().withUnifiedDiffValues(patch1).build());

        String version2 = DataSetPatchUtils.dataSetValues(DataSet.builder().withConstants(Map.of("a", "c")).build(), false);
        String patch2 = DataSetPatchUtils.unifiedDiff(version2, revised1Patched);
        String revised2Patched = DataSetPatchUtils.patchString(revised1Patched, DataSetPatch.builder().withUnifiedDiffValues(patch2).build());

        // Then
        assertThat(revised2Patched).isEqualTo(finalDataset);
    }

    @Test
    public void should_create_then_apply_diff() throws PatchFailedException {
        // Given
        String original = "p1 | value1\n" +
            "checkcheck | v2\n" +
            "\n" +
            "| p3 | param4 |\n" +
            "| v31 | value41 |\n" +
            "| value32 | v42 |\n";

        String revised = "p1 | value1\n" +
            "param2 | v22222222222\n" +
            "\n" +
            "| p33 | param4 |\n" +
            "| ccc | value41 |\n";

        // When
        String patch = DataSetPatchUtils.unifiedDiff(revised, original);

        String originalPatched = DataSetPatchUtils.patchString(original, DataSetPatch.builder().withUnifiedDiffValues(patch).build());

        // Then
        assertThat(originalPatched).isEqualTo(revised);
    }

    private DataSet testDataSet() {
        return DataSet.builder()
            .withName("n")
            .withConstants(Map.of("p1", "value1", "param2", "v2"))
            .withDatatable(
                Arrays.asList(
                    Map.of("p3", "v31", "param4", "value41"),
                    Map.of("p3", "value32", "param4", "v42"))
            ).build();
    }

    private Path rawTestResources() throws URISyntaxException {
        return testResources("/dataset/raw.txt");
    }

    private Path prettyTestResources() throws URISyntaxException {
        return testResources("/dataset/pretty.txt");
    }

    private Path testResources(String s) throws URISyntaxException {
        return Paths.get(DataSetPatchUtilsTest.class.getResource(s).toURI());
    }
}
