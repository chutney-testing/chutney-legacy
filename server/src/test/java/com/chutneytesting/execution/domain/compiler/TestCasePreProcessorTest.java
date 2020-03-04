package com.chutneytesting.execution.domain.compiler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;

public class TestCasePreProcessorTest {

    @Test
    public void should_escape_new_line_and_carriage_return() {
        TestCasePreProcessor mock = Mockito.mock(TestCasePreProcessor.class);
        when(mock.replaceParams(Mockito.any(), Mockito.any())).thenCallRealMethod();
        Map<String, String> dataset = new HashMap<>();
        dataset.put("value","http://host:port/path");
        dataset.put("value_multiline","first line \n seconde line \r third line");

        String resultSingleLine = mock.replaceParams(dataset, "to be replaced: **value**");
        assertThat(resultSingleLine).isEqualTo("to be replaced: http://host:port/path");

        String resultMultiLine = mock.replaceParams(dataset, "to be replaced: **value_multiline**");
        assertThat(resultMultiLine).isEqualTo("to be replaced: first line \\n seconde line \\n third line");
    }

}
