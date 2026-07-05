package com.example.agent.core.model;

import java.util.List;

import com.example.agent.core.tool.ToolCall;

/**
 * 流式响应的生命周期
 */
public interface StreamingResponseHandler {
    /**
     * 当返回部分流的时候
     * 
     * @param partialResponse 部分回应
     */
    void onPartialResponse(String partialResponse);

    /**
     * 当模型思考的时候
     * 
     * @param partialReasoning 部分思考
     */
    default void onPartialReasoning(String partialReasoning) {

    };

    /**
     * 当工具调用的时候
     * 
     * @param toolCalls 工具列表
     */
    default void onToolCalls(List<ToolCall> toolCalls) {

    };

    /**
     * 响应完成的时候
     * 
     * @param completeResponse 最终回复
     */
    void onCompleteResponse(String completeResponse);

    /**
     * 当发生错误的时候
     * 
     * @param error 错误抛出的异常
     */
    void onError(Throwable error);
}
