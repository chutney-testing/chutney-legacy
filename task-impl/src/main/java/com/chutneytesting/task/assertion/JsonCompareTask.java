package com.chutneytesting.task.assertion;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.ReadContext;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.assertion.json.JsonUtils;
import java.util.Map;

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

        checkInputs(document1, document2, comparingPaths);

        this.logger = logger;
        this.document1 = JsonUtils.jsonStringify(document1);
        this.document2 = JsonUtils.jsonStringify(document2);
        this.comparingPaths = comparingPaths;
    }

    /**
     * Checks whether all required fields are provided.
     */
    private void checkInputs(String doc1, String doc2, Map<String, String> comparingPaths) throws IllegalStateException {
        if (doc1 == null) {
            logger.error("'document1' argument is required");
            throw new IllegalStateException("'document1' argument is required");
        }
        if (doc2 == null) {
            logger.error("'document2' argument is required");
            throw new IllegalStateException("'document2' argument is required");
        }
        if (comparingPaths == null || comparingPaths.isEmpty()) {
            logger.error("'comparingPaths' argument is required");
            throw new IllegalStateException("'comparingPaths' argument is required");
        }
    }

    @Override
    public TaskExecutionResult execute() {

        ReadContext doc1 = JsonPath.parse(document1);
        ReadContext doc2 = JsonPath.parse(document2);
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
