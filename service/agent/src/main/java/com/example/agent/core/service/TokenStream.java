package com.example.agent.core.service;

import java.util.List;
import java.util.function.Consumer;

import com.example.agent.core.tool.ToolCall;

/**
 * AI Service 层的流返回对象
 * 
 * 设计定位：
 * - 不是底层 Provider
 * - 不是 StreamingChatModel
 * - 是用户拿到的可订阅流
 */
public interface TokenStream {
    /**
     * 部分响应的时候
     * 
     * @param handler
     * @return
     */
    TokenStream onPartialResponse(Consumer<String> handler);

    /**
     * 思考/推理的时候
     * 
     * @param handler
     * @return
     */
    TokenStream onPartialReasoning(Consumer<String> handler);

    /**
     * 工具调用的时候
     * 
     * @param handler
     * @return
     */
    TokenStream onToolCalls(Consumer<List<ToolCall>> handler);

    /**
     * 完全响应的时候
     * 
     * @param handler
     * @return
     */
    TokenStream onCompleteResponse(Consumer<String> handler);

    /**
     * 在错误的时候
     * 
     * @param handler
     * @return
     */
    TokenStream onError(Consumer<Throwable> handler);

    void start();
}
