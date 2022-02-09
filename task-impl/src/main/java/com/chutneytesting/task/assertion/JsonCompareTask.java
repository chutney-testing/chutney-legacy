package com.chutneytesting.task.assertion;

import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.enumValidation;
import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

import com.chutneytesting.task.assertion.utils.JsonUtils;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.ReadContext;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class JsonCompareTask implements Task {

    public enum COMPARE_MODE {STRICT, LENIENT}

    private final Logger logger;
    private final String document1;
    private final String document2;
    private final Map<String, String> pahs;
    private final String mode;

    private final static Map<String, String> DEFAULT_PATHS = Map.of("$", "$");

    public JsonCompareTask(Logger logger,
                           @Input("document1") String document1,
                           @Input("document2") String document2,
                           @Input("comparingPaths") Map<String, String> comparingPaths,
                           @Input("mode") String mode
    ) {
        this.logger = logger;
        this.document1 = document1;
        this.document2 = document2;
        this.pahs = ofNullable(comparingPaths).filter(not(Map::isEmpty)).orElse(DEFAULT_PATHS);
        this.mode = ofNullable(mode).filter(not(String::isEmpty)).map(String::toUpperCase).orElse(COMPARE_MODE.STRICT.name());
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notBlankStringValidation(document1, "document1"),
            notBlankStringValidation(document2, "document2"),
            enumValidation(COMPARE_MODE.class, mode, "mode")
        );
    }

    @Override
    public TaskExecutionResult execute() {
        ReadContext doc1 = JsonPath.parse(JsonUtils.jsonStringify(document1));
        ReadContext doc2 = JsonPath.parse(JsonUtils.jsonStringify(document2));
        AtomicBoolean result = new AtomicBoolean(true);
        pahs.forEach((key, value) -> {
            try {
                Object read1 = doc1.read(key);
                Object read2 = doc2.read(value);
                if (!isEqual(read1, read2)) {
                    result.set(false);
                    logger.error("Value [" + read1 + "] at path [" + key + "] is not equal to value [" + read2 + "] at path [" + value + "]");
                } else {
                    logger.info(read1.toString());
                    logger.info(read2.toString());
                }
            } catch (JsonPathException ex) {
                logger.error(ex.getMessage());
            }
        });

        if (!result.get()) {
            return TaskExecutionResult.ko();
        }
        return TaskExecutionResult.ok();
    }

    private boolean isEqual(Object read1, Object read2) {
        switch (COMPARE_MODE.valueOf(mode)) {
            case STRICT:
                return read1.equals(read2);
            case LENIENT:
                return lenientEqual(read1, read2, null);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean lenientEqual(Object read1, Object read2, Boolean leftContains) {
        if (read1 instanceof Map && read2 instanceof Map) {
            Map<Object, Object> map1 = (Map<Object, Object>) read1;
            Map<Object, Object> map2 = (Map<Object, Object>) read2;
            MapDifference<Object, Object> diff = Maps.difference(map1, map2);
            if (diff.areEqual()) {
                return true;
            } else if ((leftContains == null || !leftContains) && diff.entriesOnlyOnLeft().isEmpty()) {
                return diff.entriesDiffering().keySet().stream()
                    .map(k -> lenientEqual(map1.get(k), map2.get(k), false))
                    .reduce(Boolean::logicalAnd).orElse(true);
            } else if ((leftContains == null || leftContains) && diff.entriesOnlyOnRight().isEmpty()) {
                return diff.entriesDiffering().keySet().stream()
                    .map(k -> lenientEqual(map1.get(k), map2.get(k), true))
                    .reduce(Boolean::logicalAnd).orElse(true);
            }
            return false;
        } else if (read1 instanceof List && read2 instanceof List) {
            List<Object> list1 = (List<Object>) read1;
            List<Object> list2 = (List<Object>) read2;
            return list1.size() == list2.size() && list1.containsAll(list2);
        } else {
            return read1.equals(read2);
        }
    }
}
