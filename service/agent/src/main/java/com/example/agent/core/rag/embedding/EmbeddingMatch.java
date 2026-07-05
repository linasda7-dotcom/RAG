package com.example.agent.core.rag.embedding;

import com.example.agent.core.rag.document.TextSegment;

/**
 * 
 * EmbeddingMatch
 * 一条向量检索匹配结果
 * 
 * @param score       相关度分数
 * @param embeddingId 向量存储中的记录id
 * @param embedding   命中的向量 允许为空
 * @param segment     命中的文本片段
 */
public record EmbeddingMatch(
        double score,
        String embeddingId,
        Embedding embedding,
        TextSegment segment) {

    public EmbeddingMatch {

        if (!Double.isFinite(score)) {
            throw new IllegalArgumentException(
                    "embedding 不能为空");
        }

        if (embeddingId == null || embeddingId.isBlank()) {
            throw new IllegalArgumentException(
                    "embedding 不能为空");
        }

        if (segment == null) {
            throw new IllegalArgumentException(
                    "segment 不能为空");
        }

    }

    /**
     * @deprecated 我们已经决定在1.4版本中弃用此构造器
     *             请用新的构造器 {@link #EmbeddingMatch(double, String, Embedding,
     *             TextSegment)}
     * @param score
     * @param segment
     */
    @Deprecated(since = "1.4", forRemoval = true)
    public EmbeddingMatch(double score, TextSegment segment) {
        this(
                score,
                segment == null ? null : segment.id(),
                null,
                segment);
    }
}
