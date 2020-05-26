package com.chutneytesting.engine.domain.execution.engine.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContextImpl;
import com.chutneytesting.engine.domain.execution.evaluation.SpelFunctions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("unchecked")
public class StepDataEvaluatorTest {

    private StepDataEvaluator evaluator = new StepDataEvaluator(new SpelFunctions());

    @Test
    public void testInputDataEvaluator() {
        TestObject testObject = new TestObject("attributeValue");

        Map<String, Object> context = new HashMap<>();
        context.put("destination", "stringDestination");
        context.put("jsonSringVariable", "{\"key\": \"value\"}");
        context.put("object", testObject);

        ScenarioContextImpl scenarioContext = new ScenarioContextImpl();
        scenarioContext.putAll(context);

        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("dateTimeFormat", "ss");
        innerMap.put("dateTimeFormatRef", "${#dateTimeFormat}");

        Map<String, Object> map = new HashMap<>();
        map.put("stringRawValue", "rawValue");
        map.put("objectRawValue", testObject);
        map.put("destination", "${#destination}");
        map.put("other destination", "other ${#destination}");
        map.put("${#destination}", "destination");
        map.put("jsonKey", "${#jsonSringVariable}");
        map.put("objectKey", "${#object}");
        map.put("${#object}", "objectValue");
        map.put("objectAttributeValue", "${#object.attribute()}");
        map.put("innerMap", innerMap);

        List<Object> list = new ArrayList<>();
        list.add("rawValue");
        list.add(testObject);
        list.add("${#destination}");
        list.add("other ${#destination}");
        list.add("${#jsonSringVariable}");
        list.add("${#object}");
        list.add("${#object.attribute()}");

        Set<Object> set = new HashSet<>();
        set.add("rawValue");
        set.add(testObject);
        set.add("${#destination}");
        set.add("other ${#destination}");
        set.add("${#jsonSringVariable}");
        set.add("${#object}");
        set.add("${#object.attribute()}");

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("stringRawValue", "rawValue");
        inputs.put("objectRawValue", testObject);
        inputs.put("destination", "${#destination}");
        inputs.put("jsonKey", "${#jsonSringVariable}");
        inputs.put("objectKey", "${#object}");
        inputs.put("inputReference", "${#objectKey}");
        inputs.put("objectAttributeValue", "${#object.attribute()}");
        inputs.put("map", map);
        inputs.put("list", list);
        inputs.put("set", set);

        Map<String, Object> evaluatedInputs = evaluator.evaluateNamedDataWithContextVariables(inputs, scenarioContext);

        assertThat(evaluatedInputs.get("stringRawValue")).isEqualTo("rawValue");
        assertThat(evaluatedInputs.get("objectRawValue")).isEqualTo(testObject);
        assertThat(evaluatedInputs.get("destination")).isEqualTo("stringDestination");
        assertThat(evaluatedInputs.get("jsonKey")).isEqualTo("{\"key\": \"value\"}");
        assertThat(evaluatedInputs.get("objectKey")).isEqualTo(testObject);
        assertThat(evaluatedInputs.get("inputReference")).isEqualTo(testObject);
        assertThat(evaluatedInputs.get("objectAttributeValue")).isEqualTo("attributeValue");

        assertThat(evaluatedInputs.get("map")).isInstanceOf(Map.class);
        Map evaluatedMap = (Map<String, Object>)evaluatedInputs.get("map");
        assertThat(evaluatedMap.get("stringRawValue")).isEqualTo("rawValue");
        assertThat(evaluatedMap.get("objectRawValue")).isEqualTo(testObject);
        assertThat(evaluatedMap.get("destination")).isEqualTo("stringDestination");
        assertThat(evaluatedMap.get("other destination")).isEqualTo("other stringDestination");
        assertThat(evaluatedMap.get("jsonKey")).isEqualTo("{\"key\": \"value\"}");
        assertThat(evaluatedMap.get("objectKey")).isEqualTo(testObject);
        assertThat(evaluatedMap.get(testObject)).isEqualTo("objectValue");
        assertThat(evaluatedMap.get("objectAttributeValue")).isEqualTo("attributeValue");

        assertThat(evaluatedMap.get("innerMap")).isInstanceOf(Map.class);
        Map evaluatedInnerMap = (Map<Object, Object>)evaluatedMap.get("innerMap");
        assertThat(evaluatedInnerMap.get("dateTimeFormat")).isEqualTo("ss");
        assertThat(evaluatedInnerMap.get("dateTimeFormatRef")).isEqualTo("ss");


        assertThat(evaluatedInputs.get("list")).isInstanceOf(List.class);
        List evaluatedList = (List<Object>)evaluatedInputs.get("list");
        assertThat(evaluatedList.get(0)).isEqualTo("rawValue");
        assertThat(evaluatedList.get(1)).isEqualTo(testObject);
        assertThat(evaluatedList.get(2)).isEqualTo("stringDestination");
        assertThat(evaluatedList.get(3)).isEqualTo("other stringDestination");
        assertThat(evaluatedList.get(4)).isEqualTo("{\"key\": \"value\"}");
        assertThat(evaluatedList.get(5)).isEqualTo(testObject);
        assertThat(evaluatedList.get(6)).isEqualTo("attributeValue");

        assertThat(evaluatedInputs.get("set")).isInstanceOf(Set.class);
        Set evaluatedSet = (Set<Object>)evaluatedInputs.get("set");
        assertThat(evaluatedSet).contains("rawValue");
        assertThat(evaluatedSet).contains(testObject);
        assertThat(evaluatedSet).contains("stringDestination");
        assertThat(evaluatedSet).contains("other stringDestination");
        assertThat(evaluatedSet).contains("{\"key\": \"value\"}");
        assertThat(evaluatedSet).contains(testObject);
        assertThat(evaluatedSet).contains("attributeValue");
    }

    @ParameterizedTest()
    @ValueSource(strings = {
        "${T(java.lang.Runtime).getRuntime().exec(\"echo I_c4n_5cr3w_Y0ur_1if3\")}",
        "${\"\".getClass().forName(\"java.lang.Runtime\").getMethod(\"getRuntime\")}",
        "${T(com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator).getClass().forName('java.lang.Runtime').getRuntime()}",
        "${new java.lang.ProcessBuilder({\"whoami\"}).start()}"
    })
    public void should_prevent_malicious_use_of_spel(String magicSpel) {
        // Given
        ScenarioContextImpl scenarioContext = new ScenarioContextImpl();
        scenarioContext.putAll(new HashMap<>());

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("MaliciousInjection", magicSpel);

        // When
        assertThatThrownBy(() ->
            evaluator.evaluateNamedDataWithContextVariables(inputs, scenarioContext)
        )
            .isInstanceOf(com.chutneytesting.engine.domain.execution.engine.evaluation.EvaluationException.class);
    }

    @Test
    public void should_not_prevent_legit_use_of_spel() {
        // Given
        ScenarioContextImpl scenarioContext = new ScenarioContextImpl();
        scenarioContext.putAll(new HashMap<>());

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("dateTimeFormat", "ss");
        inputs.put("MaliciousInjection", "${T(java.time.format.DateTimeFormatter).ofPattern(#dateTimeFormat).format(T(java.time.ZonedDateTime).now().plusSeconds(5))}");

        // When
        evaluator.evaluateNamedDataWithContextVariables(inputs, scenarioContext);
    }

    @Test
    public void should_evaluate_multiple_spel() {
        // Given
        ScenarioContextImpl scenarioContext = new ScenarioContextImpl();

        Map<String, Object> context = new HashMap<>();
        context.put("variable_toto", "toto");
        context.put("variable_tata", "tata");
        scenarioContext.putAll(context);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("singleVariable", "${#variable_toto}");
        inputs.put("singleVariableWithTextAfterSpel", "Text - ${#variable_toto}");
        inputs.put("singleVariableWithTextBeforeSpel", "${#variable_toto} - text");
        inputs.put("consecutiveVariables", "${#variable_toto}${#variable_tata}");
        inputs.put("twoVariablesSeparedByText", "${#variable_toto} - text - ${#variable_tata}");
        inputs.put("twoVariablesWithTextAtBeginning", "Text - ${#variable_toto} - ${#variable_tata}");
        inputs.put("twoVariablesWithTextAtTheEnd", "${#variable_toto} - ${#variable_tata} - text");

        // Object spel
        inputs.put("object", "${{'k1': 'value1'}}");
        inputs.put("objectWithSpaceAfter", "${{'k2': 'value2'}    }");
        inputs.put("objectWithSpaceBefore", "${    {'k3': 'value3'}}");
        inputs.put("objectWithSpaceAfterSuffix", "${{'k4': 'value4'}}    ");
        inputs.put("objectWithSpaceBeforePrefix", "     ${{'k5': 'value5'}}");

        // When
        Map<String, Object> evaluatedInputs = evaluator.evaluateNamedDataWithContextVariables(inputs, scenarioContext);

        assertThat(evaluatedInputs.get("singleVariable")).isEqualTo("toto");
        assertThat(evaluatedInputs.get("singleVariableWithTextAfterSpel")).isEqualTo("Text - toto");
        assertThat(evaluatedInputs.get("singleVariableWithTextBeforeSpel")).isEqualTo("toto - text");
        assertThat(evaluatedInputs.get("consecutiveVariables")).isEqualTo("tototata");
        assertThat(evaluatedInputs.get("twoVariablesSeparedByText")).isEqualTo("toto - text - tata");
        assertThat(evaluatedInputs.get("twoVariablesWithTextAtBeginning")).isEqualTo("Text - toto - tata");
        assertThat(evaluatedInputs.get("twoVariablesWithTextAtTheEnd")).isEqualTo("toto - tata - text");

        assertThat(((Map)evaluatedInputs.get("object")).get("k1")).isEqualTo("value1");
        assertThat(((Map)evaluatedInputs.get("objectWithSpaceAfter")).get("k2")).isEqualTo("value2");
        assertThat(((Map)evaluatedInputs.get("objectWithSpaceBefore")).get("k3")).isEqualTo("value3");
        assertThat(((Map)evaluatedInputs.get("objectWithSpaceAfterSuffix")).get("k4")).isEqualTo("value4");
        assertThat(((Map)evaluatedInputs.get("objectWithSpaceBeforePrefix")).get("k5")).isEqualTo("value5");
    }

    private class TestObject {
        private String attribute;
        public TestObject(String attribute) {
            this.attribute = attribute;
        }
        public String attribute() {
            return attribute;
        }
    }
}
