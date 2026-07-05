package com.example.agent.core.message;

public class ToolMessage extends ChatMessage {
    private final String name;
    private final String toolCallId;

    public ToolMessage(String toolCallId, String name, String content) {
        super("tool", content);
        this.name = name;
        this.toolCallId = toolCallId;
    }

    @Override
    public String role() {
        return "tool";
    }

    public String name() {
        return name;
    }

    public String toolCallId() {
        return toolCallId;
    }
}
