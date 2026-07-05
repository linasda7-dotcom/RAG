package com.example.agent.provider.openai;

public class OpenAiEmbeddingException extends RuntimeException {

    public OpenAiEmbeddingException(String message) {
        super(message);
    }

    public OpenAiEmbeddingException(String message, Throwable cause) {
        super(message, cause);
    }

}
