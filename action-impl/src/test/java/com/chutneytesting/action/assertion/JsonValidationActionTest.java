/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.action.assertion;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.ActionExecutionResult.Status;
import com.chutneytesting.action.spi.injectable.Logger;
import org.junit.jupiter.api.Test;

public class JsonValidationActionTest {

    JsonValidationAction task;

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


        task = new JsonValidationAction(logger, json, SCHEMA);

        //When
        ActionExecutionResult result = task.execute();

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

        task = new JsonValidationAction(logger, json, SCHEMA);

        //When
        ActionExecutionResult result = task.execute();

        //Then
        assertThat(result.status).isEqualTo(Status.Failure);
    }


}
