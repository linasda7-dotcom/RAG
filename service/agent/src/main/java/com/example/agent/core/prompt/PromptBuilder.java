package com.example.agent.core.prompt;

import java.util.List;

import com.example.agent.core.message.ChatMessage;

import com.example.agent.core.request.ChatRequest;
import com.example.agent.core.tool.ToolMetadata;

/**
 * 这个类只负责构造ChatRequest请求并不负责存储消息
 */
public class PromptBuilder {

    public ChatRequest buildRequest(
            String model,
            String systemMessage,
            List<ChatMessage> messages,
            List<ToolMetadata> toolMetadata,
            Double temperature,
            Integer maxTokens,
            Boolean enableThinking) {
        return ChatRequest.builder()
                .model(model)
                .systemMessage(systemMessage)
                .messages(messages)
                .tools(toolMetadata)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .enableThinking(enableThinking)
                .build();
    }
}