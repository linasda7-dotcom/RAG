package com.example.agent.provider.openai.dto.request;

public record OpenAiRequestToolFunction(
        String name, String arguments) {
}
