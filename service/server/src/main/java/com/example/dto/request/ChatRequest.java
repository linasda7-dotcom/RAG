package com.example.dto.request;

public record ChatRequest(
        String question,
        Long kbId,
        String sessionId) {
    public ChatRequest {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("问题不能为空");
        }
    }
}
