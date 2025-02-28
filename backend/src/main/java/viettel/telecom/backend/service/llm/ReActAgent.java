package viettel.telecom.backend.service.llm;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import viettel.telecom.backend.agent.AgentTool;
import viettel.telecom.backend.agent.ToolResult;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Main orchestrator that implements a ReAct-style loop:
 *  1. Build a prompt with chain-of-thought
 *  2. Call LLM to get next action or final answer
 *  3. If tool is requested, call the tool, append result to chain-of-thought
 *  4. Repeat until final answer
 */
@Service
@Slf4j
public class ReActAgent {

    private final List<AgentTool> tools;
    private final LLMService llmService;  // <-- We'll use this to actually call the OpenAI / other LLM

    // For parsing the "Action Input" JSON in the LLM output
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * The constructor. Spring will inject:
     *  1) A list of all AgentTool beans
     *  2) The LLMService bean
     */
    public ReActAgent(List<AgentTool> tools, LLMService llmService) {
        this.tools = tools;
        this.llmService = llmService;
    }

    /**
     * Primary entry point: user input => final answer (ReAct loop).
     *
     * @param userQuery the user's question or command
     * @param context   a context map for storing relevant variables, session info, etc.
     */
    public String runAgent(String userQuery, Map<String,Object> context) {
        // We'll store the ReAct chain-of-thought here. In a real system, keep it hidden from the user.
        String chainOfThought = "";

        // The loop continues until we find a final answer or an error.
        while (true) {
            // 1) Build a prompt that includes chain-of-thought + user query
            String prompt = buildPrompt(userQuery, chainOfThought);

            // 2) Call the LLM to see next step (action or final)
            String llmOutput = callLLM(prompt);  // <-- We'll implement this with LLMService
            log.debug("LLM Output:\n{}", llmOutput);

            // 3) Parse the LLM output to see if there's an action or final answer
            ReActParsingResult parsed = parseReActOutput(llmOutput);

            if (parsed.isFinalAnswer()) {
                // Return final to user
                log.debug("Final answer found -> {}", parsed.getFinalAnswer());
                return parsed.getFinalAnswer();
            }

            // If an action is requested, find the tool
            AgentTool chosenTool = findToolByName(parsed.getActionName());
            if (chosenTool == null) {
                // No known tool => append error to chain-of-thought or just exit
                chainOfThought += "\n[Agent: Unknown tool \"" + parsed.getActionName() + "\"]";
                return "I'm sorry, I don't know how to perform the requested action: "
                        + parsed.getActionName();
            }

            // 4) Execute the tool
            //   The input to the tool is in parsed.getActionInput().
            //   We'll merge that into context so the tool can read it.
            Map<String,Object> actionInput = parsed.getActionInput();
            if (actionInput != null) {
                context.putAll(actionInput);
            }

            ToolResult result = chosenTool.executeTool(context);

            // 5) Append the result to chainOfThought (for next iteration)
            chainOfThought += "\nTool '" + parsed.getActionName()
                    + "' output: " + result.getOutput();

            // Then the loop continues with an updated chainOfThought
        }
    }

    /**
     * Construct the ReAct style prompt, including system instructions,
     * chain-of-thought so far, and user query.
     */
    private String buildPrompt(String userQuery, String chainOfThought) {
        return
                "You are a ReAct agent. You have these tools:\n" +
                        "1) DATA_HANDLER\n2) LLM_HANDLER\n3) FLOW_EXECUTOR\n\n" +
                        "Follow the format:\n" +
                        "Thought: <your hidden chain-of-thought>\n" +
                        "Action: <ToolName>\n" +
                        "Action Input: <JSON input>\n" +
                        "...\n" +
                        "Final Answer: <your answer>\n\n" +
                        "Chain of Thought so far:\n" + chainOfThought + "\n\n" +
                        "User query: " + userQuery + "\n" +
                        "Next step?\n";
    }

    /**
     * Call your LLM or external service via the LLMService.
     * This is the real version, no placeholder.
     */
    private String callLLM(String prompt) {
        // We'll treat the 'prompt' as user input, and also set a systemPrompt
        // that can contain ReAct instructions or anything else. For demonstration,
        // we keep it simple: systemPrompt can be a short role-based statement.
        String systemPrompt = "You are ChatGPT, a large language model using ReAct framework.";

        // Build a config map for the LLM.
        // You can set model, temperature, etc.
        // The "modelType" or "aiModel" will determine which ModelHandler is used.
        Map<String, Object> config = new HashMap<>();
        // If you want to specifically use your "openai" handler, set modelType to "openai":
        config.put("modelType", "openai");
        // Optionally, set the exact model name:
        config.put("aiModel", "gpt-4");
        config.put("temperature", 0.7);
        config.put("max_tokens", 500);
        config.put("stream", false);

        // Now call LLMService with systemPrompt, userInput (the ReAct prompt), and config
        Map<String, Object> result = llmService.processRequest("openai", systemPrompt, prompt, config);

        // Typically, OpenAI returns a structure like:
        // {
        //   "choices": [
        //     {
        //       "message": {
        //         "role": "assistant",
        //         "content": "Your output here..."
        //       }
        //     }
        //   ]
        // }
        //
        // So let's extract that content:
        if (result == null || !result.containsKey("choices")) {
            log.warn("LLM response has no 'choices' field => defaulting to empty string.");
            return "";
        }

        Object choicesObj = result.get("choices");
        if (!(choicesObj instanceof List)) {
            log.warn("LLM response 'choices' is not a list => defaulting to empty string.");
            return "";
        }
        List<?> choicesList = (List<?>) choicesObj;
        if (choicesList.isEmpty()) {
            log.warn("LLM response 'choices' list is empty => defaulting to empty string.");
            return "";
        }

        // We’ll look at the first choice
        Object firstChoice = choicesList.get(0);
        if (!(firstChoice instanceof Map)) {
            log.warn("First choice is not a map => defaulting to empty string.");
            return "";
        }
        Map<?,?> choiceMap = (Map<?,?>) firstChoice;
        Object messageObj = choiceMap.get("message");
        if (!(messageObj instanceof Map)) {
            log.warn("No 'message' found in first choice => defaulting to empty string.");
            return "";
        }
        Map<?,?> messageMap = (Map<?,?>) messageObj;
        Object contentObj = messageMap.get("content");
        if (!(contentObj instanceof String)) {
            log.warn("No 'content' string found in message => defaulting to empty string.");
            return "";
        }

        String content = (String) contentObj;
        log.debug("LLM returned content:\n{}", content);
        return content;
    }

    /**
     * Parse the LLM output to see which action is requested,
     * or if there's a "Final Answer."
     */
    private ReActParsingResult parseReActOutput(String llmOutput) {
        // 1) Check if there's a line starting with "Final Answer:"
        if (llmOutput.contains("Final Answer:")) {
            String answer = llmOutput.substring(llmOutput.indexOf("Final Answer:") + 13).trim();
            return new ReActParsingResult(true, null, null, answer);
        }

        // 2) Otherwise look for "Action:" lines
        String[] lines = llmOutput.split("\\r?\\n");
        String actionName = null;
        String actionInputJson = null;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("Action:")) {
                actionName = trimmed.substring("Action:".length()).trim();
            } else if (trimmed.startsWith("Action Input:")) {
                actionInputJson = trimmed.substring("Action Input:".length()).trim();
            }
        }

        // 3) If we found no action line and no final answer, we can treat it as final or an error
        if (actionName == null) {
            return new ReActParsingResult(
                    true,
                    null,
                    null,
                    "No action found. Possibly the LLM is confused. Exiting."
            );
        }

        // 4) Parse the Action Input JSON
        Map<String, Object> inputMap = new HashMap<>();
        if (actionInputJson != null && !actionInputJson.isEmpty()) {
            try {
                inputMap = MAPPER.readValue(actionInputJson, Map.class);
            } catch (Exception e) {
                log.warn("Failed to parse action input JSON: {}", e.getMessage());
                // store raw fallback
                inputMap.put("raw", actionInputJson);
            }
        }

        return new ReActParsingResult(false, actionName, inputMap, null);
    }

    private AgentTool findToolByName(String name) {
        for (AgentTool t : tools) {
            if (t.getToolName().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Inner class to hold parse results.
     */
    private static class ReActParsingResult {
        private final boolean finalAnswer;
        private final String actionName;
        private final Map<String,Object> actionInput;
        private final String finalAnswerContent;

        public ReActParsingResult(
                boolean finalAnswer,
                String actionName,
                Map<String, Object> actionInput,
                String finalAnswerContent
        ) {
            this.finalAnswer = finalAnswer;
            this.actionName = actionName;
            this.actionInput = actionInput;
            this.finalAnswerContent = finalAnswerContent;
        }

        public boolean isFinalAnswer() {
            return finalAnswer;
        }
        public String getActionName() {
            return actionName;
        }
        public Map<String, Object> getActionInput() {
            return actionInput;
        }
        public String getFinalAnswer() {
            return finalAnswerContent;
        }
    }
}

