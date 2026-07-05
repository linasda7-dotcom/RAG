package com.example.service;

import java.util.List;
import java.util.function.Consumer;

import com.example.agent.core.service.TokenStream;
import com.example.agent.core.tool.ToolCall;

/**
 * TokenStream包装类，在完成时发送sessionId而不是完整响应
 */
public class SessionIdTokenStream implements TokenStream {

    private final TokenStream originalStream;
    private final String sessionId;
    private final Runnable onComplete;

    public SessionIdTokenStream(TokenStream originalStream, String sessionId, Runnable onComplete) {
        this.originalStream = originalStream;
        this.sessionId = sessionId;
        this.onComplete = onComplete;
    }

    @Override
    public TokenStream onPartialResponse(Consumer<String> handler) {
        originalStream.onPartialResponse(handler);
        return this;
    }

    @Override
    public TokenStream onPartialReasoning(Consumer<String> handler) {
        originalStream.onPartialReasoning(handler);
        return this;
    }

    @Override
    public TokenStream onToolCalls(Consumer<List<ToolCall>> handler) {
        originalStream.onToolCalls(handler);
        return this;
    }

    @Override
    public TokenStream onCompleteResponse(Consumer<String> handler) {
        originalStream.onCompleteResponse(completeResponse -> {
            // 执行保存聊天记录的操作
            if (onComplete != null) {
                onComplete.run();
            }
            // 发送sessionId给前端
            handler.accept(sessionId);
        });
        return this;
    }

    @Override
    public TokenStream onError(Consumer<Throwable> handler) {
        originalStream.onError(handler);
        return this;
    }

    @Override
    public void start() {
        originalStream.start();
    }
}