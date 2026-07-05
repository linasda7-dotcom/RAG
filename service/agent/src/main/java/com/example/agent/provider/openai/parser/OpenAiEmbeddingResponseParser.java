package com.example.agent.provider.openai.parser;

import java.util.Comparator;
import java.util.List;

import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.provider.openai.dto.response.OpenAiEmbeddingData;
import com.example.agent.provider.openai.dto.response.OpenAiEmbeddingResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class OpenAiEmbeddingResponseParser {
    private final ObjectMapper objectMapper;

    public OpenAiEmbeddingResponseParser(ObjectMapper objectMapper) {

        if (objectMapper == null) {
            throw new IllegalArgumentException("objectMapper 不能为空");
        }

        this.objectMapper = objectMapper;
    }

    public List<Embedding> parse(String responseBody) {

        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalArgumentException("responseBody 不能为空");
        }

        try {
            OpenAiEmbeddingResponse response = objectMapper.readValue(responseBody, OpenAiEmbeddingResponse.class);

            if (response.data() == null || response.data().isEmpty()) {
                throw new IllegalStateException("Embedding 响应中没有 data");
            }

            return response.data()
                    .stream()
                    .sorted(Comparator.comparingInt(
                            embeddingData -> embeddingData.index()))
                    .map(this::toEmbedding)
                    .toList();

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("解析 Embedding 响应失败");
        }

    }

    private Embedding toEmbedding(OpenAiEmbeddingData data) {
        if (data.embedding() == null || data.embedding().isEmpty()) {
            throw new IllegalStateException(
                    "Embedding vector 不能为空");
        }

        float[] vector = new float[data.embedding().size()];

        for (int i = 0; i < vector.length; i++) {
            Float value = data.embedding().get(i);
            if (value == null) {
                throw new IllegalStateException(
                        "Embedding vector 不能包含 null");
            }
            vector[i] = value;
        }
        return new Embedding(vector);
    }
}
