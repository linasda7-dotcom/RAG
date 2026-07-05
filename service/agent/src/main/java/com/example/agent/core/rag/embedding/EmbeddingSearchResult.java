package com.example.agent.core.rag.embedding;

import java.util.List;

/**
 * 一次向量检索的结果
 * 
 * EmbeddingSearchResult
 * 
 * @param matches 文本分段
 */
public record EmbeddingSearchResult(
        List<EmbeddingMatch> matches) {

    public EmbeddingSearchResult {

        if (matches == null) {
            throw new IllegalArgumentException("matches 不能为空");
        }

        matches = List.copyOf(matches);
    }

    public static EmbeddingSearchResult empty() {
        return new EmbeddingSearchResult(List.of());
    }

    public boolean isEmpty() {
        return matches.isEmpty();
    }
}
