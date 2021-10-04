package com.chutneytesting.task.assertion;

import static com.chutneytesting.task.TaskValidatorsUtils.mapValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.task.spi.validation.Validator.of;

import com.chutneytesting.task.assertion.utils.JsonUtils;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.validation.Validator;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.ReadContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * TODO :
 * Refactor this class to have this dsl :
 * <p>
 * "documents": [
 * document:
 * name:"un_document_lambda"
 * content:"${#test_json}",
 * document:
 * name:"un_autre_document_lambda"
 * content:"${#test_json}",
 * document:
 * name:"encore_un_document_lambda"
 * content:"${#test_json}"
 * ],
 * "comparingPaths":[
 * compare {
 * first: {
 * documentName="un_document_lambda",
 * path="$.status"
 * },
 * second:{
 * documentName="un_document_lambda",
 * un_autre_document_lambda
 * }
 * }
 * ]
 */
public class JsonCompareTask implements Task {

    private final Logger logger;
    private final String document1;
    private final String document2;
    private final Map<String, String> comparingPaths;

    public JsonCompareTask(Logger logger,
                           @Input("document1") String document1,
                           @Input("document2") String document2,
                           @Input("comparingPaths") Map<String, String> comparingPaths) {
        this.logger = logger;
        this.document1 = document1;
        this.document2 = document2;
        this.comparingPaths = comparingPaths;
    }

    @Override
    public List<String> validateInputs() {
        Validator<String> document1Validation = of(document1)
            .validate(Objects::nonNull, "No document1 provided")
            .validate(d -> JsonUtils.jsonStringify(d), noException -> true, "Cannot parse document1");
        Validator<String> document2Validation = of(document2)
            .validate(Objects::nonNull, "No document2 provided")
            .validate(d -> JsonUtils.jsonStringify(d), noException -> true, "Cannot parse document2");
        return getErrorsFrom(
            document1Validation,
            document2Validation,
            mapValidation(comparingPaths, "comparingPaths")
        );
    }

    @Override
    public TaskExecutionResult execute() {
        ReadContext doc1 = JsonPath.parse(JsonUtils.jsonStringify(document1));
        ReadContext doc2 = JsonPath.parse(JsonUtils.jsonStringify(document2));
        boolean matchesOk = comparingPaths.entrySet().stream()
            .allMatch(cp -> {
                    boolean result = false;
                    try {
                        Object read1 = doc1.read(cp.getKey());
                        Object read2 = doc2.read(cp.getValue());
                        result = read1.equals(read2);
                        if (!result) {
                            logger.error("Value [" + read1 + "] at path [" + cp.getKey() + "] is not equal to value [" + read2 + "] at path [" + cp.getValue() + "]");
                        } else {
                            logger.info(read1.toString());
                            logger.info(read2.toString());
                        }
                    } catch (JsonPathException ex) {
                        logger.error(ex.getMessage());
                    }
                    return result;
                }
            );

        if (!matchesOk) {
            return TaskExecutionResult.ko();
        }
        return TaskExecutionResult.ok();
    }

}
