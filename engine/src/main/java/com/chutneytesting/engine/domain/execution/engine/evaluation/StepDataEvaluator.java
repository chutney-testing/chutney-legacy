package com.chutneytesting.engine.domain.execution.engine.evaluation;

import static com.chutneytesting.engine.domain.execution.engine.evaluation.Strings.escapeForRegex;

import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.evaluation.SpelFunctions;
import com.chutneytesting.task.spi.injectable.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class StepDataEvaluator {

    private static final String EVALUATION_STRING_PREFIX = "${";
    private static final String EVALUATION_STRING_SUFFIX = "}";
    private static final String EVALUATION_STRING_ESCAPE = "\\";
    private static final Pattern EVALUATION_OBJECT_PATTERN = Pattern.compile("^(?:" + escapeForRegex(EVALUATION_STRING_ESCAPE) + ")?" + escapeForRegex(EVALUATION_STRING_PREFIX) + "(?:(?!" + escapeForRegex(EVALUATION_STRING_PREFIX) + ").)*" + escapeForRegex(EVALUATION_STRING_SUFFIX) + "$", Pattern.DOTALL);


    private final SpelFunctions spelFunctions;
    private final ExpressionParser parser = new SpelExpressionParser();

    public StepDataEvaluator(SpelFunctions spelFunctions) {
        this.spelFunctions = spelFunctions;
    }

    public Map<String, Object> evaluateNamedDataWithContextVariables(final Map<String, Object> data, final Map<String, Object> contextVariables) throws EvaluationException {
        Map<String, Object> evaluatedNamedData = new LinkedHashMap<>();

        StandardEvaluationContext evaluationContext = buildEvaluationContext(contextVariables);

        data.forEach(
            (dataName, dataValue) -> {
                Object value = evaluateObject(dataValue, evaluationContext);
                evaluatedNamedData.put(dataName, value);
                evaluationContext.setVariable(dataName, value);
            }
        );
        return evaluatedNamedData;
    }

    public Target evaluateTarget(final Target target, final Map<String, Object> contextVariables) throws EvaluationException {
        TargetImpl.TargetBuilder builder = TargetImpl.builder();

        StandardEvaluationContext evaluationContext = buildEvaluationContext(contextVariables);

        builder.withName((String) evaluateObject(target.name(), evaluationContext));
        builder.withUrl((String) evaluateObject(target.rawUri(), evaluationContext));
        builder.withProperties((Map<String,String>) evaluateObject(target.prefixedProperties(""), evaluationContext));
        return builder.build();
    }

    private StandardEvaluationContext buildEvaluationContext(Map<String, Object> contextVariables) {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.registerMethodFilter(Runtime.class, methods -> Collections.emptyList());
        evaluationContext.registerMethodFilter(ProcessBuilder.class, methods -> Collections.emptyList());

        if (spelFunctions != null) {
            spelFunctions.stream().forEach(f -> evaluationContext.registerFunction(f.getName(), f.getMethod()));
        }
        evaluationContext.setVariables(contextVariables);
        return evaluationContext;
    }

    @SuppressWarnings("unchecked")
    private Object evaluateObject(final Object object, final EvaluationContext evaluationContext) throws EvaluationException {
        Object inputEvaluatedValue;
        if (object instanceof String) {
            String stringValue = (String) object;
            if (isObjectEvaluation(stringValue)) {
                inputEvaluatedValue = Strings.replaceExpression(stringValue, s -> evaluate(parser, evaluationContext, s), EVALUATION_STRING_PREFIX, EVALUATION_STRING_SUFFIX, EVALUATION_STRING_ESCAPE);
            } else {
                inputEvaluatedValue = Strings.replaceExpressions(stringValue, s -> evaluate(parser, evaluationContext, s), EVALUATION_STRING_PREFIX, EVALUATION_STRING_SUFFIX, EVALUATION_STRING_ESCAPE);
            }
        } else if (object instanceof Map) {
            Map evaluatedMap = new LinkedHashMap();
            ((Map) object).forEach(
                (key, value) -> {
                    Object keyValue = evaluateObject(key, evaluationContext);
                    Object valueValue = evaluateObject(value, evaluationContext);
                    evaluatedMap.put(keyValue, valueValue);
                    if (keyValue instanceof String) {
                        evaluationContext.setVariable((String) keyValue, valueValue);
                    }
                });
            inputEvaluatedValue = evaluatedMap;
        } else if (object instanceof List) {
            List evaluatedList = new ArrayList<>();
            ((List) object).forEach(
                obj -> evaluatedList.add(evaluateObject(obj, evaluationContext))
            );
            inputEvaluatedValue = evaluatedList;
        } else if (object instanceof Set) {
            Set evaluatedSet = new LinkedHashSet();
            ((Set) object).forEach(
                obj -> evaluatedSet.add(evaluateObject(obj, evaluationContext))
            );
            inputEvaluatedValue = evaluatedSet;
        } else {
            inputEvaluatedValue = object;
        }

        return inputEvaluatedValue;
    }

    private Object evaluate(ExpressionParser parser, final EvaluationContext evaluationContext, String expressionAsString) throws EvaluationException {
        final Expression expression = parseExpression(parser, expressionAsString);

        try {
            Object result = expression.getValue(evaluationContext);
            if (result == null) {
                throw new EvaluationException("Cannot resolve " + expressionAsString + ", Spring evaluation is null");
            }
            return result;
        } catch (org.springframework.expression.EvaluationException e) {
            Exception initialException = e;
            if (initialException.getCause() != null && initialException.getCause() instanceof InvocationTargetException) {
                initialException = (InvocationTargetException) e.getCause();
                if (initialException.getCause() != null) {
                    initialException = (Exception) initialException.getCause();
                }
            }
            throw new EvaluationException("Cannot resolve " + expressionAsString + " , " + initialException.getMessage(), initialException);
        }
    }

    private boolean isObjectEvaluation(String template) {
        return EVALUATION_OBJECT_PATTERN.matcher(template.trim()).matches();
    }

    private Expression parseExpression(ExpressionParser parser, String expressionAsString) {
        Expression expression;
        try {
            expression = parser.parseExpression(expressionAsString);
        } catch (ParseException e) {
            throw new EvaluationException("Cannot parse " + expressionAsString + " , " + e.getMessage(), e);
        }
        return expression;
    }
}
