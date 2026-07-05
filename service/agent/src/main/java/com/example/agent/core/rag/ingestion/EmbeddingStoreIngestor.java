package com.example.agent.core.rag.ingestion;

import java.util.List;

import com.example.agent.core.rag.document.Document;
import com.example.agent.core.rag.document.DocumentSplitter;
import com.example.agent.core.rag.document.TextSegment;
import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.core.rag.embedding.EmbeddingModel;
import com.example.agent.core.rag.embedding.EmbeddingStore;

/**
 * 
 * EmbeddingStoreIngestor
 */
public final class EmbeddingStoreIngestor {

    private final DocumentSplitter documentSplitter;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore embeddingStore;

    private EmbeddingStoreIngestor(Builder builder) {
        this.documentSplitter = builder.documentSplitter;
        this.embeddingModel = builder.embeddingModel;
        this.embeddingStore = builder.embeddingStore;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void ingest(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("document 不能为空");
        }

        ingestAll(List.of(document));
    }

    public void ingestAll(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        List<TextSegment> segments = documentSplitter.splitAll(documents);

        if (segments.isEmpty()) {
            return;
        }

        List<Embedding> embeddings = embeddingModel.embeddingAll(segments);
        embeddingStore.addAll(embeddings, segments);
    }

    public static final class Builder {
        private DocumentSplitter documentSplitter;
        private EmbeddingModel embeddingModel;
        private EmbeddingStore embeddingStore;

        public Builder documentSplitter(DocumentSplitter documentSplitter) {
            if (documentSplitter == null) {
                throw new IllegalArgumentException("documentSplitter 不能为空");
            }

            this.documentSplitter = documentSplitter;
            return this;
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

        public EmbeddingStoreIngestor build() {

            if (documentSplitter == null) {
                throw new IllegalStateException("必须装配 documentSplitter");
            }

            if (embeddingModel == null) {
                throw new IllegalStateException("必须装配 embeddingModel");
            }

            if (embeddingStore == null) {
                throw new IllegalStateException("必须装配 embeddingStore");
            }

            return new EmbeddingStoreIngestor(this);
        }
    }
}
