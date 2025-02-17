package viettel.telecom.backend.service.flow;

import java.util.Map;

/**
 * Defines an interface for evaluating a boolean expression against a context.
 */
public interface ConditionEvaluator {
    /**
     * Evaluate a boolean expression (SpEL, MVEL, or any other syntax)
     * against the given context map.
     *
     * @param expression The conditional expression as a String
     *                   (e.g., "context['userInput'] == 'Hello'")
     * @param context    A map of runtime variables/fields used in evaluation.
     * @return true if the expression evaluates to true; false otherwise
     */
    boolean evaluate(String expression, Map<String, Object> context);
}
