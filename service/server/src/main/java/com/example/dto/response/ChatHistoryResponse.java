package com.example.dto.response;

import java.time.LocalDateTime;

public record ChatHistoryResponse(
        Long id,
        String question,
        String answer,
        Long kbId,
        String sessionId,
        LocalDateTime createdAt) {
}
