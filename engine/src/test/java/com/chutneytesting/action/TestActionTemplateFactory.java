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

package com.chutneytesting.action;

import com.chutneytesting.action.domain.ActionInstantiationFailureException;
import com.chutneytesting.action.domain.ActionTemplate;
import com.chutneytesting.action.domain.UnresolvableActionParameterException;
import com.chutneytesting.action.domain.parameter.Parameter;
import com.chutneytesting.action.domain.parameter.ParameterResolver;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class TestActionTemplateFactory {
    private TestActionTemplateFactory() {
    }

    public static ActionTemplate buildActionTemplate(String actionType, Class<?> implementationClass) {
        return new ActionTemplate() {
            @Override
            public String identifier() {
                return actionType;
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
            public Action create(List<ParameterResolver> parameterResolvers) throws UnresolvableActionParameterException, ActionInstantiationFailureException {
                throw new RuntimeException(TestAction.class.getSimpleName() + "s are not instantiable");
            }
        };
    }

    public interface TestAction {
        Object execute();
    }

    public static class TestAction1 implements TestAction {
        @Override
        public Object execute() {
            return null;
        }
    }

    public static class TestAction2 implements TestAction {
        private final Map<String, Object> inputs;

        public TestAction2(Map<String, Object> inputs) {
            this.inputs = inputs;
        }

        @Override
        public Object execute() {
            return inputs;
        }
    }

    public static class TestAction3 implements TestAction {
        @Override
        public Object execute() {
            throw new IllegalStateException("test error");
        }
    }

    public static class ValidSimpleAction implements Action {

        @Override
        public ActionExecutionResult execute() {
            return ActionExecutionResult.ok();
        }
    }

    public static class ComplexAction implements Action {

        private final String someString;
        private final Pojo someObject;

        public ComplexAction(@Input("stringParam") String someString, @Input("pojoParam") Pojo someObject) {
            this.someString = someString;
            this.someObject = someObject;
        }

        @Override
        public ActionExecutionResult execute() {
            Map<String, Object> store = new HashMap<>();
            store.put("someString", someString);
            store.put("someObject", someObject);
            return ActionExecutionResult.ok(store);
        }
    }

    public static class TwoParametersAction implements Action {
        private final Map<String, Object> store = new HashMap<>();

        public TwoParametersAction(String someString, int someInt) {
            store.put("someString", someString);
            store.put("someInt", someInt);
        }

        @Override
        public ActionExecutionResult execute() {
            return ActionExecutionResult.ok(store);
        }
    }

    public static class TwoConstructorAction implements Action {

        public TwoConstructorAction(String someString) {
        }

        public TwoConstructorAction(String someString, String someString2) {
        }

        @Override
        public ActionExecutionResult execute() {
            return ActionExecutionResult.ok();
        }
    }

    public static class SuccessAction implements Action {

        @Override
        public ActionExecutionResult execute() {
            return ActionExecutionResult.ok();
        }
    }

    public static class FailAction implements Action {

        @Override
        public ActionExecutionResult execute() {
            return ActionExecutionResult.ko();
        }
    }

    public static class SleepAction implements Action {

        private final Duration duration;

        public SleepAction(@Input("duration") String duration) {
            this.duration = Duration.parse(duration);
        }

        @Override
        public ActionExecutionResult execute() {
            try {
                TimeUnit.MILLISECONDS.sleep(duration.toMilliseconds());
            } catch (InterruptedException e) {
                return ActionExecutionResult.ko();
            }
            return ActionExecutionResult.ok();
        }
    }

    public static class ContextPutAction implements Action {

        private final Map<String, Object> entries;

        public ContextPutAction(@Input("entries") Map<String, Object> entries) {
            this.entries = entries;
        }

        @Override
        public ActionExecutionResult execute() {
            return ActionExecutionResult.ok(entries);
        }
    }

    public static class ListAction implements Action {

        private final List<Map<String, Object>> list;

        public ListAction(@Input("list") List<Map<String, Object>> list) {
            this.list = list;
        }

        @Override
        public ActionExecutionResult execute() {
            return ActionExecutionResult.ok("mylist", list);
        }
    }

    public static class Pojo {
        public final String param1;
        public final String param2;

        public Pojo(@Input("param1") String param1, @Input("param2") String param2) {
            this.param1 = param1;
            this.param2 = param2;
        }
    }
}
