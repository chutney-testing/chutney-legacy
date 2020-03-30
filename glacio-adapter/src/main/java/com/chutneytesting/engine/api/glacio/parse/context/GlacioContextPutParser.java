package com.chutneytesting.engine.api.glacio.parse.context;

import static java.util.Locale.ENGLISH;
import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.api.glacio.parse.GlacioParser;
import com.chutneytesting.engine.api.glacio.parse.InputsParser;
import com.chutneytesting.engine.api.glacio.parse.default_.EmptyParser;
import com.github.fridujo.glacio.ast.DataTable;
import com.github.fridujo.glacio.ast.Step;
import com.github.fridujo.glacio.ast.TableCell;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlacioContextPutParser extends GlacioParser {

    public GlacioContextPutParser() {
        this.targetParser = EmptyParser.instance;
        this.inputsParser = new ContextPutInputsParser();
        this.outputsParser = EmptyParser.instance;
        this.strategyParser = EmptyParser.instance;
    }

    @Override
    public String parseTaskType(Step step) {
        return "context-put";
    }

    @Override
    public Map<Locale, Set<String>> keywords() {
        Map<Locale, Set<String>> keywords = new HashMap<>();
        keywords.put(ENGLISH, new HashSet<>(Arrays.asList("Add", "add", "Put", "put", "Store", "store")));
        return keywords;
    }

    private class ContextPutInputsParser implements InputsParser {

        private final Pattern ENTRIES_PATTERN = Pattern.compile("(?: )?(?<key>[^\"][^ ]+[^\"]|\"[^\"]+\") (?<value>[^\"][^ ]+[^\"]|\"[^\"]+\")");

        @Override
        public Map<String, Object> parseTaskInputs(Step step) {
            Map<String, Object> entries = new HashMap<>();
            // DataTable or substeps entries
            if (step.getDataTable().isPresent()) {
                entries.putAll(extractDataTableEntries(step.getDataTable().get()));
            } else if (!step.getSubsteps().isEmpty()) {
                entries.putAll(extractSubStepsEntries(step.getSubsteps()));
            }
            return Collections.singletonMap("entries", entries);
        }

        private Map<? extends String, ?> extractInlineEntries(String inlineEntries) {
            Matcher matcher = ENTRIES_PATTERN.matcher(inlineEntries);
            Map<String, Object> entries = new HashMap<>();
            while (matcher.find()) {
                Optional<String> key = ofNullable(matcher.group("key"));
                Optional<String> value = ofNullable(matcher.group("value"));
                if (key.isPresent() && value.isPresent()) {
                    entries.put(trimEntryValue(key.get()), trimEntryValue(value.get()));
                }
            }
            return entries;
        }

        private Map<? extends String, ?> extractDataTableEntries(DataTable dataTable) {
            Map<String, Object> entries = new HashMap<>();
            dataTable.getRows().forEach(tableRow -> {
                while (tableRow.getCells().size() >= 2) {
                    List<TableCell> cells = tableRow.getCells().subList(0, 2);
                    entries.put(cells.get(0).getValue(), cells.get(1).getValue());
                    cells.clear();
                }
            });
            return entries;
        }

        private Map<? extends String, ?> extractSubStepsEntries(List<Step> subStepsEntries) {
            Map<String, Object> entries = new HashMap<>();
            subStepsEntries.forEach(step -> entries.putAll(extractInlineEntries(step.getText())));
            return entries;
        }

        private String trimEntryValue(String entryValue) {
            String trimedEntryValue = entryValue.trim();
            if (trimedEntryValue.startsWith("\"") && trimedEntryValue.endsWith("\"")) {
                return trimedEntryValue.substring(1, trimedEntryValue.length() - 1).trim();
            }
            return trimedEntryValue;
        }
    }
}
