package com.chutneytesting.task;

import com.chutneytesting.task.domain.TaskInstantiationFailureException;
import com.chutneytesting.task.domain.TaskTemplate;
import com.chutneytesting.task.domain.UnresolvableTaskParameterException;
import com.chutneytesting.task.domain.parameter.Parameter;
import com.chutneytesting.task.domain.parameter.ParameterResolver;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class TestTaskTemplateFactory {
    private TestTaskTemplateFactory() {
    }

    public static TaskTemplate buildTaskTemplate(String taskType, Class<?> implementationClass) {
        return new TaskTemplate() {
            @Override
            public String identifier() {
                return taskType;
            }

            @Override
            public Class<?> implementationClass() {
                return implementationClass;
            }

            @Override
            public Set<Parameter> parameters() {
                return Collections.emptySet();
            }

            @Override
            public Task create(List<ParameterResolver> parameterResolvers) throws UnresolvableTaskParameterException, TaskInstantiationFailureException {
                throw new RuntimeException(TestTask.class.getSimpleName() + "s are not instantiable");
            }
        };
    }

    public interface TestTask {
        Object execute();
    }

    public static class TestTask1 implements TestTask {
        @Override
        public Object execute() {
            return null;
        }
    }

    public static class TestTask2 implements TestTask {
        private final Map<String, Object> inputs;

        public TestTask2(Map<String, Object> inputs) {
            this.inputs = inputs;
        }

        @Override
        public Object execute() {
            return inputs;
        }
    }

    public static class TestTask3 implements TestTask {
        @Override
        public Object execute() {
            throw new IllegalStateException("test error");
        }
    }

    public static class ValidSimpleTask implements Task {

        @Override
        public TaskExecutionResult execute() {
            return TaskExecutionResult.ok();
        }
    }

    public static class ComplexeTask implements Task {

        private final String someString;
        private final Pojo someObject;

        public ComplexeTask(@Input("stringParam") String someString, @Input("pojoParam") Pojo someObject) {
           this.someString = someString;
           this.someObject = someObject;
        }

        @Override
        public TaskExecutionResult execute() {
            Map<String, Object> store = new HashMap<>();
            store.put("someString", someString);
            store.put("someObject", someObject);
            return TaskExecutionResult.ok(store);
        }
    }

    public static class TwoParametersTask implements Task {
        private final Map<String, Object> store = new HashMap<>();

        public TwoParametersTask(String someString, int someInt) {
            store.put("someString", someString);
            store.put("someInt", someInt);
        }

        @Override
        public TaskExecutionResult execute() {
            return TaskExecutionResult.ok(store);
        }
    }

    public static class TwoConstructorTask implements Task {

        public TwoConstructorTask(String someString) {
        }

        public TwoConstructorTask(String someString, String someString2) {
        }

        @Override
        public TaskExecutionResult execute() {
            return TaskExecutionResult.ok();
        }
    }

    public static class Pojo {
        public final String param1;
        public final String param2;

        public Pojo(@Input("param1") String param1, @Input("param2")String param2) {
            this.param1 = param1;
            this.param2 = param2;
        }
    }
}
