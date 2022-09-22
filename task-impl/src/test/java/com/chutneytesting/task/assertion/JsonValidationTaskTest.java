package com.chutneytesting.task.assertion;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.TaskExecutionResult.Status;
import com.chutneytesting.task.spi.injectable.Logger;
import org.junit.jupiter.api.Test;

public class JsonValidationTaskTest {

    JsonValidationTask task;

    private final String SCHEMA = "{\n" +
        "    \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
        "    \"title\": \"Product\",\n" +
        "    \"description\": \"A product from the catalog\",\n" +
        "    \"type\": \"object\",\n" +
        "    \"properties\": {\n" +
        "        \"id\": {\n" +
        "            \"description\": \"The unique identifier for a product\",\n" +
        "            \"type\": \"integer\"\n" +
        "        },\n" +
        "        \"name\": {\n" +
        "            \"description\": \"Name of the product\",\n" +
        "            \"type\": \"string\"\n" +
        "        },\n" +
        "        \"price\": {\n" +
        "            \"type\": \"number\",\n" +
        "            \"minimum\": 0,\n" +
        "            \"exclusiveMinimum\": true\n" +
        "        }\n" +
        "    },\n" +
        "    \"required\": [\"id\", \"name\", \"price\"]\n" +
        "}";

    @Test
    public void should_validate_simple_json() {
        Logger logger = new TestLogger();
        String json = "{\n" +
            "    \"id\": 1,\n" +
            "    \"name\": \"Lampshade\",\n" +
            "    \"price\": 12\n" +
            "}";


        task = new JsonValidationTask(logger, json, SCHEMA);

        //When
        TaskExecutionResult result = task.execute();

        //Then
        assertThat(result.status).isEqualTo(Status.Success);
    }


    @Test
    public void should_not_validate_simple_json() {
        Logger logger = new TestLogger();
        String json = "{\n" +
            "    \"id\": 1,\n" +
            "    \"name\": \"Lampshade\",\n" +
            "    \"price\": 0\n" +
            "}";

        task = new JsonValidationTask(logger, json, SCHEMA);

        //When
        TaskExecutionResult result = task.execute();

        //Then
        assertThat(result.status).isEqualTo(Status.Failure);
    }


}
