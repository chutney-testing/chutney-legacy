package com.chutneytesting.action.function;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringFunctionTest {
    @Test
    public void should_replace_when_matching() {

        String input = "{ \"chutney\" : 12345, \"Carotte\" : \"poivron\" }";
        String regExp = "(?i)(.*\"caRotTe\" : )(\".*?\")(.*)";
        String replacement = "$1\"pimiento\"$3";

        String actual = StringFunction.stringReplace(input, regExp, replacement);

        Assertions.assertThat(actual).isEqualTo("{ \"chutney\" : 12345, \"Carotte\" : \"pimiento\" }");
    }
}
