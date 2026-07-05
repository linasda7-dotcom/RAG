package com.example.agent.provider.openai.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiChatResponse(
                String id,
                String object,
                long created,
                String model,
                List<OpenAiChoice> choices,
                OpenAiUsage usage,
                String systemFingerprint) {

}
