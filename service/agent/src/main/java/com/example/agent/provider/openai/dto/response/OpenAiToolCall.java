package com.example.agent.provider.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiToolCall(
        String id,
        String type,
        OpenAiToolFunction function) {
}
