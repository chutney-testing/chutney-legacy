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
        this.mode = ofNullable(mode).filter(not(String::isEmpty)).orElse(COMPARE_MODE.STRICT.name());
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
                if (!read1.equals(read2)) {
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

}
