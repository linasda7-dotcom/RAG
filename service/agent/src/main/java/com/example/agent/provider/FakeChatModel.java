package com.example.agent.provider;

import com.example.agent.core.model.ChatModel;
import com.example.agent.core.model.ChatModelResponse;
import com.example.agent.core.request.ChatRequest;

public class FakeChatModel implements ChatModel {
    private ChatRequest lastRequest;

    @Override
    public ChatModelResponse chat(ChatRequest request) {
        lastRequest = request;
        return ChatModelResponse.content("fake response");
    }

    @Override
    public String modelName() {
        return "fake-chat-model";
    }

    public ChatRequest lastRequest() {
        return lastRequest;
    }

}
