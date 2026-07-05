package com.example.agent.provider.openai.dto.stream;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiStreamDelta(
                String role,
                String content,
                String reasoningContent,
                List<OpenAiStreamToolCall> toolCalls) {

}
