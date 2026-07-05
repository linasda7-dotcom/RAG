package com.example.agent.core.rag.retriever;

import java.util.List;

import com.example.agent.core.rag.document.TextSegment;
import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.core.rag.embedding.EmbeddingModel;
import com.example.agent.core.rag.embedding.EmbeddingSearchRequest;
import com.example.agent.core.rag.embedding.EmbeddingSearchResult;
import com.example.agent.core.rag.embedding.EmbeddingStore;

public final class EmbeddingStoreContentRetriever implements ContentRetriever {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore embeddingStore;
    private final int maxResults;
    private final double minScore;

    private EmbeddingStoreContentRetriever(Builder builder) {
        this.embeddingModel = builder.embeddingModel;
        this.embeddingStore = builder.embeddingStore;
        this.maxResults = builder.maxResults;
        this.minScore = builder.minScore;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<TextSegment> retrieve(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        Embedding queryEmbedding = embeddingModel.embed(query);

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest
                .builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();

        EmbeddingSearchResult searchResult = embeddingStore.search(searchRequest);

        return searchResult.matches()
                .stream()
                .map(match -> match.segment())
                .toList();
    }

    public static class Builder {
        private EmbeddingModel embeddingModel;
        private EmbeddingStore embeddingStore;
        private int maxResults = 3;
        private double minScore = 0.7;

        public Builder() {
        }

        public Builder embeddingModel(EmbeddingModel embeddingModel) {
            if (embeddingModel == null) {
                throw new IllegalArgumentException("embeddingModel 不能为空");
            }
            this.embeddingModel = embeddingModel;
            return this;
        }

        public Builder embeddingStore(EmbeddingStore embeddingStore) {
            if (embeddingStore == null) {
                throw new IllegalArgumentException("embeddingStore 不能为空");
            }
            this.embeddingStore = embeddingStore;
            return this;
        }

        public Builder maxResults(int maxResults) {
            if (maxResults <= 0) {
                throw new IllegalArgumentException("maxResults 必须大于 0");
            }
            this.maxResults = maxResults;
            return this;
        }

        public Builder minScore(double minScore) {
            if (!Double.isFinite(minScore)
                    || minScore < -1
                    || minScore > 1) {

                throw new IllegalArgumentException(
                        "minScore 必须在 -1 到 1 之间");
            }

            this.minScore = minScore;
            return this;
        }

        public EmbeddingStoreContentRetriever build() {
            if (embeddingModel == null) {
                throw new IllegalStateException(
                        "必须配置 embeddingModel");
            }

            if (embeddingStore == null) {
                throw new IllegalStateException(
                        "必须配置 embeddingStore");
            }
            return new EmbeddingStoreContentRetriever(this);
        }
    }
}
