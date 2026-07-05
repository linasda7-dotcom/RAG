package com.example.agent.provider.openai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.example.agent.core.model.ChatModel;
import com.example.agent.core.model.ChatModelResponse;
import com.example.agent.core.request.ChatRequest;
import com.example.agent.provider.openai.dto.request.OpenAiChatRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OpenAiChatModel implements ChatModel {
    private static final Logger log = LoggerFactory.getLogger(OpenAiChatModel.class);
    private static final String DEFAULT_BASE_URL = "https://api.siliconflow.cn/v1";
    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    private Duration requestTimeout = Duration.ofSeconds(60);

    private final HttpClient httpClient;
    private final OpenAiChatRequestBuilder requestBuilder;
    private final OpenAiChatResponseParser responseParser;

    private OpenAiChatModel(Builder builder) {
        this.baseUrl = normalizeBaseUrl(builder.baseUrl);
        this.apiKey = builder.apiKey;
        this.model = builder.model;

        this.httpClient = builder.httpClient;
        this.requestBuilder = builder.requestBuilder;
        this.responseParser = builder.responseParser;
        this.requestTimeout = builder.requestTimeout;
    }

    public final static Builder builder() {
        return new Builder();
    }

    @Override
    public ChatModelResponse chat(ChatRequest message) {
        ChatRequest actualRequest = withDefaultModel(message);

        OpenAiChatRequest openAiChatRequest = requestBuilder.build(actualRequest);
        String requestBody = toJson(openAiChatRequest);

        log.info("OpenAI request prepared, model={}, messages={}, systemMessagePresent={}, requestBodySnippet='{}'",
                actualRequest.model(),
                actualRequest.messages() == null ? 0 : actualRequest.messages().size(),
                actualRequest.systemMessage() != null && !actualRequest.systemMessage().isBlank(),
                requestBody.replaceAll("\n", " ").substring(0, Math.min(1024, requestBody.length())));
        if (log.isDebugEnabled()) {
            log.debug("OpenAI request body={} ", requestBody);
        }

        HttpRequest httpRequest;
        try {
            httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + CHAT_COMPLETIONS_PATH))
                    .timeout(requestTimeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode < 200 || statusCode >= 300) {
                throw new RuntimeException("""
                        OpenAI HTTP 调用失败
                        statusCode: %s
                        responsesBody: %s
                        """.formatted(statusCode, responseBody));
            }

            ChatModelResponse chatModelResponse = responseParser.parse(responseBody);
            return chatModelResponse;
        } catch (HttpTimeoutException e) {
            throw new RuntimeException("""
                    OpenAI HTTP 请求超时
                    baseUrl:%s
                    model:%s
                    timeout:%s
                    """.formatted(baseUrl, model, requestTimeout), e);
        } catch (IOException e) {
            throw new RuntimeException("""
                    OpenAI HTTP 请求失败
                    BaseUrl:%s
                    model:%s
                    reason:%s
                    """.formatted(baseUrl, model, e.getMessage()), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("OpenAI HTTP 请求被中断", e);
        }

    }

    private ChatRequest withDefaultModel(ChatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("ChatRequest 不能为空");
        }
        if (request.model() != null && !request.model().isBlank()) {
            return request;
        }
        return ChatRequest.builder()
                .model(model)
                .systemMessage(request.systemMessage())
                .messages(request.messages())
                .tools(request.tools())
                .temperature(request.temperature())
                .maxTokens(request.maxTokens())
                .enableThinking(request.enableThinking())
                .build();
    }

    private String toJson(OpenAiChatRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("OpenAI 请求体序列化失败", e);
        }
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return DEFAULT_BASE_URL;
        }
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    public static class Builder {
        private String baseUrl = DEFAULT_BASE_URL;
        private String apiKey;
        private String model;
        private Duration requestTimeout = Duration.ofSeconds(60);

        private HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        private OpenAiChatRequestBuilder requestBuilder = new OpenAiChatRequestBuilder();
        private OpenAiChatResponseParser responseParser = new OpenAiChatResponseParser();

        public Builder baseUrl(String baseUrl) {
            if (baseUrl != null && !baseUrl.isBlank()) {
                this.baseUrl = baseUrl;
            }
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder requestBuilder(OpenAiChatRequestBuilder requestBuilder) {
            this.requestBuilder = requestBuilder;
            return this;
        }

        public Builder requestTimeout(Duration requestTimeout) {
            if (requestTimeout != null) {
                this.requestTimeout = requestTimeout;
            }
            return this;
        }

        public Builder responseParser(OpenAiChatResponseParser responseParser) {
            this.responseParser = responseParser;
            return this;
        }

        public OpenAiChatModel build() {
            if (apiKey == null || apiKey.isBlank()) {
                throw new RuntimeException("apiKey 不能为空");
            }

            if (model == null || model.isBlank()) {
                throw new RuntimeException("model不能为空");
            }

            return new OpenAiChatModel(this);
        }
    }

    public static String getDefaultBaseUrl() {
        return DEFAULT_BASE_URL;
    }

    public static String getChatCompletionsPath() {
        return CHAT_COMPLETIONS_PATH;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public OpenAiChatRequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

    public OpenAiChatResponseParser getResponseParser() {
        return responseParser;
    }

    @Override
    public String modelName() {
        return model;
    }
}
