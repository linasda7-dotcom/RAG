package com.example.agent.core.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.example.agent.core.model.StreamingResponseHandler;
import com.example.agent.core.tool.ToolCall;

/**
 * TokenStream 的默认实现
 * 
 * 它本身不直接请求模型，而是持有StreamingAgentRunner。
 * 用户注册完回调后，调用start() 才真正正式启动流 Agent Loop
 */
public class AiServiceTokenStream implements TokenStream {

    private final StreamingAgentRunner runner;
    private final String userMessage;

    private Consumer<String> partialResponseHandler = ignored -> {
    };
    private Consumer<String> partialReasoningHandler = ignored -> {
    };
    private Consumer<List<ToolCall>> toolCallsHandler = ignored -> {
    };
    private Consumer<String> completeResponseHandler = ignored -> {
    };
    private Consumer<Throwable> errorHandler = error -> {
        throw new RuntimeException("TokenStream 执行失败", error);
    };

    private boolean started = false;

    public AiServiceTokenStream(StreamingAgentRunner runner, String userMessage) {
        this.runner = Objects.requireNonNull(runner, "runner 不能为空");
        this.userMessage = userMessage == null ? "" : userMessage;
    }

    @Override
    public TokenStream onPartialResponse(Consumer<String> handler) {
        partialResponseHandler = Objects.requireNonNull(handler, "partialResponse handler 不能为空");
        return this;
    }

    @Override
    public TokenStream onPartialReasoning(Consumer<String> handler) {
        partialReasoningHandler = Objects.requireNonNull(handler, "partialReasoning handler 不能为空");
        return this;
    }

    @Override
    public TokenStream onToolCalls(Consumer<List<ToolCall>> handler) {
        toolCallsHandler = Objects.requireNonNull(handler, "toolCalls handler 不能为空");
        return this;
    }

    @Override
    public TokenStream onCompleteResponse(Consumer<String> handler) {
        completeResponseHandler = Objects.requireNonNull(handler, "completeResponse 不能为空");
        return this;
    }

    @Override
    public TokenStream onError(Consumer<Throwable> handler) {
        errorHandler = Objects.requireNonNull(handler, "error handler 不能为空");
        return this;
    }

    @Override
    public void start() {
        if (started) {
            throw new IllegalStateException("TokenStream 已经启动，不能重复启动");
        }

        started = true;

        runner.chat(userMessage, new StreamingResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                partialResponseHandler.accept(partialResponse);
            }

            @Override
            public void onPartialReasoning(String partialReasoning) {
                partialReasoningHandler.accept(partialReasoning);
            }

            @Override
            public void onToolCalls(List<ToolCall> toolCalls) {
                toolCallsHandler.accept(toolCalls);
            }

            @Override
            public void onCompleteResponse(String completeResponse) {
                completeResponseHandler.accept(completeResponse);
            }

            @Override
            public void onError(Throwable error) {
                errorHandler.accept(error);
            }

        });
    }
}
