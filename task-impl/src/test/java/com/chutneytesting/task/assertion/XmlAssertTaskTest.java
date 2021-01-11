package com.chutneytesting.task.assertion;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Failure;
import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import org.junit.Test;

public class XmlAssertTaskTest {

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
        Task task = new XmlAssertTask(mock(Logger.class), fakeActualResult, expected);
        TaskExecutionResult result = task.execute();

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
        Task task = new XmlAssertTask(mock(Logger.class), fakeActualResult, expected);
        TaskExecutionResult result = task.execute();

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
        Task task = new XmlAssertTask(mock(Logger.class), fakeActualResult, expected);
        TaskExecutionResult result = task.execute();

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
        Task task = new XmlAssertTask(mock(Logger.class), fakeActualResult, expected);
        TaskExecutionResult result = task.execute();

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
        Task task = new XmlAssertTask(mock(Logger.class), fakeActualResult, expected);
        TaskExecutionResult result = task.execute();

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
        Task jsonAssertTask = new XmlAssertTask(new TestLogger(), fakeActualResult, expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
    }

    @SuppressWarnings("resource")
    private String loadFileFromClasspath(String filePath) {
        return new Scanner(XmlAssertTask.class.getClassLoader().getResourceAsStream(filePath), "UTF-8").useDelimiter("\\A").next();
    }
}
