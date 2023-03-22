package com.chutneytesting.action.assertion;

import static com.chutneytesting.action.common.JsonUtils.lenientEqual;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.enumValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

import com.chutneytesting.action.common.JsonUtils;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.ReadContext;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class JsonCompareAction implements Action {

    public enum COMPARE_MODE {STRICT, LENIENT}

    private final Logger logger;
    private final String document1;
    private final String document2;
    private final Map<String, String> paths;
    private final String mode;

    private final static Map<String, String> DEFAULT_PATHS = Map.of("$", "$");

    public JsonCompareAction(Logger logger,
                           @Input("document1") String document1,
                           @Input("document2") String document2,
                           @Input("comparingPaths") Map<String, String> comparingPaths,
                           @Input("mode") String mode
    ) {
        this.logger = logger;
        this.document1 = document1;
        this.document2 = document2;
        this.paths = ofNullable(comparingPaths).filter(not(Map::isEmpty)).orElse(DEFAULT_PATHS);
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
    public ActionExecutionResult execute() {
        ReadContext doc1 = JsonPath.parse(JsonUtils.jsonStringify(document1));
        ReadContext doc2 = JsonPath.parse(JsonUtils.jsonStringify(document2));
        AtomicBoolean result = new AtomicBoolean(true);
        paths.forEach((path1, path2) -> {
            try {
                Object read1 = doc1.read(path1);
                Object read2 = doc2.read(path2);
                if (!isEqual(read1, read2)) {
                    result.set(false);
                    logger.error("Value [" + read1 + "] at path [" + path1 + "] is not equal to value [" + read2 + "] at path [" + path2 + "]");
                } else {
                    logger.info(read1.toString());
                    logger.info(read2.toString());
                }
            } catch (JsonPathException ex) {
                logger.error(ex.getMessage());
            }
        });

        if (!result.get()) {
            return ActionExecutionResult.ko();
        }
        return ActionExecutionResult.ok();
    }

    private boolean isEqual(Object read1, Object read2) {
        return switch (COMPARE_MODE.valueOf(mode)) {
            case STRICT -> read1.equals(read2);
            case LENIENT -> lenientEqual(read1, read2, null);
        };
    }
}
