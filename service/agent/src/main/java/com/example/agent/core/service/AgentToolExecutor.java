package com.example.agent.core.service;

import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.agent.AgentLogger;
import com.example.agent.core.agent.AgentOptions;
import com.example.agent.core.message.ToolMessage;
import com.example.agent.core.tool.ToolCall;
import com.example.agent.core.tool.ToolExecutor;
import com.example.agent.core.tool.ToolMetadata;

public class AgentToolExecutor {
    private final AgentOptions agentOptions;
    private final AgentLogger agentLogger;
    private final ToolExecutor toolExecutor;

    public AgentToolExecutor(
            List<ToolMetadata> toolMetadataList,
            AgentOptions agentOptions,
            AgentLogger agentLogger) {

        List<ToolMetadata> safeToolMetadataList = toolMetadataList == null
                ? List.of()
                : new ArrayList<>(toolMetadataList);

        this.agentOptions = agentOptions == null
                ? AgentOptions.defaultOptions()
                : agentOptions;

        this.agentLogger = agentLogger == null
                ? new AgentLogger(this.agentOptions.logEnabled())
                : agentLogger;

        this.toolExecutor = new ToolExecutor(safeToolMetadataList);
    }

    public List<ToolMessage> execute(
            List<ToolCall> toolCalls,
            int step) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            throw new RuntimeException("模型返回了工具调用，但toolCalls为空");
        }

        agentLogger.step(step, "开始执行工具调用");

        List<ToolMessage> toolMessages = new ArrayList<ToolMessage>();

        for (ToolCall toolCall : toolCalls) {
            String toolResult = executeSingleToolCall(toolCall);

            ToolMessage toolMessage = new ToolMessage(
                    toolCall.id(),
                    toolCall.name(),
                    toolResult);

            toolMessages.add(toolMessage);

        }
        agentLogger.step(step, "工具调用执行完成");

        return toolMessages;
    }

    private String executeSingleToolCall(ToolCall toolCall) {
        if (toolCall == null) {
            throw new RuntimeException("toolCall不能为空");
        }

        try {
            agentLogger.tool(toolCall.name(), "开始执行，arguments=" + toolCall.arguments());

            String result = toolExecutor.execute(toolCall);

            agentLogger.tool(toolCall.name(), "执行成功,result=" + result);

            return result;
        } catch (Exception e) {
            agentLogger.error("工具执行失败:" + toolCall.name(), e);

            if (agentOptions.failFastOnToolError()) {
                throw new RuntimeException(
                        "工具执行失败:"
                                + toolCall.name()
                                + ",arguments="
                                + toolCall.arguments(),
                        e);
            }

            return "工具执行失败:" + e.getMessage();
        }
    }

}
