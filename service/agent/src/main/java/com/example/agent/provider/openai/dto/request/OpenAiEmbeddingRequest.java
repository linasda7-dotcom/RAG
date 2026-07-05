package com.example.agent.provider.openai.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenAiEmbeddingRequest(
                String model,
                List<String> input,
                @JsonProperty("encoding_format") String encodingFormat,
                int dimensions) {
}
