package com.example.agent.provider.openai.dto.stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiStreamToolFunction(
        String name,
        String arguments) {

}
