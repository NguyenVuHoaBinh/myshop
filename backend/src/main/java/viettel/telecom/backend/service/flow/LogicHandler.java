package viettel.telecom.backend.service.flow;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import viettel.telecom.backend.entity.flow.Flow;

import java.util.Map;

@Service
public class LogicHandler {

    public String handle(Flow.Step step, Map<String, Object> context) {
        String condition = step.getCondition();

        if (condition != null) {
            try {
                SpelExpressionParser parser = new SpelExpressionParser();
                EvaluationContext evalContext = new StandardEvaluationContext(context);

                boolean conditionMet = parser.parseExpression(condition).getValue(evalContext, Boolean.class);
                return conditionMet ? step.getNextStepId() : step.getFallbackStepId();
            } catch (Exception e) {
                return step.getFallbackStepId(); // Default to fallback if condition evaluation fails
            }
        }
        return step.getNextStepId(); // Proceed by default if no condition
    }
}
