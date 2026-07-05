package com.example.agent.infrastructure.rag.store;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.example.agent.core.rag.document.TextSegment;
import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.core.rag.embedding.EmbeddingMatch;
import com.example.agent.core.rag.embedding.EmbeddingSearchRequest;
import com.example.agent.core.rag.embedding.EmbeddingSearchResult;
import com.example.agent.core.rag.embedding.EmbeddingStore;

public class InMemoryEmbeddingStore implements EmbeddingStore {

    private final List<Entry> entries = new ArrayList<Entry>();

    @Override
    public String add(Embedding embedding, TextSegment segment) {
        if (embedding == null) {
            throw new IllegalArgumentException("embedding 不能为空");
        }

        if (segment == null) {
            throw new IllegalArgumentException("segment 不能为空");
        }
        entries.add(new Entry(embedding, segment));
        return segment.id();
    }

    @Override
    public EmbeddingSearchResult search(EmbeddingSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request 不能为空");
        }

        Embedding queryEmbedding = request.queryEmbedding();

        List<EmbeddingMatch> matches = entries.stream()
                .map(entry -> new EmbeddingMatch(
                        cosineSimilarity(queryEmbedding, entry.embedding),
                        entry.segment.id(),
                        entry.embedding,
                        entry.segment()))
                .filter(match -> match.score() >= request.minScore())
                .sorted(Comparator
                        .comparingDouble(
                                (EmbeddingMatch match) -> match.score())
                        .reversed())
                .limit(request.maxResults())
                .toList();

        return new EmbeddingSearchResult(matches);
    }

    /**
     * calculation the cosine similarity tow embedding
     *
     * @param a the first embedding
     * @param b embedding embedding
     * @return the cosine similarity between {@code a} and {@code b}
     */
    private double cosineSimilarity(Embedding a, Embedding b) {

        float[] av = a.vector();
        float[] bv = b.vector();

        if (av.length != bv.length) {
            throw new IllegalArgumentException(
                    "embedding 维度不一致:"
                            + av.length
                            + "!="
                            + bv.length);
        }

        double dot = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < av.length; i++) {
            dot += av[i] * bv[i];
            normA += av[i] * av[i];
            normB += bv[i] * bv[i];
        }

        if (normA == 0 || normB == 0) {
            return 0;
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));

    }

    /**
     * 
     * Entry 向量以及文本
     * 
     * @param embedding 向量
     * @param segment   原始文本
     */
    public record Entry(
            Embedding embedding,
            TextSegment segment) {

    }
}
