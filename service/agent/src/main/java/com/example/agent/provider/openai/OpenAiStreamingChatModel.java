package com.example.agent.provider.openai;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.agent.core.model.StreamingChatModel;
import com.example.agent.core.model.StreamingResponseHandler;
import com.example.agent.core.request.ChatRequest;
import com.example.agent.provider.openai.OpenAiStreamParser.StreamChunk;
import com.example.agent.provider.openai.dto.request.OpenAiChatRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenAiStreamingChatModel implements StreamingChatModel {

    private static final String DEFAULT_BASE_URL = "https://api.siliconflow.cn/v1/chat/completions";

    private static final Logger log = LoggerFactory.getLogger(OpenAiStreamingChatModel.class);

    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final Duration requestTimeout;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OpenAiStreamParser streamParser;

    public OpenAiStreamingChatModel(Builder builder) {
        this.apiKey = builder.apiKey;
        this.model = builder.model;
        this.baseUrl = builder.baseUrl;
        this.requestTimeout = builder.requestTimeout;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(builder.requestTimeout)
                .build();

        this.objectMapper = new ObjectMapper();
        this.streamParser = new OpenAiStreamParser();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void chat(ChatRequest request, StreamingResponseHandler handler) {
        Objects.requireNonNull(request, "request不能为空");
        Objects.requireNonNull(handler, "handler不能为空");

        StringBuilder completeResponse = new StringBuilder();
        OpenAiStreamToolCallAccumulator toolCallAccumulator = new OpenAiStreamToolCallAccumulator();

        try {
            ChatRequest finalRequest = request.withDefaultModel(model);

            OpenAiChatRequestBuilder openAiChatRequestBuilder = new OpenAiChatRequestBuilder();
            OpenAiChatRequest openAiRequest = openAiChatRequestBuilder.build(finalRequest, true);

            String requestBody = objectMapper.writeValueAsString(openAiRequest);
            log.info(
                    "OpenAI streaming request prepared, model={}, messages={}, systemMessagePresent={}, requestBodySnippet='{}'",
                    finalRequest.model(),
                    finalRequest.messages() == null ? 0 : finalRequest.messages().size(),
                    finalRequest.systemMessage() != null && !finalRequest.systemMessage().isBlank(),
                    requestBody.replaceAll("\n", " ").substring(0, Math.min(1024, requestBody.length())));
            if (log.isDebugEnabled()) {
                log.debug("OpenAI streaming request body={}", requestBody);
            }

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            requestBody,
                            StandardCharsets.UTF_8));

            if (requestTimeout != null) {
                requestBuilder.timeout(requestTimeout);
            }

            // 发送请求
            HttpResponse<InputStream> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());

            // 当发生错误时
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new RuntimeException("OpenAI STream请求失败,status="
                        + response.statusCode()
                        + ",body="
                        + errorBody);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }

                    if (!line.startsWith("data:")) {
                        continue;
                    }

                    String data = line.substring("data:".length()).trim();

                    // 得到响应块
                    StreamChunk chunk = streamParser.parse(data);
                    if (chunk.isDone()) {
                        break;
                    }

                    if (chunk.isToolCallDelta()) {
                        toolCallAccumulator.append(chunk.toolCalls());
                        continue;
                    }

                    if (chunk.isReasoning()) {
                        handler.onPartialReasoning(chunk.content());
                        continue;
                    }

                    if (chunk.isContent()) {
                        completeResponse.append(chunk.content());
                        handler.onPartialResponse(chunk.content());
                    }

                }
            }

            if (toolCallAccumulator.hasToolCalls()) {
                handler.onToolCalls(toolCallAccumulator.toToolCalls());
            }

            handler.onCompleteResponse(completeResponse.toString());
        } catch (Throwable e) {
            handler.onError(e);
        }
    }

    public static class Builder {
        private String apiKey;
        private String model;
        private String baseUrl = DEFAULT_BASE_URL;
        private Duration requestTimeout = Duration.ofSeconds(60);

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        public OpenAiStreamingChatModel build() {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalArgumentException("apiKey不能为空");
            }

            if (model == null || model.isBlank()) {
                throw new IllegalArgumentException("model不能为空");
            }

            if (baseUrl == null || baseUrl.isBlank()) {
                throw new IllegalArgumentException("baseUrl不能为空");
            }

            return new OpenAiStreamingChatModel(this);
        }
    }

    @Override
    public String modelName() {
        return model;
    }
}
