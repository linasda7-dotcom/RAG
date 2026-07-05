package com.example.agent.core.message;

import java.util.List;

import com.example.agent.core.tool.ToolCall;

public class AssistantToolCallMessage extends ChatMessage {
    private final List<ToolCall> toolCalls;

    public AssistantToolCallMessage(String content, List<ToolCall> toolCalls) {
        super("assistant", content);
        this.toolCalls = toolCalls;
    }

    @Override
    public String role() {
        return "assistant";
    }

    public List<ToolCall> toolCalls() {
        return toolCalls;
    }

}
