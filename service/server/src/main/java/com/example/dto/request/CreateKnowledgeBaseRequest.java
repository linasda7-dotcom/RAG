package com.example.dto.request;

public record CreateKnowledgeBaseRequest(
        String name,
        String description) {
    public CreateKnowledgeBaseRequest {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("知识库名称不能为空");
        }
    }
}
