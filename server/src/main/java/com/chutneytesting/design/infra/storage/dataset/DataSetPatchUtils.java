package com.chutneytesting.design.infra.storage.dataset;

import static java.util.Optional.empty;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.tools.Try;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.MyersDiff;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;

public final class DataSetPatchUtils {

    private static final String SPACE = " ";
    private static final String SEPARATOR = "|";
    private static final String NEWLINE = "\n";
    private static final String SEPARATOR_SPACED = SPACE + SEPARATOR + SPACE;
    private static final String SEPARATOR_REGEX = "\\" + SEPARATOR;

    private DataSetPatchUtils() {
    }

    public static Pair<Map<String, String>, List<Map<String, String>>> extractValues(String s) {
        Map<String, String> uniqueValues = new LinkedHashMap<>();
        List<Map<String, String>> multipleValues = new ArrayList<>();
        List<String> multipleValuesHeaders = new ArrayList<>();

        List<String> lines = stringLines(s);
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                if (line.startsWith(SEPARATOR)) { // Multiple values
                    if (multipleValuesHeaders.isEmpty()) { // headers
                        String[] headers = line.split(SEPARATOR_REGEX);
                        for (int i = 1; i < headers.length; i++) {
                            multipleValuesHeaders.add(headers[i].trim());
                        }
                    } else { // values
                        String[] values = line.split(SEPARATOR_REGEX);
                        Map<String, String> valuesMap = new LinkedHashMap<>();
                        for (int i = 0; i < multipleValuesHeaders.size(); i++) {
                            valuesMap.put(multipleValuesHeaders.get(i), values[i + 1].trim());
                        }
                        multipleValues.add(valuesMap);
                    }
                } else { // Unique values
                    String[] uniqueValue = line.split(SEPARATOR_REGEX);
                    if (uniqueValue.length == 2) {
                        uniqueValues.put(uniqueValue[0].trim(), uniqueValue[1].trim());
                    }
                }
            }
        }
        return Pair.of(uniqueValues, multipleValues);
    }

    public static String dataSetValues(DataSet dataSet, boolean pretty) {
        Map<String, String> uniqueValues = dataSet.uniqueValues;
        List<Map<String, String>> multipleValues = dataSet.multipleValues;

        StringBuilder values = new StringBuilder();
        StringBuilder line = new StringBuilder();

        if (!uniqueValues.isEmpty()) {
            if (pretty) {
                int maxKeyLength = uniqueValues.keySet().stream().mapToInt(String::length).max().getAsInt();
                uniqueValues.forEach((k, v) -> {
                    line.append(k);
                    for (int i = k.length(); i < maxKeyLength; i++) {
                        line.append(SPACE);
                    }
                    line.append(SEPARATOR_SPACED).append(v);
                    values.append(line.toString().trim()).append(NEWLINE);
                    line.setLength(0);
                });
                values.append(NEWLINE);
            } else {
                uniqueValues.forEach((k, v) -> {
                    line.append(k).append(SEPARATOR_SPACED).append(v);
                    values.append(line.toString().trim()).append(NEWLINE);
                    line.setLength(0);
                });
                values.append(NEWLINE);
            }
        }

        if (!multipleValues.isEmpty()) {
            if (pretty) {
                List<Pair<String, Integer>> headers = new ArrayList<>();
                multipleValues.get(0).keySet().forEach(m ->
                    headers.add(Pair.of(m, multipleValues.stream().mapToInt(mm -> mm.get(m).length()).max().getAsInt()))
                );
                headers.forEach(h -> {
                    String header = h.getLeft();
                    line.append(SEPARATOR_SPACED).append(header);
                    for (int i = header.length(); i < h.getRight(); i++) {
                        line.append(SPACE);
                    }
                });
                line.append(SEPARATOR_SPACED);
                values.append(line.toString().trim()).append(NEWLINE);
                line.setLength(0);
                multipleValues.forEach(m -> {
                    headers.forEach(h -> {
                        String value = m.get(h.getLeft());
                        line.append(SEPARATOR_SPACED).append(value);
                        for (int j = value.length(); j < h.getRight(); j++) {
                            line.append(SPACE);
                        }
                    });
                    line.append(SEPARATOR_SPACED);
                    values.append(line.toString().trim()).append(NEWLINE);
                    line.setLength(0);
                });
            } else {
                List<String> headers = new ArrayList<>(multipleValues.get(0).keySet());
                headers.forEach(k -> line.append(SEPARATOR_SPACED).append(k));
                line.append(SEPARATOR_SPACED);
                values.append(line.toString().trim()).append(NEWLINE);
                line.setLength(0);
                multipleValues.forEach(m -> {
                    headers.forEach(k -> line.append(SEPARATOR_SPACED).append(m.get(k)));
                    line.append(SEPARATOR_SPACED);
                    values.append(line.toString().trim()).append(NEWLINE);
                    line.setLength(0);
                });
            }
        }

        return values.toString();
    }

    public static String unifiedDiff(String revised, String original) {
        RawText originalRaw = new RawText(original.getBytes());
        RawText revisedRaw = new RawText(revised.getBytes());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EditList edits = new EditList();
        edits.addAll(MyersDiff.INSTANCE.diff(RawTextComparator.DEFAULT, originalRaw, revisedRaw));
        Try.unsafe(() -> {
            new DiffFormatter(out).format(edits, originalRaw, revisedRaw);
            return empty();
        });

        return out.toString();
    }

    public static String patchString(String original, DataSetPatch dataSetPatch) throws PatchFailedException {
        Optional<String> unifiedDiffValues = Optional.ofNullable(dataSetPatch.unifiedDiffValues);
        if (unifiedDiffValues.isPresent()) {
            List<String> originalLines = stringLines(original);
            List<String> diffLines = stringLines(unifiedDiffValues.get());
            if (diffLines.size() > 0) {
                List<String> diff = new ArrayList<>(diffLines);
                diff.add(0, "+++ t\n");
                diff.add(0, "--- t\n");
                Patch<String> patch = UnifiedDiffUtils.parseUnifiedDiff(diff);
                List<String> patchedOriginal = DiffUtils.patch(originalLines, patch);

                StringBuilder sb = new StringBuilder();
                patchedOriginal.forEach(s -> sb.append(s).append(NEWLINE));
                return sb.toString();
            }
        }
        return original;
    }

    public static List<String> stringLines(String s) {
        return Arrays.asList(s.split("\\R"));
    }
}
