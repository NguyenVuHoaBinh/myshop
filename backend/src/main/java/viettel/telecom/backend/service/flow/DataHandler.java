package viettel.telecom.backend.service.flow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import viettel.telecom.backend.entity.flow.Flow;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DataHandler {

    private final RestTemplate restTemplate;

    public DataHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Executes a "DATA" node by:
     *  1) Extracting 'requestUrl' and 'requestBody' from NodeData
     *  2) Resolving placeholders from 'context'
     *  3) POSTing to the specified URL
     *  4) Returning the next node ID (onSuccess or onError)
     *
     * @param node    The current flow node
     * @param context A map storing dynamic data for placeholders
     * @return        The ID of the next node in the flow
     */
    public String handle(Flow.Node node, Map<String, Object> context) {
        // If your engine calls this only for "DATA" nodes, fine.
        // Otherwise, you can check if (node.getType().equals("DATA")) at runtime.

        Flow.Node.NodeData nodeData = node.getData();
        if (nodeData == null) {
            log.error("NodeData is null for node ID: {}", node.getId());
            return "END";
        }

        String requestUrl = nodeData.getRequestUrl();
        Map<String, Object> rawBody = nodeData.getRequestBody();

        String successNode = nodeData.getOnSuccessNextNode();
        String errorNode = nodeData.getOnErrorNextNode();

        try {
            // Validate we have a URL and body
            if (requestUrl == null) {
                log.error("Data node missing 'requestUrl' (Node ID: {})", node.getId());
                return fallbackNode(errorNode);
            }
            if (rawBody == null) {
                log.error("Data node missing 'requestBody' (Node ID: {})", node.getId());
                return fallbackNode(errorNode);
            }

            // Resolve placeholders
            String resolvedUrl = resolvePlaceholdersInString(requestUrl, context);
            Map<String, Object> resolvedBody = resolvePlaceholdersInMap(rawBody, context);

            log.info("Data Node => POST {} with body {}", resolvedUrl, resolvedBody);

            // Make the POST request; expecting JSON -> Map
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    resolvedUrl,
                    resolvedBody,
                    Map.class
            );

            // Optionally store the response in context, e.g., "lastResponse"
            if (response != null) {
                context.put("lastResponse", response);
            }

            // On success, use onSuccessNextNode or node.getNext()
            if (successNode != null && !successNode.isEmpty()) {
                return successNode;
            }
            // Fallback to 'next' if present, else "END"
            return (node.getNext() != null && !node.getNext().isEmpty())
                    ? node.getNext()
                    : "END";

        } catch (Exception ex) {
            log.error("Error in DataHandler for node {}: {}", node.getId(), ex.getMessage(), ex);
            return fallbackNode(errorNode);
        }
    }

    /**
     * Helper to return the fallback node on error or "END" if none set.
     */
    private String fallbackNode(String errorNode) {
        return (errorNode != null && !errorNode.isEmpty()) ? errorNode : "END";
    }

    /**
     * Recursively resolves placeholders [xxx] in a map.
     */
    private Map<String, Object> resolvePlaceholdersInMap(
            Map<String, Object> rawMap,
            Map<String, Object> context
    ) {
        Map<String, Object> resolved = new HashMap<>();
        for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                // Replace placeholders in strings
                resolved.put(entry.getKey(), resolvePlaceholdersInString((String) value, context));
            } else if (value instanceof Map) {
                // Recurse into nested maps
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                resolved.put(entry.getKey(), resolvePlaceholdersInMap(nestedMap, context));
            } else {
                // Arrays, booleans, numbers remain unchanged
                resolved.put(entry.getKey(), value);
            }
        }
        return resolved;
    }

    /**
     * Replaces any [key] placeholders in 'raw' with values from 'context'.
     * If the context doesn't have 'key', we replace with "" (empty string) by default.
     */
    private String resolvePlaceholdersInString(String raw, Map<String, Object> context) {
        if (raw == null) return null;

        String patternStr = "\\[([^\\]]+)]"; // e.g. "[groupName]"
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(raw);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String placeholder = matcher.group(1); // e.g. "groupName"
            Object replacement = context.getOrDefault(placeholder, "");
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
