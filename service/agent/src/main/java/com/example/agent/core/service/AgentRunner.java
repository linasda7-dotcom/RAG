package com.example.agent.core.service;

import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.agent.AgentLogger;
import com.example.agent.core.agent.AgentOptions;
import com.example.agent.core.memory.ChatMemory;
import com.example.agent.core.message.AssistantMessage;
import com.example.agent.core.message.AssistantToolCallMessage;
import com.example.agent.core.message.ChatMessage;

import com.example.agent.core.message.UserMessage;
import com.example.agent.core.model.ChatModel;
import com.example.agent.core.model.ChatModelResponse;
import com.example.agent.core.prompt.PromptBuilder;
import com.example.agent.core.rag.retriever.ContentRetriever;
import com.example.agent.core.rag.retriever.RagPromptAugmentor;
import com.example.agent.core.request.ChatRequest;

import com.example.agent.core.tool.ToolMetadata;

public class AgentRunner {
    private final ChatModel chatModel;
    private final ChatMemory chatMemory;
    private final String systemMessage;
    private final List<ToolMetadata> toolMetadataList = new ArrayList<ToolMetadata>();
    private final AgentOptions agentOptions;
    private final AgentLogger agentLogger;
    private final AgentToolExecutor agentToolExecutor;
    private final PromptBuilder promptBuilder = new PromptBuilder();
    private final RagPromptAugmentor ragPromptAugmentor;

    public AgentRunner(
            ChatModel chatModel,
            ChatMemory chatMemory,
            String systemMessage,
            List<ToolMetadata> toolMetadataList,
            AgentOptions agentOptions,
            ContentRetriever contentRetriever) {

        if (chatModel == null) {
            throw new IllegalArgumentException("ChatModel 不能为空");
        }

        this.chatModel = chatModel;
        this.chatMemory = chatMemory;
        this.systemMessage = systemMessage;
        this.agentOptions = agentOptions == null
                ? AgentOptions.defaultOptions()
                : agentOptions;

        this.ragPromptAugmentor = new RagPromptAugmentor(contentRetriever);
        this.agentLogger = new AgentLogger(this.agentOptions.logEnabled());

        if (toolMetadataList != null) {
            this.toolMetadataList.addAll(toolMetadataList);
        }

        this.agentToolExecutor = new AgentToolExecutor(
                this.toolMetadataList,
                this.agentOptions,
                this.agentLogger);
    }

    public String chat(String userMessage) {
        List<ChatMessage> messages = new ArrayList<ChatMessage>();

        if (chatMemory != null) {
            messages.addAll(chatMemory.messages());
        }

        int newMessageStartIndex = messages.size();

        String augmentUserMessage = ragPromptAugmentor.augment(userMessage);

        agentLogger.info("userMessage original length=" + userMessage.length()
                + ", snippet='" + userMessage.replaceAll("\n", " ")
                        .substring(0, Math.min(240, userMessage.length()))
                + "'");
        agentLogger.info("userMessage augmented length=" + augmentUserMessage.length()
                + ", snippet='" + augmentUserMessage.replaceAll("\n", " ")
                        .substring(0, Math.min(240, augmentUserMessage.length()))
                + "'");

        messages.add(new UserMessage(augmentUserMessage));
        agentLogger.info("最终构建用户消息 length=" + augmentUserMessage.length()
                + ", snippet='" + augmentUserMessage.replaceAll("\n", " ")
                        .substring(0, Math.min(240, augmentUserMessage.length()))
                + "'");

        String finalResponse = runAgentLoop(messages);

        saveNewMessage(messages, newMessageStartIndex, new UserMessage(userMessage));

        return finalResponse;

    }

    private String runAgentLoop(List<ChatMessage> messages) {
        agentLogger.info("""
                Agent Loop 启动,maxAgentSteps= %s,temperature= %s,enableThinking= %s,tools= %s
                """.formatted(agentOptions.maxAgentSteps(), agentOptions.temperature(), agentOptions.enableThinking(),
                toolMetadataList.size()));

        for (int step = 1; step <= agentOptions.maxAgentSteps(); step++) {

            if (messages.size() > 50) {
                throw new RuntimeException("Agent 消息数量异常增长，已终止。message.size=" + messages.size());
            }

            agentLogger.step(step, "构建ChatRequest,messages.size=" + messages.size());

            ChatRequest request = promptBuilder.buildRequest(
                    chatModel.modelName(),
                    systemMessage,
                    messages,
                    toolMetadataList,
                    agentOptions.temperature(),
                    agentOptions.maxTokens(),
                    agentOptions.enableThinking());

            agentLogger.step(step, "开始请求模型");

            ChatModelResponse response = chatModel.chat(request);

            agentLogger.step(step, "模型响应完成，finishResponse=" + response.finishReason());

            if (!response.hasToolCalls()) {
                String aiResult = response.contentOrEmpty();

                agentLogger.step(step, "模型返回最终结果Agent Loop");

                messages.add(new AssistantMessage(aiResult));
                return aiResult;

            }

            agentLogger.step(step, "模型请求调用工具，数量=" + response.toolCalls().size());

            AssistantToolCallMessage assistantToolCallMessage = new AssistantToolCallMessage(
                    response.contentOrEmpty(),
                    response.toolCalls());

            messages.add(assistantToolCallMessage);

            messages.addAll(agentToolExecutor.execute(response.toolCalls(), step));

        }

        throw new RuntimeException(
                "Agent Loop 超过最大执行步数:"
                        + agentOptions.maxAgentSteps()
                        + "可能出现了循环调用工具");
    }

    private void saveNewMessage(
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

}
