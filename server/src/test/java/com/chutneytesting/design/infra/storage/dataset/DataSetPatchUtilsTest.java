package com.chutneytesting.design.infra.storage.dataset;

import static com.chutneytesting.design.infra.storage.dataset.DataSetPatchUtils.dataSetValues;
import static com.chutneytesting.design.infra.storage.dataset.DataSetPatchUtils.extractValues;
import static com.chutneytesting.design.infra.storage.dataset.DataSetPatchUtils.patchString;
import static com.chutneytesting.design.infra.storage.dataset.DataSetPatchUtils.stringLines;
import static com.chutneytesting.design.infra.storage.dataset.DataSetPatchUtils.unifiedDiff;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetMetaData;
import com.github.difflib.patch.PatchFailedException;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.groovy.util.Maps;
import org.junit.Test;

public class DataSetPatchUtilsTest {

    private Charset UTF8 = Charset.forName("UTF-8");

    @Test
    public void should_map_dataset_as_string() throws IOException, URISyntaxException {
        // Given
        DataSet dataSet = testDataSet();

        // When
        List<String> rawValues = stringLines(dataSetValues(dataSet, false));
        List<String> rawDataSetText = Files.readAllLines(rawTestResources(), UTF8);
        // Then
        assertThat(rawValues).containsExactlyElementsOf(rawDataSetText);

        // When
        List<String> prettyValues = stringLines(dataSetValues(dataSet, true));
        List<String> prettyDataSetText = Files.readAllLines(prettyTestResources(), UTF8);
        // Then
        assertThat(prettyValues).containsExactlyElementsOf(prettyDataSetText);
    }

    @Test
    public void should_map_string_as_dataset_values() throws IOException, URISyntaxException {
        // Given
        DataSet dataSet = testDataSet();
        String raw = new String(Files.readAllBytes(rawTestResources()), UTF8);
        String pretty = new String(Files.readAllBytes(prettyTestResources()), UTF8);

        // When
        Pair<Map<String, String>, List<Map<String, String>>> rawValues = extractValues(raw);
        Pair<Map<String, String>, List<Map<String, String>>> prettyValues = extractValues(pretty);

        // Then
        assertThat(rawValues.getLeft()).containsExactlyEntriesOf(dataSet.uniqueValues);
        assertThat(prettyValues.getLeft()).containsExactlyEntriesOf(dataSet.uniqueValues);
        assertThat(rawValues.getRight()).containsExactlyElementsOf(dataSet.multipleValues);
        assertThat(prettyValues.getRight()).containsExactlyElementsOf(dataSet.multipleValues);
    }

    @Test
    public void should_create_then_apply_diff() throws PatchFailedException {
        // Given
        String original = "p1 | value1\n" +
            "param2 | v2\n" +
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
        String patch = unifiedDiff(revised, original);
        String originalPatched = patchString(original, DataSetPatch.builder().withUnifiedDiffValues(patch).build());

        // Then
        assertThat(originalPatched).isEqualTo(revised);
    }

    private DataSet testDataSet() {
        return DataSet.builder()
            .withMetaData(DataSetMetaData.builder().withName("n").build())
            .withUniqueValues(Maps.of("p1", "value1", "param2", "v2"))
            .withMultipleValues(
                Arrays.asList(
                    Maps.of("p3", "v31", "param4", "value41"),
                    Maps.of("p3", "value32", "param4", "v42"))
            ).build();
    }

    private Path rawTestResources() throws URISyntaxException {
        return testResources("dataset/raw.txt");
    }

    private Path prettyTestResources() throws URISyntaxException {
        return testResources("dataset/pretty.txt");
    }

    private Path testResources(String s) throws URISyntaxException {
        return Paths.get(Resources.getResource(s).toURI());
    }
}
