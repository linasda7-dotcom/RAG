package com.example.dto.response;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        Long kbId,
        String fileName,
        String fileType,
        Long fileSize,
        Integer chunkCount,
        String status,
        LocalDateTime createdAt) {
}
