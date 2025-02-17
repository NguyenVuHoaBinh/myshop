package viettel.telecom.backend.service.flow;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * A Spring Expression Language-based implementation of ConditionEvaluator.
 * It parses the given expression as SpEL and evaluates it against a Map context.
 */
@Service
public class SpelConditionEvaluator implements ConditionEvaluator {

    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Override
    public boolean evaluate(String expression, Map<String, Object> context) {
        try {
            // Create an evaluation context from the given map
            EvaluationContext evalContext = new StandardEvaluationContext(context);

            // Parse and evaluate the expression, expecting a Boolean result
            Boolean result = parser.parseExpression(expression).getValue(evalContext, Boolean.class);

            // Return true only if the parsed result is explicitly true
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            // If there's a parsing or runtime error, treat it as false or handle differently
            return false;
        }
    }
}
