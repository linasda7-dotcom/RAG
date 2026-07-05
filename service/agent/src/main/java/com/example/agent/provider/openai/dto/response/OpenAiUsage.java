package com.example.agent.provider.openai.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiUsage(
                int promptTokens,
                int completionTokens,
                int totalTokens,
                OpenAiCompletionTokensDetails completionTokensDetails,
                OpenAiPromptTokensDetails promptTokensDetails,
                int promptCacheHitTokens,
                int promptCacheMissTokens) {
}
