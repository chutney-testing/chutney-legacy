package com.chutneytesting.action.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.TestActionTemplateFactory.ComplexAction;
import com.chutneytesting.action.TestActionTemplateFactory.TwoConstructorAction;
import com.chutneytesting.action.TestActionTemplateFactory.TwoParametersAction;
import com.chutneytesting.action.TestActionTemplateFactory.ValidSimpleAction;
import org.junit.jupiter.api.Test;

public class ActionTemplateParserV2Test {

    private ActionTemplateParserV2 parser = new ActionTemplateParserV2();

    @Test
    public void simple_action_parsing() {
        ResultOrError<ActionTemplate, ParsingError> parsingResult = parser.parse(ValidSimpleAction.class);

        assertThat(parsingResult.isOk()).isTrue();
        ActionTemplate actionTemplate = parsingResult.result();
        assertThat(actionTemplate.identifier()).isEqualTo("valid-simple");
        assertThat(actionTemplate.implementationClass()).isEqualTo(ValidSimpleAction.class);
        assertThat(actionTemplate.parameters()).hasSize(0);
    }

    @Test
    public void complex_action_parsing() {
        ResultOrError<ActionTemplate, ParsingError> parsingResult = parser.parse(ComplexAction.class);

        assertThat(parsingResult.isOk()).isTrue();
        ActionTemplate actionTemplate = parsingResult.result();
        assertThat(actionTemplate.identifier()).isEqualTo("complex");
        assertThat(actionTemplate.implementationClass()).isEqualTo(ComplexAction.class);
        assertThat(actionTemplate.parameters()).hasSize(2);
    }


    @Test
    public void action_with_parameters_parsing() {
        ResultOrError<ActionTemplate, ParsingError> parsingResult = parser.parse(TwoParametersAction.class);

        assertThat(parsingResult.isOk()).isTrue();
        ActionTemplate actionTemplate = parsingResult.result();
        assertThat(actionTemplate.identifier()).isEqualTo("two-parameters");
        assertThat(actionTemplate.implementationClass()).isEqualTo(TwoParametersAction.class);
        assertThat(actionTemplate.parameters()).hasSize(2);
    }

    @Test
    public void action_with_more_than_one_constructor() {
        ActionTemplateParserV2 parser = new ActionTemplateParserV2();
        ResultOrError<ActionTemplate, ParsingError> parsingResult = parser.parse(TwoConstructorAction.class);
        assertThat(parsingResult.isError()).isTrue();
        assertThat(parsingResult.error().errorMessage()).isEqualTo("More than one constructor");
    }
}
