package com.example.agent.provider.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiToolFunction(
        String name, String arguments) {
}
