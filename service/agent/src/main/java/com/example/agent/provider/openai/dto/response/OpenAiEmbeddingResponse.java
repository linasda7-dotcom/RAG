package com.example.agent.provider.openai.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiEmbeddingResponse(
        String object,
        List<OpenAiEmbeddingData> data,
        String model,
        OpenAiEmbeddingUsage usage) {

}
