package com.example.agent.core.model;

import java.util.List;

import com.example.agent.core.tool.ToolCall;

public record ChatModelResponse(
        String content,
        List<ToolCall> toolCalls,
        String finishReason) {
    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    public String contentOrEmpty() {
        return content == null ? "" : content;
    }

    // 普通回复
    public static ChatModelResponse content(String content) {
        return new ChatModelResponse(content, List.of(), "stop");
    }

    // 工具调用回复
    public static ChatModelResponse toolCall(String content, List<ToolCall> toolCalls) {
        return new ChatModelResponse(content, toolCalls, "tool_calls");
    }
}
