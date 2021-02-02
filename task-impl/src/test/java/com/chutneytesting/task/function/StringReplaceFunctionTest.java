package com.chutneytesting.task.function;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringReplaceFunctionTest {
    @Test
    public void should_replace_when_matching() throws Exception {

        String input = "{ \"chutney\" : 12345, \"Carotte\" : \"poivron\" }";
        String regExp = "(?i)(.*\"caRotTe\" : )(\".*?\")(.*)";
        String replacement = "$1\"pimiento\"$3";

        String actual = StringReplaceFunction.str_replace(input, regExp, replacement);

        Assertions.assertThat(actual).isEqualTo("{ \"chutney\" : 12345, \"Carotte\" : \"pimiento\" }");
    }
}
