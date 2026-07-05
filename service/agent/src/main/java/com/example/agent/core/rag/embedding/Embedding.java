package com.example.agent.core.rag.embedding;

import java.util.Arrays;

public final class Embedding {
    private final float[] vector;

    public Embedding(float[] vector) {

        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("embedding vector");
        }

        this.vector = Arrays.copyOf(vector, vector.length);

    }

    public float[] vector() {
        return Arrays.copyOf(vector, vector.length);
    }

    public int dimension() {
        return vector.length;
    }

}
