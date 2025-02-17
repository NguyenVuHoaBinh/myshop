package viettel.telecom.backend.entity.flow;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Represents a single "case" (or branch) in a multi-branch logic node.
 * Each case has:
 *  - an expression (SpEL)
 *  - a nextNode (the node ID to go to if expression is true)
 *  - an optional label for display
 */
@Data
public class LogicCase {

    @Field(type = FieldType.Text)
    private String expression; // e.g., "context['userInput'] == 'Yes'"

    @Field(type = FieldType.Keyword)
    private String nextNode;   // Node ID to jump to if expression == true

    @Field(type = FieldType.Text)
    private String label;      // Optional label/description for this case
}
