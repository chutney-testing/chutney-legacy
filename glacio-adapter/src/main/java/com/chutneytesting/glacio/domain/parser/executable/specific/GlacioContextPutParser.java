package com.chutneytesting.glacio.domain.parser.executable.specific;

import static java.util.Locale.ENGLISH;
import static java.util.Optional.ofNullable;

import com.chutneytesting.glacio.domain.parser.ExecutableGlacioStepParser;
import com.chutneytesting.glacio.domain.parser.GlacioStepParser;
import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.chutneytesting.glacio.domain.parser.executable.common.EmptyParser;
import com.chutneytesting.glacio.domain.parser.util.ParserHelper;
import com.github.fridujo.glacio.model.DataTable;
import com.github.fridujo.glacio.model.Step;
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

public class GlacioContextPutParser extends ExecutableGlacioStepParser {

    public GlacioContextPutParser() {
        super(EmptyParser.noTargetParser,
            new ContextPutInputsParser(),
            EmptyParser.emptyMapParser,
            EmptyParser.emptyMapParser);
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

    private static class ContextPutInputsParser implements GlacioStepParser<Map<String, Object>> {

        private final Pattern ENTRIES_PATTERN = Pattern.compile("^(?<key>[^\"][^\\s]+[^\"]|\"[^\"]+\")\\s?(?<value>.*)$");

        @Override
        public Map<String, Object> parseGlacioStep(ParsingContext context, Step step) {
            Map<String, Object> entries = new HashMap<>();
            // DataTable or substeps entries
            Optional<DataTable> dataTable = ParserHelper.stepDataTable(step);
            if (dataTable.isPresent()) {
                entries.putAll(extractDataTableEntries(dataTable.get()));
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
                if (tableRow.getCells().size() == 2) {
                    List<String> cells = tableRow.getCells().subList(0, 2);
                    entries.put(cells.get(0), cells.get(1));
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
