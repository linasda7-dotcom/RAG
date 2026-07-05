package com.example.agent.core.model;

import com.example.agent.core.request.ChatRequest;

public interface StreamingChatModel {
    void chat(ChatRequest request, StreamingResponseHandler handler);

    default String modelName() {
        return "";
    }
}
