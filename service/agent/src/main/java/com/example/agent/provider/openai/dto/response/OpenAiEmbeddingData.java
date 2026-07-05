package com.example.agent.provider.openai.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiEmbeddingData(
        String object,
        List<Float> embedding,
        int index) {

}
