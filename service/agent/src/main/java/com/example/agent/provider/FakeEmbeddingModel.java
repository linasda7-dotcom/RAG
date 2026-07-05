package com.example.agent.provider;

import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.core.rag.embedding.EmbeddingModel;

public class FakeEmbeddingModel implements EmbeddingModel {
    private final int dimension;

    public FakeEmbeddingModel() {
        this(1024);
    }

    public FakeEmbeddingModel(int dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("dimension 必须大于0");
        }
        this.dimension = dimension;
    }

    @Override
    public Embedding embed(String text) {
        float[] vector = new float[dimension];

        if (text == null || text.isBlank()) {
            return new Embedding(vector);
        }

        String normalized = text.toLowerCase();

        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);

            if (Character.isWhitespace(ch)
                    || Character.isISOControl(ch)
                    || isPunctuation(ch)) {
                continue;
            }

            int index = Math.floorMod(ch, dimension);
            vector[index] += 1.0f;
        }

        normalized(vector);

        return new Embedding(vector);
    }

    private boolean isPunctuation(char ch) {
        return "，。！？；：,.!?;:、()（）[]【】{}<>《》\"'`".indexOf(ch) >= 0;
    }

    private void normalized(float[] vector) {
        // 向量的长度
        double norm = 0;

        for (float value : vector) {
            norm += value * value;
        }

        if (norm == 0) {
            return;
        }

        double sqrt = Math.sqrt(norm);

        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) (vector[i] / sqrt);
        }
    }

    public int dimension() {
        return dimension;
    }

}
