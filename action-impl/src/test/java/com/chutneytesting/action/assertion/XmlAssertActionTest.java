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

import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Failure;
import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

public class XmlAssertActionTest {

    @Test
    public void should_execute_2_successful_assertions_on_comparing_actual_result_to_expected() throws Exception {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("/root/node1/leaf1", "val1");
        expected.put("//leaf2", 5);
        expected.put("//node1/leaf3", "val2");
        expected.put("//node1/@at1", "val3");

        // Given
        String fakeActualResult = "<root><node1 at1=\"val3\"><leaf1>val1</leaf1><leaf2>5</leaf2><leaf3><![CDATA[val2]]></leaf3></node1></root>";

        // When
        Action action = new XmlAssertAction(mock(Logger.class), fakeActualResult, expected);
        ActionExecutionResult result = action.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
    }

    @Test
    public void should_execute_a_failing_assertion_on_comparing_actual_result_to_expected() throws Exception {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("/root/node1/leaf1", "val1");

        // Given

        String fakeActualResult = "<root><node1><leaf1>incorrrectValue</leaf1></node1></root>";

        // When
        Action action = new XmlAssertAction(mock(Logger.class), fakeActualResult, expected);
        ActionExecutionResult result = action.execute();

        // Then
        assertThat(result.status).isEqualTo(Failure);
    }

    @Test
    public void should_execute_a_failing_assertion_on_invalid_XML_content_in_actual() throws Exception {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("/root/node1/leaf1", "val1");

        // Given
        String fakeActualResult = "broken xml";

        // When
        Action action = new XmlAssertAction(mock(Logger.class), fakeActualResult, expected);
        ActionExecutionResult result = action.execute();

        // Then
        assertThat(result.status).isEqualTo(Failure);
    }

    @Test
    public void should_execute_a_failing_assertion_on_wrong_XPath_value_in_expected() throws Exception {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("//missingnode", "val1");

        // Given
        String fakeActualResult = "<root><node1><leaf1>val1</leaf1></node1></root>";

        // When
        Action action = new XmlAssertAction(mock(Logger.class), fakeActualResult, expected);
        ActionExecutionResult result = action.execute();

        // Then
        assertThat(result.status).isEqualTo(Failure);
    }

    @Test
    public void xpath_accesses_value_whatever_the_namepace() throws Exception {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("/descriptionComplete/test1/test2/number", "5072899");
        expected.put("/descriptionComplete/test1/test2/num", "5072899");

        // Given
        String fakeActualResult = loadFileFromClasspath("xml_samples/with_default_and_tag_namespaces.xml");

        // When
        Action action = new XmlAssertAction(mock(Logger.class), fakeActualResult, expected);
        ActionExecutionResult result = action.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
        //verify(stepContext, times(1)).success(eq("/descriptionComplete/test1/test2/number = 5072899"));
    }

    @Test
    public void should_execute_successful_assertions_with_placeholder() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("/something/value", "$isNotNull");
        expected.put("/something/notexist", "$isNull");
        expected.put("/something/valuenull", "$isNull");
        expected.put("/something/alphabet", "$contains:abcdefg");
        expected.put("/something/matchregexp", "$matches:\\d{4}-\\d{2}-\\d{2}");
        expected.put("/something/onedate", "$isBeforeDate:2010-01-01T11:12:13.1230Z");
        expected.put("/something/seconddate", "$isAfterDate:1998-07-14T02:03:04.456Z");
        expected.put("/something/thirddate", "$isEqualDate:2000-01-01T10:11:12.123Z");
        expected.put("/something/anumber", "$isLessThan:42000");
        expected.put("/something/thenumber", "$isGreaterThan:45");

        // Given
        String fakeActualResult =
            "<something>" +
                "<value>3</value>" +
                "<alphabet>abcdefg</alphabet>" +
                "<valuenull></valuenull>" +
                "<matchregexp>1983-10-26</matchregexp>" +
                "<onedate>2000-01-01T10:11:12.123Z</onedate>" +
                "<seconddate>2000-01-01T10:11:12.123Z</seconddate>" +
                "<thirddate>2000-01-01T10:11:12.123Z</thirddate>" +
                "<anumber>4 100</anumber>" +
                "<thenumber>46</thenumber>" +
            "</something>";

        // When
        Action jsonAssertAction = new XmlAssertAction(new TestLogger(), fakeActualResult, expected);
        ActionExecutionResult result = jsonAssertAction.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
    }

    @Test
    public void should_fails_when_xml_contains_doctype_declaration() {
        // Given
        String xml = loadFileFromClasspath("xml_samples/with_dtd.xml");

        // When
        Action task = new XmlAssertAction(mock(Logger.class), xml, new LinkedHashMap<>());
        ActionExecutionResult result = task.execute();

        // Then
        assertThat(result.status).isEqualTo(Failure);
    }

    @SuppressWarnings("resource")
    private String loadFileFromClasspath(String filePath) {
        return new Scanner(XmlAssertAction.class.getClassLoader().getResourceAsStream(filePath), StandardCharsets.UTF_8).useDelimiter("\\A").next();
    }
}
