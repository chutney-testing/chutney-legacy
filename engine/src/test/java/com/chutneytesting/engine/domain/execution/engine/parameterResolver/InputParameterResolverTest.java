package com.chutneytesting.engine.domain.execution.engine.parameterResolver;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.action.domain.parameter.Parameter;
import com.chutneytesting.action.spi.injectable.Input;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

public class InputParameterResolverTest {

    private static class CanResolveAction {
        public final String annotatedInput;

        CanResolveAction(@Input("inputName") String annotatedInput, String input) {
            this.annotatedInput = annotatedInput;
        }
    }

    @Test
    public void could_resolve_parameter_annotated_with_Input_api_annotation() {
        java.lang.reflect.Parameter[] actionParameters = CanResolveAction.class.getDeclaredConstructors()[0].getParameters();
        InputParameterResolver sut = new InputParameterResolver(emptyMap());

        Parameter annotatedParameter = Parameter.fromJavaParameter(actionParameters[0]);
        assertThat(sut.canResolve(annotatedParameter)).isTrue();

        Parameter parameter = Parameter.fromJavaParameter(actionParameters[1]);
        assertThat(sut.canResolve(parameter)).isFalse();
    }

    private static class NoNameInputAction {
        NoNameInputAction(@Input("") String noNameInput) {
        }
    }

    @Test
    public void coudld_not_resolve_nameless_annotated_parameter() {
        java.lang.reflect.Parameter[] actionParameters = NoNameInputAction.class.getDeclaredConstructors()[0].getParameters();
        Parameter namelessParameter = Parameter.fromJavaParameter(actionParameters[0]);
        InputParameterResolver sut = new InputParameterResolver(emptyMap());

        assertThatThrownBy(() -> sut.resolve(namelessParameter))
            .isExactlyInstanceOf(InputNameMandatoryException.class);
    }

    private static class ComplexType {
        public final String a, b;

        public ComplexType(@Input("simpleA") String a, @Input("simpleB") String b) {
            this.a = a;
            this.b = b;
        }
    }

    @Test
    public void should_resolve_to_null_when_simple_parameter_and_input_null() {
        java.lang.reflect.Parameter[] actionParameters = CanResolveAction.class.getDeclaredConstructors()[0].getParameters();
        Parameter parameter = Parameter.fromJavaParameter(actionParameters[0]);
        Map<String, Object> mapWithNull = new HashMap<>();
        mapWithNull.put("inputName", null);
        InputParameterResolver sut = new InputParameterResolver(mapWithNull);

        assertThat(sut.resolve(parameter)).isNull();
    }

    @Test
    public void should_resolve_to_input_when_simple_parameter() {
        java.lang.reflect.Parameter[] actionParameters = CanResolveAction.class.getDeclaredConstructors()[0].getParameters();
        Parameter parameter = Parameter.fromJavaParameter(actionParameters[0]);
        InputParameterResolver sut = new InputParameterResolver(Map.of("inputName", "input value"));

        assertThat(sut.resolve(parameter)).isEqualTo("input value");
    }

    private static class AssignableFromAction {
        AssignableFromAction(@Input("inputName") Object object) {
        }
    }

    @Test
    public void should_resolve_to_input_when_assignable_for_simple_parameter() {
        java.lang.reflect.Parameter[] actionParameters = AssignableFromAction.class.getDeclaredConstructors()[0].getParameters();
        Parameter parameter = Parameter.fromJavaParameter(actionParameters[0]);

        List<?> list = Lists.list("a string", (short) 5, 5, 5L, Boolean.TRUE, 'r', 7.9F, 7.9D, (byte) 42);

        for (Object o : list) {
            InputParameterResolver sut = new InputParameterResolver(Map.of("inputName", o));
            assertThat(sut.resolve(parameter)).isEqualTo(o);
        }
    }

    @Test
    public void should_resolve_map_to_json_string_when_string_parameter() {
        java.lang.reflect.Parameter[] actionParameters = CanResolveAction.class.getDeclaredConstructors()[0].getParameters();
        Parameter parameter = Parameter.fromJavaParameter(actionParameters[0]);
        InputParameterResolver sut = new InputParameterResolver(Map.of("inputName",
            Map.of("k1", "v1",
                "k2", Map.of("k21", "v21"),
                "k3", Lists.list("v3", Map.of("k31", "v31")))
        ));

        assertThat(sut.resolve(parameter)).isEqualTo("{\"k3\":[\"v3\",{\"k31\":\"v31\"}],\"k1\":\"v1\",\"k2\":{\"k21\":\"v21\"}}");
    }

    @Test
    public void should_resolve_primitives_when_string_parameter() {
        java.lang.reflect.Parameter[] actionParameters = CanResolveAction.class.getDeclaredConstructors()[0].getParameters();
        Parameter parameter = Parameter.fromJavaParameter(actionParameters[0]);

        List<?> list = Lists.list((short) 5, 5, 5L, Boolean.TRUE, 'r', 7.9F, 7.9D, (byte) 78);

        for (Object o : list) {
            InputParameterResolver sut = new InputParameterResolver(Map.of("inputName", o));
            assertThat(sut.resolve(parameter)).isEqualTo(String.valueOf(o));
        }
    }

    private static class PrimitivesAction {
        PrimitivesAction(@Input("inputName") Short short$, @Input("inputName") Integer int$, @Input("inputName") Long long$, @Input("inputName") Boolean boolean$,
                         @Input("inputName") Character char$, @Input("inputName") Float float$, @Input("inputName") Double double$, @Input("inputName") Byte byte$) {
        }
    }

    @Test
    public void should_resolve_string_when_primitive_parameter() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        java.lang.reflect.Parameter[] actionParameters = PrimitivesAction.class.getDeclaredConstructors()[0].getParameters();

        List<String> inputValues = Lists.list("5", "5", "5", "true", "ru", "7.9", "7.9", "78");
        List<Method> valueOfLists = Lists.list(
            Short.class.getMethod("valueOf", String.class), Integer.class.getMethod("valueOf", String.class),
            Long.class.getMethod("valueOf", String.class), Boolean.class.getMethod("valueOf", String.class),
            Character.class.getMethod("valueOf", char.class), Float.class.getMethod("valueOf", String.class),
            Double.class.getMethod("valueOf", String.class), Byte.class.getMethod("valueOf", String.class)
        );

        for (int i = 0, listSize = inputValues.size(); i < listSize; i++) {
            String inputValue = inputValues.get(i);
            Method valueOfMethod = valueOfLists.get(i);
            Parameter parameter = Parameter.fromJavaParameter(actionParameters[i]);

            InputParameterResolver sut = new InputParameterResolver(Map.of("inputName", inputValue));
            if (valueOfMethod.getDeclaringClass().equals(Character.class)) {
                assertThat(sut.resolve(parameter)).isEqualTo(valueOfMethod.invoke(null, inputValue.charAt(0)));
            } else {
                assertThat(sut.resolve(parameter)).isEqualTo(valueOfMethod.invoke(null, inputValue));
            }
        }
    }

    @Test
    public void should_throw_exception_when_cannot_resolve() {
        java.lang.reflect.Parameter[] actionParameters = CanResolveAction.class.getDeclaredConstructors()[0].getParameters();
        Parameter parameter = Parameter.fromJavaParameter(actionParameters[0]);

        InputParameterResolver sut = new InputParameterResolver(Map.of("inputName", Lists.list(1, 2, 3)));
        assertThatThrownBy(() -> sut.resolve(parameter)).isExactlyInstanceOf(IllegalArgumentException.class);

        InputParameterResolver sutA = new InputParameterResolver(Map.of("inputName", new HashSet<>(Lists.list(1, 2, 3))));
        assertThatThrownBy(() -> sutA.resolve(parameter)).isExactlyInstanceOf(IllegalArgumentException.class);

        InputParameterResolver sutB = new InputParameterResolver(Map.of("inputName", new ComplexType("", "")));
        assertThatThrownBy(() -> sutB.resolve(parameter)).isExactlyInstanceOf(IllegalArgumentException.class);

        actionParameters = PrimitivesAction.class.getDeclaredConstructors()[0].getParameters();
        Parameter anotherParameter = Parameter.fromJavaParameter(actionParameters[0]);

        InputParameterResolver sutC = new InputParameterResolver(Map.of("inputName", Lists.list(1, 2, 3)));
        assertThatThrownBy(() -> sutC.resolve(anotherParameter)).isExactlyInstanceOf(IllegalArgumentException.class);

        InputParameterResolver sutD = new InputParameterResolver(Map.of("inputName", new HashSet<>(Lists.list(1, 2, 3))));
        assertThatThrownBy(() -> sutD.resolve(anotherParameter)).isExactlyInstanceOf(IllegalArgumentException.class);

        InputParameterResolver sutE = new InputParameterResolver(Map.of("inputName", new ComplexType("", "")));
        assertThatThrownBy(() -> sutE.resolve(anotherParameter)).isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
