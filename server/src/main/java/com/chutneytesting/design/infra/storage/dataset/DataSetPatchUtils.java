package com.chutneytesting.design.infra.storage.dataset;

import static java.util.Collections.emptyMap;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

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
                    List<String> lineValues = extractMultipleValuesLine(line);
                    if (multipleValuesHeaders.isEmpty()) { // headers
                        multipleValuesHeaders.addAll(lineValues);
                    } else { // values
                        Map<String, String> valuesMap = new LinkedHashMap<>();
                        Iterator<String> headersIterator = multipleValuesHeaders.iterator();
                        Iterator<String> valuesIterator = lineValues.iterator();
                        while (headersIterator.hasNext() && valuesIterator.hasNext()) {
                            valuesMap.put(headersIterator.next(), valuesIterator.next());
                        }
                        multipleValues.add(valuesMap);
                    }
                } else { // Unique values
                    String[] uniqueValue = line.split(SEPARATOR_REGEX);
                    if (uniqueValue.length == 2) {
                        uniqueValues.put(uniqueValue[0].trim(), uniqueValue[1].trim());
                    } else if (uniqueValue.length == 1) {
                        uniqueValues.put(uniqueValue[0].trim(), "");
                    }
                }
            }
        }
        return Pair.of(uniqueValues, multipleValues);
    }

    public static String dataSetValues(DataSet dataSet, boolean pretty) {
        Map<String, String> uniqueValues = dataSet.constants;
        List<Map<String, String>> multipleValues = dataSet.datatable;

        StringBuilder values = new StringBuilder();

        // Process unique values first
        if (!uniqueValues.isEmpty()) {
            int maxKeyLength = 0;
            if (pretty) {
                maxKeyLength = uniqueValues.keySet().stream().mapToInt(String::length).max().getAsInt();
            }
            int finalMaxKeyLength = maxKeyLength;
            uniqueValues.forEach((k, v) -> addUniqueValueLine(values, k, v, finalMaxKeyLength - k.length()));
            // Blank line between unique and multiple values
            values.append(NEWLINE);
        }

        // Process multiple values
        if (!multipleValues.isEmpty()) {
            List<String> headers = new ArrayList<>(multipleValues.get(0).keySet());
            List<Integer> columnLength = new ArrayList<>();
            if (pretty) {
                headers.forEach(m ->
                    columnLength.add(multipleValues.stream().mapToInt(mm -> mm.get(m).length()).max().getAsInt()));

            }
            addMultipleValueLine(values, headers, columnLength, emptyMap());
            multipleValues.forEach(m -> addMultipleValueLine(values, headers, columnLength, m));
        }

        return values.toString();
    }

    public static String unifiedDiff(String revised, String original) {
        try {
            List<String> originalLines = stringLines(original);
            Patch<String> diff = DiffUtils.diff(originalLines, stringLines(revised));
            List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff("original", "revised", originalLines, diff, 0);
            return unifiedDiff.stream().collect(Collectors.joining("\r"));
        } catch (DiffException e) {
            throw new RuntimeException(e);
        }
    }

    public static String patchString(String original, DataSetPatch dataSetPatch) throws PatchFailedException {
        Optional<String> unifiedDiffValues = Optional.ofNullable(dataSetPatch.unifiedDiffValues);
        if (unifiedDiffValues.isPresent()) {
            List<String> originalLines = stringLines(original);
            List<String> diffLines = stringLines(unifiedDiffValues.get());
            if (diffLines.size() > 0 && (diffLines.size() != 1 && !"".equals(diffLines.get(0)))) {
                List<String> diff = new ArrayList<>(diffLines);
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

    private static List<String> extractMultipleValuesLine(String line) {
        List<String> values = new ArrayList<>();
        String[] lineValues = line.split(SEPARATOR_REGEX);
        for (int i = 1; i < lineValues.length; i++) {
            values.add(lineValues[i].trim());
        }
        return values;
    }

    private static void addUniqueValueLine(StringBuilder uniqueValues, String key, String value, Integer spaces) {
        StringBuilder line = new StringBuilder();
        line.append(key);
        addSpaces(line, spaces);
        line.append(SEPARATOR_SPACED).append(value);
        uniqueValues.append(line.toString().trim()).append(NEWLINE);
    }

    private static void addMultipleValueLine(StringBuilder multipleValues, List<String> headers, List<Integer> columnLength, Map<String, String> values) {
        StringBuilder line = new StringBuilder();
        boolean isLengthSet = !columnLength.isEmpty();
        if (values.isEmpty()) { // Add header values
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                line.append(SEPARATOR_SPACED).append(header);
                if (isLengthSet) {
                    addSpaces(line, columnLength.get(i) - header.length());
                }
            }
        } else { // Add values
            for (int i = 0; i < headers.size(); i++) {
                String value = values.get(headers.get(i));
                line.append(SEPARATOR_SPACED).append(value);
                if (isLengthSet) {
                    addSpaces(line, columnLength.get(i) - value.length());
                }
            }
        }
        line.append(SEPARATOR_SPACED);
        multipleValues.append(line.toString().trim()).append(NEWLINE);
    }

    private static void addSpaces(StringBuilder line, Integer nb) {
        for (int i = 0; i < nb; i++) {
            line.append(SPACE);
        }
    }
}
