package com.example.agent.provider.openai.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiChatRequest(
                String model,
                Double temperature,
                Integer maxTokens,
                Boolean enableThinking,
                Boolean stream,
                List<OpenAiMessage> messages,
                List<OpenAiTool> tools) {
}
