package com.example.agent.core.request;

public interface ChatRequestBuilder<T> {
    T  build(ChatRequest request);
}
