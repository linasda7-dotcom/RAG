package com.example.dto.response;

import java.time.LocalDateTime;

public record KnowledgeBaseResponse(
        Long id,
        String name,
        String description,
        Integer docCount,
        LocalDateTime createdAt) {
}
