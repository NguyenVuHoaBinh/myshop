package viettel.telecom.backend.service.llm;

import java.util.List;

public enum ModelType {
    // All acceptable variants for OpenAI are mapped to the canonical key "openai"
    OPENAI("openai", List.of("gpt-4o", "gpt-4o-mini", "gpt-o3-mini")),
    DEEPSEEK("deepseek", List.of("deepseek-chat","deepseek-reasoner"));

    private final String key;
    private final List<String> aliases;

    ModelType(String key, List<String> aliases) {
        this.key = key;
        this.aliases = aliases;
    }

    public String getKey() {
        return key;
    }

    /**
     * Resolve an input string to its canonical model type key.
     * Returns null if no matching type is found.
     */
    public static String resolve(String input) {
        if (input == null) return null;
        String lower = input.toLowerCase();
        for (ModelType mt : ModelType.values()) {
            // Check if the input matches the canonical key or any alias
            if (mt.key.equals(lower) || mt.aliases.contains(lower)) {
                return mt.key;
            }
        }
        return null;
    }
}

