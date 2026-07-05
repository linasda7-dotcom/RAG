package com.example.agent.provider.openai.dto.request;

public record OpenAiRequestToolCall(
        String id, String type, OpenAiRequestToolFunction function) {

}
