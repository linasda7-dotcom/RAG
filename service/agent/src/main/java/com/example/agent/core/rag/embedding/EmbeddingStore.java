package com.example.agent.core.rag.embedding;

import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.rag.document.TextSegment;

public interface EmbeddingStore {

    /**
     * add embedding
     * 
     * @param embedding textSegment vector
     * @param segment   textSegment
     */
    String add(Embedding embedding, TextSegment segment);

    /**
     * addAll embedding
     * 
     * @param embeddings embeddings vectors
     * @param segments   textSegments
     */
    default List<String> addAll(
            List<Embedding> embeddings,
            List<TextSegment> segments) {

        if (embeddings == null) {
            throw new IllegalArgumentException("embedding 不能为空");
        }

        if (segments == null) {
            throw new IllegalArgumentException("segment 不能为空");
        }

        if (embeddings.size() != segments.size()) {
            throw new IllegalArgumentException("embeddings 和 segments数量必须一致");
        }

        List<String> ids = new ArrayList<String>();

        for (int i = 0; i < embeddings.size(); i++) {
            ids.add(add(embeddings.get(i), segments.get(i)));
        }

        return ids;
    }

    EmbeddingSearchResult search(EmbeddingSearchRequest request);
}
