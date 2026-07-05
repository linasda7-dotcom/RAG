package com.example.agent.core.service;

import java.lang.reflect.Proxy;

import com.example.agent.core.agent.AgentOptions;
import com.example.agent.core.memory.ChatMemory;
import com.example.agent.core.model.ChatModel;
import com.example.agent.core.model.StreamingChatModel;
import com.example.agent.core.rag.retriever.ContentRetriever;

public class AiServiceBuilder<T> {
    private final Class<T> serviceClass;

    private ChatModel chatModel;
    private StreamingChatModel streamingChatModel;
    private ChatMemory chatMemory;
    private ContentRetriever contentRetriever;
    private String systemMessage;
    private Object[] tools = new Object[0];

    private AgentOptions agentOptions = AgentOptions.defaultOptions();

    public AiServiceBuilder(Class<T> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public AiServiceBuilder<T> systemMessage(String systemMessage) {
        this.systemMessage = systemMessage;
        return this;
    }

    public AiServiceBuilder<T> chatModel(ChatModel chatModel) {
        this.chatModel = chatModel;
        return this;
    }

    public AiServiceBuilder<T> streamingChatModel(StreamingChatModel streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
        return this;
    }

    public AiServiceBuilder<T> chatMemory(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
        return this;
    }

    public AiServiceBuilder<T> tools(Object... tools) {
        this.tools = tools == null ? new Object[0] : tools;
        return this;
    }

    public AiServiceBuilder<T> agentOptions(AgentOptions agentOptions) {
        if (agentOptions == null) {
            throw new IllegalArgumentException("agentOptions 不能为空");
        }
        this.agentOptions = agentOptions;
        return this;
    }

    public AiServiceBuilder<T> maxAgentSteps(int maxAgentSteps) {
        this.agentOptions = this.agentOptions
                .toBuilder()
                .maxAgentSteps(maxAgentSteps)
                .build();
        return this;
    }

    public AiServiceBuilder<T> temperature(double temperature) {
        this.agentOptions = this.agentOptions
                .toBuilder()
                .temperature(temperature)
                .build();
        return this;
    }

    public AiServiceBuilder<T> logEnabled(boolean logEnabled) {
        this.agentOptions = this.agentOptions
                .toBuilder()
                .logEnabled(logEnabled)
                .build();
        return this;
    }

    public AiServiceBuilder<T> failFastOnToolError(boolean failFastOnToolError) {
        this.agentOptions = this.agentOptions
                .toBuilder()
                .failFastOnToolError(failFastOnToolError)
                .build();
        return this;
    }

    public AiServiceBuilder<T> maxTokens(Integer maxTokens) {
        this.agentOptions = this.agentOptions
                .toBuilder()
                .maxTokens(maxTokens)
                .build();
        return this;
    }

    public AiServiceBuilder<T> enableThinking(Boolean enableThinking) {
        this.agentOptions = this.agentOptions
                .toBuilder()
                .enableThinking(enableThinking)
                .build();
        return this;
    }

    public AiServiceBuilder<T> contentRetriever(ContentRetriever contentRetriever) {
        if (contentRetriever == null) {
            throw new IllegalArgumentException("contentRetriever 不能为空");
        }
        this.contentRetriever = contentRetriever;
        // TODO 待完善
        // this.agentOptions = this.agentOptions
        // .toBuilder()
        // .contentRetriever(contentRetriever)
        // .build();
        return this;
    }

    @SuppressWarnings("unchecked")
    public T build() {
        validate();
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[] { serviceClass },
                new AiServiceInvocationHandler(
                        chatModel,
                        streamingChatModel,
                        chatMemory,
                        systemMessage,
                        tools,
                        agentOptions,
                        contentRetriever));
    }

    private void validate() {

        if (serviceClass == null) {
            throw new IllegalArgumentException("serviceClass 不能为空");
        }

        if (!serviceClass.isInterface()) {
            throw new IllegalArgumentException("AiService 必须是接口");
        }

        if (chatModel == null && streamingChatModel == null) {
            throw new IllegalArgumentException("chatModel 或 streamingChatModel 不能为空");
        }
    }

}
