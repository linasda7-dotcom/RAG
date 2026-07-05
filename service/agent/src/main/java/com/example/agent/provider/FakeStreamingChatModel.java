package com.example.agent.provider;

import com.example.agent.core.model.StreamingChatModel;
import com.example.agent.core.model.StreamingResponseHandler;
import com.example.agent.core.request.ChatRequest;

public class FakeStreamingChatModel implements StreamingChatModel {
    private ChatRequest lastRequest;
    private int callCount;

    @Override
    public void chat(ChatRequest request, StreamingResponseHandler handler) {
        this.lastRequest = request;
        callCount++;
        handler.onPartialResponse("fake");
        handler.onCompleteResponse("fake");
    }

    @Override
    public String modelName() {
        return "fake-streaming-model";
    }

    public ChatRequest lastRequest() {
        return lastRequest;
    }

    public int callCount() {
        return callCount;
    }

}
