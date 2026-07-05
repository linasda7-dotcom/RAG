package com.example.agent.core.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.agent.core.agent.AgentOptions;
import com.example.agent.core.memory.ChatMemory;

import com.example.agent.core.model.ChatModel;

import com.example.agent.core.model.StreamingChatModel;
import com.example.agent.core.rag.retriever.ContentRetriever;
import com.example.agent.core.tool.ToolMetadata;
import com.example.agent.core.tool.ToolScanner;

public class AiServiceInvocationHandler implements InvocationHandler {
    /**
     * 防止模型无限循环调用工具
     */

    private static final Logger log = LoggerFactory.getLogger(AiServiceInvocationHandler.class);

    private final AgentOptions agentOptions;

    private final ChatModel chatModel;
    private final ChatMemory chatMemory;
    private final StreamingChatModel streamingChatModel;
    private final String systemMessage;
    private final List<ToolMetadata> toolMetadataList = new ArrayList<>();
    private final ContentRetriever contentRetriever;

    public AiServiceInvocationHandler(
            ChatModel chatModel,
            StreamingChatModel streamingChatModel,
            ChatMemory chatMemory,
            String systemMessage,
            Object[] tools,
            AgentOptions agentOptions,
            ContentRetriever contentRetriever) {

        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
        this.chatMemory = chatMemory;
        this.systemMessage = systemMessage;
        this.contentRetriever = contentRetriever;

        this.agentOptions = agentOptions == null ? AgentOptions.defaultOptions() : agentOptions;

        if (tools != null) {
            for (Object tool : tools) {
                if (tool != null) {
                    toolMetadataList.addAll(ToolScanner.scan(tool));
                }
            }
        }

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

        if (isTokenStreamMethod(method)) {
            return createTokenStream(args);
        }

        return invokeChat(args, method);
    }

    private Object invokeChat(Object[] args, Method method) {
        if (chatModel == null) {
            throw new IllegalStateException(
                    "chatModel 不能为空，无法执行非流响应:" + method.getName());
        }
        String userMessage = buildUserMessage(args);

        AgentRunner agentRunner = new AgentRunner(
                chatModel,
                chatMemory,
                systemMessage,
                toolMetadataList,
                agentOptions,
                contentRetriever);

        return agentRunner.chat(userMessage);
    }

    private boolean isTokenStreamMethod(Method method) {
        return TokenStream.class.isAssignableFrom(method.getReturnType());
    }

    private Object createTokenStream(Object[] args) {

        if (streamingChatModel == null) {
            throw new IllegalStateException("streamingChatModel 不能为空，无法执行 TokenStream");
        }

        String userMessage = buildUserMessage(args);

        StreamingAgentRunner runner = new StreamingAgentRunner(
                streamingChatModel,
                chatMemory,
                systemMessage,
                toolMetadataList,
                agentOptions,
                contentRetriever);
        return new AiServiceTokenStream(runner, userMessage);
    }

    /**
     * 构造用户消息
     * 
     * @param args 用户本次消息
     * @return 用户本次消息
     */
    private String buildUserMessage(Object[] args) {

        if (args == null || args.length == 0) {
            log.debug("buildUserMessage args are empty or null");
            return "";
        }

        String message;
        if (args.length == 1) {
            message = String.valueOf(args[0]);
        } else {
            StringBuilder builder = new StringBuilder();
            for (Object arg : args) {
                builder.append(arg).append("\n");
            }
            message = builder.toString().trim();
        }

        log.info("buildUserMessage args={} => message length={}, snippet='{}'",
                Arrays.toString(args),
                message.length(),
                message.replaceAll("\n", " ").substring(0, Math.min(240, message.length())));
        if (log.isDebugEnabled()) {
            log.debug("buildUserMessage args={} => message='{}'", Arrays.toString(args), message.replaceAll("\n", " "));
        }
        return message;
    }
}
