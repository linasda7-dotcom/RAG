package com.example.agent.core.rag.embedding;

public class EmbeddingStoreException extends RuntimeException {

    public EmbeddingStoreException(String message) {
        super(message);
    }

    public EmbeddingStoreException(String message, Throwable cause) {
        super(message, cause);
    }

}
