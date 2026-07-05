package com.example.agent.core.request;

import java.util.List;

import com.example.agent.core.message.ChatMessage;
import com.example.agent.core.tool.ToolMetadata;

public final class ChatRequest {
    private final String model;
    private final String systemMessage;
    private final List<ChatMessage> messages;
    private final List<ToolMetadata> tools;
    private final Double temperature;
    private final Boolean enableThinking;
    private final Integer maxTokens;

    public static Builder builder() {
        return new Builder();
    }

    private ChatRequest(Builder builder) {
        this.model = builder.model;
        this.systemMessage = builder.systemMessage;
        this.messages = List.copyOf(builder.messages);
        this.tools = List.copyOf(builder.tools);
        this.temperature = builder.temperature;
        this.maxTokens = builder.maxTokens;
        this.enableThinking = builder.enableThinking;
    }

    public static final class Builder {
        private String model;
        private String systemMessage;
        private List<ChatMessage> messages = List.of();
        private List<ToolMetadata> tools = List.of();
        private Double temperature = 0.7;
        private Boolean enableThinking = true;
        private Integer maxTokens;

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder systemMessage(String systemMessage) {
            this.systemMessage = systemMessage;
            return this;
        }

        public Builder messages(List<ChatMessage> message) {
            if (message == null) {
                throw new IllegalArgumentException("message 不能为空");
            }
            this.messages = List.copyOf(message);
            return this;
        }

        public Builder tools(List<ToolMetadata> tools) {
            this.tools = tools == null
                    ? List.of()
                    : List.copyOf(tools);
            return this;
        }

        public Builder temperature(Double temperature) {
            if (temperature != null && (temperature < 0 || temperature > 2)) {
                throw new IllegalArgumentException(
                        "temperature 必须在 0 到 2 之间");
            }
            this.temperature = temperature == null
                    ? 0.7
                    : temperature;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            if (maxTokens != null && maxTokens <= 0) {
                throw new IllegalArgumentException(
                        "maxTokens 必须大于 0");
            }
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder enableThinking(Boolean enableThinking) {
            this.enableThinking = enableThinking;
            return this;
        }

        public ChatRequest build() {

            if (messages.isEmpty()) {
                throw new IllegalStateException(
                        "必须配置 messages");
            }
            return new ChatRequest(this);
        }
    }

    public ChatRequest withDefaultModel(String defaultModel) {

        if (this.model != null && !this.model.isBlank()) {

            return this;
        }
        if (defaultModel == null || defaultModel.isBlank()) {
            throw new IllegalArgumentException(
                    "defaultModel 不能为空");
        }

        return toBuilder()
                .model(defaultModel)
                .build();
    }

    public Builder toBuilder() {
        return builder()
                .model(model)
                .systemMessage(systemMessage)
                .messages(messages)
                .tools(tools)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .enableThinking(enableThinking);
    }

    public String model() {
        return model;
    }

    public String systemMessage() {
        return systemMessage;
    }

    public List<ChatMessage> messages() {
        return messages;
    }

    public List<ToolMetadata> tools() {
        return tools;
    }

    public Double temperature() {
        return temperature;
    }

    public Integer maxTokens() {
        return maxTokens;
    }

    public Boolean enableThinking() {
        return enableThinking;
    }

}
