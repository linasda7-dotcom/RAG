package com.example.agent.core.rag.embedding;

public final class EmbeddingSearchRequest {
    private final Embedding queryEmbedding;
    private final int maxResults;
    private final double minScore;

    private EmbeddingSearchRequest(Builder builder) {
        this.queryEmbedding = builder.queryEmbedding;
        this.maxResults = builder.maxResults;
        this.minScore = builder.minScore;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Embedding queryEmbedding;
        private int maxResults = 3;
        private double minScore;

        public Builder queryEmbedding(Embedding queryEmbedding) {
            if (queryEmbedding == null) {
                throw new IllegalArgumentException("queryEmbedding 不能为空");
            }
            this.queryEmbedding = queryEmbedding;
            return this;
        }

        public Builder maxResults(int maxResults) {
            if (maxResults <= 0) {
                throw new IllegalArgumentException("maxResult 必须大于0");
            }

            this.maxResults = maxResults;
            return this;

        }

        public Builder minScore(double minScore) {

            if (!Double.isFinite(minScore) || minScore < -1 || minScore > 1) {
                throw new IllegalArgumentException(
                        "minScore 必须在 -1 到 1 之间");
            }
            this.minScore = minScore;
            return this;

        }

        public EmbeddingSearchRequest build() {
            if (queryEmbedding == null) {
                throw new IllegalStateException("必须装配 queryEmbedding");
            }

            return new EmbeddingSearchRequest(this);
        }

    }

    public Embedding queryEmbedding() {
        return queryEmbedding;
    }

    public int maxResults() {
        return maxResults;
    }

    public double minScore() {
        return minScore;
    }

}
