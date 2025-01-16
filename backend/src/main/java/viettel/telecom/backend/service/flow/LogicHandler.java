package viettel.telecom.backend.service.flow;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import viettel.telecom.backend.entity.flow.Flow;

import java.util.Map;

@Service
public class LogicHandler {

    public String handle(Flow.Node node, Map<String, Object> context) {
        // Retrieve condition from the node
        String condition = node.getData().getBotResponse(); // Assuming `botResponse` stores the condition for logic nodes

        if (condition != null) {
            try {
                // Set up the SpEL parser and evaluation context
                SpelExpressionParser parser = new SpelExpressionParser();
                EvaluationContext evalContext = new StandardEvaluationContext(context);

                // Evaluate the condition
                boolean conditionMet = parser.parseExpression(condition).getValue(evalContext, Boolean.class);

                // Return the appropriate next step ID based on the evaluation
                return conditionMet ? node.getData().getTemplateId() : node.getData().getLabel();
            } catch (Exception e) {
                // Fallback if condition evaluation fails
                return node.getData().getLabel();
            }
        }

        // If no condition is provided, proceed to the next step by default
        return node.getData().getTemplateId();
    }
}
