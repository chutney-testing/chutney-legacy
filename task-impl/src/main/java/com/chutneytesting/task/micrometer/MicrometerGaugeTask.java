package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.checkRegistry;
import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.toOutputs;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.tools.Try;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MicrometerGaugeTask implements Task {

    protected static final String OUTPUT_GAUGE = "micrometerGaugeObject";

    private final Logger logger;
    private final String name;
    private final String description;
    private final String unit;
    private final List<String> tags;
    private final Object gaugeObject;
    private final String gaugeFunction;
    private final MeterRegistry registry;
    private final boolean strongReference;

    public MicrometerGaugeTask(Logger logger,
                               @Input("name") String name,
                               @Input("description") String description,
                               @Input("unit") String unit,
                               @Input("strongReference") Boolean strongReference,
                               @Input("tags") List<String> tags,
                               @Input("gaugeObject") Object gaugeObject,
                               @Input("gaugeFunction") String gaugeFunction,
                               @Input("registry") MeterRegistry registry) {
        this.logger = logger;
        this.name = requireNonNull(name);
        this.description = description;
        this.unit = unit;
        this.strongReference = ofNullable(strongReference).orElse(Boolean.FALSE);
        this.tags = tags;

        if (gaugeObject == null && gaugeFunction == null) {
            throw new IllegalArgumentException("gaugeObject and gaugeFunction cannot be both null");
        }

        this.gaugeObject = gaugeObject;
        this.gaugeFunction = gaugeFunction;
        this.registry = registry;
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            Gauge gauge = this.retrieveGauge(registry);
            logger.info("Gauge current value is " + gauge.value());
            return TaskExecutionResult.ok(toOutputs(OUTPUT_GAUGE, gaugeObject));
        } catch (Exception e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }

    private Gauge retrieveGauge(MeterRegistry registry) {
        MeterRegistry registryToUse = checkRegistry(registry);
        Gauge.Builder<?> builder = createGaugeBuilder()
            .description(description)
            .strongReference(strongReference)
            .baseUnit(unit);

        ofNullable(tags).ifPresent(t -> builder.tags(t.toArray(new String[0])));

        return builder.register(registryToUse);
    }

    private Gauge.Builder<?> createGaugeBuilder() {
        Gauge.Builder<?> builder;
        if (gaugeObject != null && gaugeFunction == null) {
            if (gaugeObject instanceof Number) {
                builder = Gauge.builder(name, (Number) gaugeObject, Number::doubleValue);
            } else if (gaugeObject instanceof Collection) {
                builder = Gauge.builder(name, (Collection) gaugeObject, Collection::size);
            } else if (gaugeObject instanceof Map) {
                builder = Gauge.builder(name, (Map) gaugeObject, Map::size);
            } else {
                throw new IllegalArgumentException("gaugeObject must be a Number, a Collection or a Map if no gaugeFunction supplied");
            }
        } else if (gaugeObject != null) {
            Method method = retrieveMethod(gaugeFunction, gaugeObject.getClass());
            builder = Gauge.builder(name, gaugeObject, o -> {
                try {
                    return ((Number) method.invoke(o)).doubleValue();
                } catch (Exception e) {
                    return Double.MIN_VALUE;
                }
            });
        } else {
            Method method = retrieveMethod(gaugeFunction, null);
            builder = Gauge.builder(name, () -> {
                try {
                    return ((Number) method.invoke(null)).doubleValue();
                } catch (Exception e) {
                    return Double.MIN_VALUE;
                }
            });
        }
        return builder;
    }

    private Method retrieveMethod(final String methodPath, final Class clazz) {
        if (clazz == null && !methodPath.contains(".")) {
            throw new IllegalArgumentException("Method " + methodPath + " cannot be resolved");
        }

        Class<?> methodClass = clazz;
        String methodName = methodPath;

        if (methodClass == null) {
            methodClass = Try.unsafe(() -> Class.forName(methodPath.substring(0, methodPath.lastIndexOf("."))));
        }

        methodName = methodPath.substring(methodPath.lastIndexOf(".") + 1);

        String finalMethodName = methodName;
        Class finalMethodClass = methodClass;
        Method method = Arrays.stream(requireNonNull(methodClass).getMethods()).filter(m -> m.getName().equals(finalMethodName)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Cannot find method " + finalMethodName + " in class " + finalMethodClass));

        if (gaugeObject == null && !Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Method " + finalMethodName + " must be static");
        }

        if (method.getParameterTypes().length != 0) {
            throw new IllegalArgumentException("Method " + finalMethodName + " must not have parameters");
        }

        Class<?> returnType = method.getReturnType();
        if (!(returnType.isAssignableFrom(Number.class) || (returnType.isPrimitive() && Arrays.asList("int", "long", "float", "double").contains(returnType.getName())))) {
            throw new IllegalArgumentException("Method " + finalMethodName + " must return a Number");
        }

        return method;
    }
}
