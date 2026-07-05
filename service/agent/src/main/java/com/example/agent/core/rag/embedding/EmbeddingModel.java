package com.example.agent.core.rag.embedding;

import java.util.List;

import com.example.agent.core.rag.document.TextSegment;

public interface EmbeddingModel {
    Embedding embed(String text);

    default List<Embedding> embeddingAll(List<TextSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return List.of();
        }

        return segments.stream()
                .map(segment -> embed(segment.text()))
                .toList();
    }
}
