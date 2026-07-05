package com.example.agent.provider.milvus;

import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.rag.document.TextSegment;
import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.core.rag.embedding.EmbeddingMatch;
import com.example.agent.core.rag.embedding.EmbeddingSearchRequest;
import com.example.agent.core.rag.embedding.EmbeddingSearchResult;
import com.example.agent.core.rag.embedding.EmbeddingStore;
import com.example.agent.core.rag.embedding.EmbeddingStoreException;

public class MilvusEmbeddingStore implements EmbeddingStore {

    private final MilvusJavaClientAdapter adapter;

    private MilvusEmbeddingStore(Builder builder) {
        this.adapter = new MilvusJavaClientAdapter(builder.config);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String add(Embedding embedding, TextSegment segment) {
        return addAll(List.of(embedding), List.of(segment)).get(0);
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings, List<TextSegment> segments) {

        if (embeddings == null) {
            throw new IllegalArgumentException("embedding 不能为空");
        }

        if (segments == null) {
            throw new IllegalArgumentException("segment 不能为空");
        }

        if (embeddings.size() != segments.size()) {
            throw new IllegalArgumentException("embedding 与 segment 数量必须一致");
        }

        List<MilvusInsertRow> rows = new ArrayList<MilvusInsertRow>();

        for (int i = 0; i < embeddings.size(); i++) {
            TextSegment segment = segments.get(i);
            Embedding embedding = embeddings.get(i);

            rows.add(
                    new MilvusInsertRow(
                            segments.get(i).id(),
                            segments.get(i).text(),
                            segment.metadata(),
                            embedding.vector()));
        }

        try {
            return adapter.insert(rows);
        } catch (RuntimeException e) {
            throw new EmbeddingStoreException("Milvus insert 失败", e);
        }
    }

    @Override
    public EmbeddingSearchResult search(EmbeddingSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request 不能为空");
        }

        MilvusSearchRequest searchRequest = MilvusSearchRequest.builder()
                .queryVector(request.queryEmbedding().vector())
                .maxResults(request.maxResults())
                .minScore(request.minScore())
                .build();

        try {
            List<MilvusSearchHit> hits = adapter.search(searchRequest);

            List<EmbeddingMatch> matches = hits.stream()
                    .map(searchHit -> new EmbeddingMatch(
                            searchHit.score(),
                            searchHit.id(),
                            null,
                            new TextSegment(
                                    searchHit.id(),
                                    searchHit.text(),
                                    searchHit.metadata())))
                    .toList();
            return new EmbeddingSearchResult(matches);
        } catch (Exception e) {
            throw new EmbeddingStoreException("Milvus search 失败", e);
        }

    }

    public static class Builder {

        private MilvusConnectionConfig config;

        public Builder config(MilvusConnectionConfig config) {
            this.config = config;
            return this;
        }

        public MilvusEmbeddingStore build() {
            if (config == null) {
                throw new IllegalArgumentException("config 不能为空");
            }
            return new MilvusEmbeddingStore(this);
        }
    }

    public MilvusJavaClientAdapter getAdapter() {
        return adapter;
    }

}
