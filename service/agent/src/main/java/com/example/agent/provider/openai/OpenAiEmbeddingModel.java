package com.example.agent.provider.openai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.rag.document.TextSegment;
import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.core.rag.embedding.EmbeddingModel;
import com.example.agent.provider.openai.dto.request.OpenAiEmbeddingRequest;
import com.example.agent.provider.openai.parser.OpenAiEmbeddingResponseParser;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class OpenAiEmbeddingModel implements EmbeddingModel {
    private static final String DEFAULT_BASE_URL = "https://api.siliconflow.cn/v1";
    private static final String EMBEDDING_PATH = "/embeddings";
    private final String baseUrl;
    private final String apiKey;
    private final String modelName;
    private final Duration requestTimeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OpenAiEmbeddingResponseParser responseParser;
    private final int dimension;

    private OpenAiEmbeddingModel(Builder builder) {
        this.baseUrl = normalizeBaseUrl(builder.baseUrl);
        this.apiKey = builder.apiKey;
        this.modelName = builder.modelName;
        this.requestTimeout = builder.requestTimeout;
        this.dimension = builder.dimension;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(requestTimeout)
                .build();

        this.objectMapper = new ObjectMapper();
        this.responseParser = new OpenAiEmbeddingResponseParser(objectMapper);
    }

    @Override
    public Embedding embed(String text) {
        return embedTexts(List.of(text)).get(0);
    }

    @Override
    public List<Embedding> embeddingAll(List<TextSegment> segments) {

        if (segments == null || segments.isEmpty()) {
            return List.of();
        }

        List<String> inputs = new ArrayList<String>(segments.size());

        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            if (segment == null) {
                throw new IllegalArgumentException("segments[" + i + "]不能为空");
            }
            inputs.add(segment.text());
        }
        return embedTexts(inputs);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String baseUrl = DEFAULT_BASE_URL;
        private String apiKey;
        private String modelName;
        private Duration requestTimeout = Duration.ofSeconds(30);
        private int dimension = 1024;

        public Builder baseUrl(String baseUrl) {
            if (baseUrl == null || baseUrl.isBlank()) {
                throw new IllegalArgumentException("");
            }
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder apiKey(String apiKey) {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalArgumentException("");
            }
            this.apiKey = apiKey;
            return this;

        }

        public Builder modelName(String modelName) {
            if (modelName == null || modelName.isBlank()) {
                throw new IllegalArgumentException("");
            }
            this.modelName = modelName;
            return this;

        }

        public Builder requestTimeout(Duration requestTimeout) {
            if (requestTimeout == null || requestTimeout.isZero() || requestTimeout.isNegative()) {
                throw new IllegalArgumentException("");
            }
            this.requestTimeout = requestTimeout;
            return this;

        }

        public OpenAiEmbeddingModel build() {
            if (apiKey == null) {
                throw new IllegalStateException(" 必须装配 apiKey");
            }

            if (modelName == null) {
                throw new IllegalStateException("必须装配 modelName");
            }

            return new OpenAiEmbeddingModel(this);
        }

        public Builder dimension(int dimension) {
            this.dimension = dimension;
            return this;
        }

    }

    private static String normalizeBaseUrl(String baseUrl) {
        String normalized = baseUrl.trim();

        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(
                    0, normalized.length() - 1);
        }

        return normalized;
    }

    private List<Embedding> embedTexts(List<String> inputs) {
        validateInputs(inputs);

        OpenAiEmbeddingRequest requestBody = new OpenAiEmbeddingRequest(
                modelName,
                List.copyOf(inputs),
                "float",
                this.dimension);

        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + EMBEDDING_PATH))
                    .timeout(requestTimeout)
                    .header(
                            "Authorization",
                            "Bearer " + apiKey)
                    .header(
                            "Content-Type",
                            "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            jsonBody,
                            StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new OpenAiEmbeddingException(
                        "Embedding 请求被失败,status="
                                + response.statusCode()
                                + ",body="
                                + response.body());
            }

            List<Embedding> embeddings = responseParser.parse(response.body());

            if (embeddings.size() != inputs.size()) {
                throw new OpenAiEmbeddingException(
                        "Embedding 返回数量与输入数量不一致，input="
                                + inputs.size()
                                + ",output="
                                + embeddings.size());
            }

            return embeddings;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenAiEmbeddingException("Embedding 请求中断", e);
        } catch (IOException e) {
            throw new OpenAiEmbeddingException("Embedding HTTP 请求失败", e);
        }

    }

    public int dimension() {
        return dimension;
    }

    private void validateInputs(List<String> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException("inputs 不能为空");
        }

        for (int i = 0; i < inputs.size(); i++) {
            String input = inputs.get(i);

            if (input == null || input.isBlank()) {
                throw new IllegalArgumentException("inputs[" + i + "]不能为空");
            }
        }
    }
}
