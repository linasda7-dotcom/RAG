package com.example.agent.core.service;

import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.agent.AgentLogger;
import com.example.agent.core.agent.AgentOptions;
import com.example.agent.core.memory.ChatMemory;
import com.example.agent.core.message.AssistantMessage;
import com.example.agent.core.message.AssistantToolCallMessage;
import com.example.agent.core.message.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.agent.core.message.UserMessage;
import com.example.agent.core.model.StreamingChatModel;
import com.example.agent.core.model.StreamingResponseHandler;
import com.example.agent.core.prompt.PromptBuilder;
import com.example.agent.core.rag.retriever.ContentRetriever;
import com.example.agent.core.rag.retriever.RagPromptAugmentor;
import com.example.agent.core.request.ChatRequest;
import com.example.agent.core.tool.ToolCall;

import com.example.agent.core.tool.ToolMetadata;

public class StreamingAgentRunner {
    private final StreamingChatModel streamingChatModel;
    private final ChatMemory chatMemory;
    private final String systemMessage;
    private final AgentOptions agentOptions;
    private final AgentLogger agentLogger;
    private final List<ToolMetadata> toolMetadataList = new ArrayList<>();
    private final AgentToolExecutor agentToolExecutor;
    private final PromptBuilder promptBuilder = new PromptBuilder();
    private final RagPromptAugmentor ragPromptAugmentor;
    private static final Logger log = LoggerFactory.getLogger(StreamingAgentRunner.class);

    public StreamingAgentRunner(
            StreamingChatModel streamingChatModel,
            ChatMemory chatMemory,
            String systemMessage,
            List<ToolMetadata> toolsMetadataList,
            AgentOptions agentOptions,
            ContentRetriever contentRetriever) {

        if (streamingChatModel == null) {
            throw new IllegalArgumentException("streamingChatModel 不能为空");
        }

        this.streamingChatModel = streamingChatModel;
        this.chatMemory = chatMemory;
        this.systemMessage = systemMessage;
        this.agentOptions = agentOptions == null
                ? AgentOptions.defaultOptions()
                : agentOptions;
        this.ragPromptAugmentor = new RagPromptAugmentor(contentRetriever);
        this.agentLogger = new AgentLogger(this.agentOptions.logEnabled());

        if (toolsMetadataList != null) {
            this.toolMetadataList.addAll(toolsMetadataList);
        }

        this.agentToolExecutor = new AgentToolExecutor(
                this.toolMetadataList,
                this.agentOptions,
                this.agentLogger);
    }

    public void chat(String userMessage, StreamingResponseHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler 不能为空");
        }

        List<ChatMessage> messages = new ArrayList<ChatMessage>();

        if (chatMemory != null) {
            messages.addAll(chatMemory.messages());
        }

        int newMessageStartIndex = messages.size();
        String augmentUserMessage = ragPromptAugmentor.augment(userMessage);

        log.info("Streaming userMessage original length={}, snippet='{}'",
                userMessage.length(),
                userMessage.replaceAll("\n", " ").substring(0, Math.min(240, userMessage.length())));
        log.info("Streaming message augmented length={}, snippet='{}'",
                augmentUserMessage.length(),
                augmentUserMessage.replaceAll("\n", " ").substring(0, Math.min(240, augmentUserMessage.length())));

        messages.add(new UserMessage(augmentUserMessage));

        try {
            runStreamingAgentLoop(messages, handler);
            saveNewMessages(messages, newMessageStartIndex, new UserMessage(userMessage));
        } catch (Exception e) {
            handler.onError(e);
        }

    }

    private void runStreamingAgentLoop(
            List<ChatMessage> messages,
            StreamingResponseHandler outerHandler) {

        agentLogger.info(
                "Streaming Agent Loop 启动"
                        + ",maxAgentSteps=" + agentOptions.maxAgentSteps()
                        + ",temperature=" + agentOptions.temperature()
                        + ",enableThinking=" + agentOptions.enableThinking()
                        + ",tools=" + toolMetadataList.size());

        for (int step = 1; step <= agentOptions.maxAgentSteps(); step++) {
            agentLogger.step(step, "构建流式 ChatRequest,messages.size=" + messages.size());

            ChatRequest request = promptBuilder.buildRequest(
                    streamingChatModel.modelName(),
                    systemMessage,
                    messages,
                    toolMetadataList,
                    agentOptions.temperature(),
                    agentOptions.maxTokens(),
                    agentOptions.enableThinking());

            CapturingStreamingResponseHandler stepHandler = new CapturingStreamingResponseHandler(
                    outerHandler);

            agentLogger.step(step, "开始流式请求模型");

            streamingChatModel.chat(request, stepHandler);

            if (stepHandler.error != null) {
                throw new RuntimeException("Streaming 模型调用失败", stepHandler.error);
            }

            agentLogger.step(step, "流式模型响应完成"
                    + ",content.length=" + stepHandler.completeResponse.length()
                    + ",toolCalls=" + stepHandler.toolCalls.size());

            if (stepHandler.toolCalls.isEmpty()) {
                String finalAnswer = stepHandler.completeResponse.toString();

                messages.add(new AssistantMessage(finalAnswer));

                agentLogger.step(step, "模型最终返回答案，Streaming Agent Loop");

                outerHandler.onCompleteResponse(finalAnswer);
                return;
            }

            outerHandler.onToolCalls(stepHandler.toolCalls);

            AssistantToolCallMessage assistantToolCallMessage = new AssistantToolCallMessage(
                    stepHandler.completeResponse.toString(),
                    stepHandler.toolCalls);

            messages.add(assistantToolCallMessage);

            messages.addAll(agentToolExecutor.execute(stepHandler.toolCalls, step));
        }
        throw new RuntimeException(
                "Streaming Agent Loop 超过最大执行步数:"
                        + agentOptions.maxAgentSteps()
                        + ",可能出现循环工具调用");
    }

    private void saveNewMessages(
            List<ChatMessage> messages,
            int startIndex,
            UserMessage originalUserMessage) {
        if (chatMemory == null) {
            return;
        }
        chatMemory.add(originalUserMessage);
        for (int i = startIndex + 1; i < messages.size(); i++) {
            chatMemory.add(messages.get(i));
        }
    }

    private static class CapturingStreamingResponseHandler implements StreamingResponseHandler {
        private final StreamingResponseHandler outerHandler;

        private final StringBuilder completeResponse = new StringBuilder();
        private final List<ToolCall> toolCalls = new ArrayList<ToolCall>();
        private Throwable error;

        private CapturingStreamingResponseHandler(StreamingResponseHandler outerHandler) {
            this.outerHandler = outerHandler;
        }

        @Override
        public void onPartialResponse(String partialResponse) {
            completeResponse.append(partialResponse);
            outerHandler.onPartialResponse(partialResponse);
        }

        @Override
        public void onPartialReasoning(String partialReasoning) {
            outerHandler.onPartialReasoning(partialReasoning);
        }

        @Override
        public void onToolCalls(List<ToolCall> toolCalls) {
            if (toolCalls != null && !toolCalls.isEmpty()) {
                this.toolCalls.addAll(toolCalls);
            }
        }

        @Override
        public void onCompleteResponse(String completeResponse) {

        }

        @Override
        public void onError(Throwable error) {
            this.error = error;
        }

    }
}
