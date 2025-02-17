package viettel.telecom.backend.service.flow;

import org.springframework.stereotype.Service;
import viettel.telecom.backend.entity.flow.Flow;
import viettel.telecom.backend.entity.flow.Flow.Node;
import viettel.telecom.backend.entity.flow.LogicCase;

import java.util.List;
import java.util.Map;

/**
 * Evaluates a "logicNode" with multiple expressions (cases).
 * The first matching expression determines the next node ID.
 */
@Service
public class LogicHandler {

    private final ConditionEvaluator conditionEvaluator;

    public LogicHandler(ConditionEvaluator conditionEvaluator) {
        this.conditionEvaluator = conditionEvaluator;
    }

    /**
     * Goes through each logicCase. Evaluates its expression (SpEL)
     * against the context map. The first that matches => returns that nextNode.
     * If none match, returns node.getNext() as a fallback.
     */
    public String handle(Node node, Map<String, Object> context) {
        if (node.getData() == null || node.getData().getLogicCases() == null) {
            // No logic cases => fallback to node.getNext()
            return node.getNext();
        }

        List<LogicCase> logicCases = node.getData().getLogicCases();
        for (LogicCase logicCase : logicCases) {
            String expression = logicCase.getExpression();
            if (expression == null || expression.isEmpty()) {
                continue; // skip blank expressions
            }

            boolean matched = conditionEvaluator.evaluate(expression, context);
            if (matched) {
                // Return the nextNode for this case
                return logicCase.getNextNode();
            }
        }

        // If no expression matched, fallback to node.getNext()
        return node.getNext();
    }
}
