package com.example.agent.provider.openai.dto.request;

public record OpenAiFunction(
        String name,
        String description,
        OpenAiParameters parameters) {

}
