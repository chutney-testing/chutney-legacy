package com.chutneytesting.action.micrometer;

import static com.chutneytesting.action.micrometer.MicrometerActionHelper.toOutputs;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.action.spi.validation.Validator.of;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.validation.Validator;
import com.chutneytesting.tools.Try;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MicrometerGaugeAction implements Action {

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

    public MicrometerGaugeAction(Logger logger,
                               @Input("name") String name,
                               @Input("description") String description,
                               @Input("unit") String unit,
                               @Input("strongReference") Boolean strongReference,
                               @Input("tags") List<String> tags,
                               @Input("gaugeObject") Object gaugeObject,
                               @Input("gaugeFunction") String gaugeFunction,
                               @Input("registry") MeterRegistry registry) {
        this.logger = logger;
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.strongReference = ofNullable(strongReference).orElse(Boolean.FALSE);
        this.tags = tags;
        this.gaugeObject = gaugeObject;
        this.gaugeFunction = gaugeFunction;
        this.registry = ofNullable(registry).orElse(globalRegistry);
    }

    @Override
    public List<String> validateInputs() {
        Validator<Object> gaugeValidation = of(null)
            .validate(a -> gaugeObject != null || gaugeFunction != null, "gaugeObject and gaugeFunction cannot be both null");

        Validator<Object> gaugeObjectValidation = of(gaugeObject);
        if (gaugeObject != null && gaugeFunction == null) {
            gaugeObjectValidation
                .validate(go -> go instanceof Number || go instanceof Collection || go instanceof Map, "gaugeObject must be a Number, a Collection or a Map if no gaugeFunction supplied");
        }
        return getErrorsFrom(notBlankStringValidation(name, "name"), gaugeValidation, gaugeObjectValidation);
    }

    @Override
    public ActionExecutionResult execute() {
        try {
            Gauge gauge = this.retrieveGauge(registry);
            logger.info("Gauge current value is " + gauge.value());
            return ActionExecutionResult.ok(toOutputs(OUTPUT_GAUGE, gaugeObject));
        } catch (Exception e) {
            logger.error(e);
            return ActionExecutionResult.ko();
        }
    }

    private Gauge retrieveGauge(MeterRegistry registry) {
        Gauge.Builder<?> builder = createGaugeBuilder()
            .description(description)
            .strongReference(strongReference)
            .baseUnit(unit);

        ofNullable(tags).ifPresent(t -> builder.tags(t.toArray(new String[0])));

        return builder.register(registry);
    }

    private Gauge.Builder<?> createGaugeBuilder() {
        if (gaugeObject != null && gaugeFunction == null) {
            if (gaugeObject instanceof Number) {
                return Gauge.builder(name, (Number) gaugeObject, Number::doubleValue);
            } else if (gaugeObject instanceof Collection) {
                return Gauge.builder(name, (Collection) gaugeObject, Collection::size);
            } else if (gaugeObject instanceof Map) {
                return Gauge.builder(name, (Map) gaugeObject, Map::size);
            }
        } else if (gaugeObject != null) {
            Method method = retrieveMethod(gaugeFunction, gaugeObject.getClass());
            return Gauge.builder(name, gaugeObject, o -> {
                try {
                    return ((Number) method.invoke(o)).doubleValue();
                } catch (Exception e) {
                    return Double.MIN_VALUE;
                }
            });
        } else {
            Method method = retrieveMethod(gaugeFunction, null);
            return Gauge.builder(name, () -> {
                try {
                    return ((Number) method.invoke(null)).doubleValue();
                } catch (Exception e) {
                    return Double.MIN_VALUE;
                }
            });
        }
        throw new IllegalStateException("Should not happen");
    }

    private Method retrieveMethod(final String methodPath, final Class clazz) {
        if (clazz == null && !methodPath.contains(".")) {
            throw new IllegalArgumentException("Method " + methodPath + " cannot be resolved");
        }

        Class<?> methodClass = clazz;

        if (methodClass == null) {
            methodClass = Try.unsafe(() -> Class.forName(methodPath.substring(0, methodPath.lastIndexOf("."))));
        }

        String methodName = methodPath.substring(methodPath.lastIndexOf(".") + 1);

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
