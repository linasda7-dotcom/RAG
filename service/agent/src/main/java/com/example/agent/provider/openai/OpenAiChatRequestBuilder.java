package com.example.agent.provider.openai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.agent.core.message.AssistantToolCallMessage;
import com.example.agent.core.message.ChatMessage;
import com.example.agent.core.message.ToolMessage;
import com.example.agent.core.request.ChatRequest;
import com.example.agent.core.request.ChatRequestBuilder;
import com.example.agent.core.tool.ToolCall;
import com.example.agent.core.tool.ToolParameterMetadata;
import com.example.agent.provider.openai.dto.request.OpenAiChatRequest;
import com.example.agent.provider.openai.dto.request.OpenAiFunction;
import com.example.agent.provider.openai.dto.request.OpenAiMessage;
import com.example.agent.provider.openai.dto.request.OpenAiParameters;
import com.example.agent.provider.openai.dto.request.OpenAiProperty;
import com.example.agent.provider.openai.dto.request.OpenAiRequestToolCall;
import com.example.agent.provider.openai.dto.request.OpenAiRequestToolFunction;
import com.example.agent.provider.openai.dto.request.OpenAiTool;

public class OpenAiChatRequestBuilder implements ChatRequestBuilder<OpenAiChatRequest> {

    @Override
    public OpenAiChatRequest build(ChatRequest request) {
        return build(request, null);
    }

    public OpenAiChatRequest build(ChatRequest request, Boolean stream) {
        OpenAiChatRequest openAiChatRequest = new OpenAiChatRequest(
                request.model(),
                request.temperature(),
                request.maxTokens(),
                request.enableThinking(),
                stream,
                buildMessage(request),
                buildTools(request));
        return openAiChatRequest;
    }

    private static List<OpenAiMessage> buildMessage(ChatRequest request) {
        List<OpenAiMessage> messages = new ArrayList<>();

        // 构造系统提示词
        if (request.systemMessage() != null && !request.systemMessage().isBlank()) {
            messages.add(new OpenAiMessage(
                    "system",
                    request.systemMessage(),
                    null,
                    null));
        }

        // 构造消息
        if (request.messages() != null) {
            request.messages().forEach(msg -> {
                messages.add(buildMessage(msg));
            });
        }
        return messages;
    }

    private static OpenAiMessage buildMessage(ChatMessage message) {
        if (message instanceof AssistantToolCallMessage assistantToolCallMessage) {
            return new OpenAiMessage(
                    "assistant",
                    assistantToolCallMessage.content(),
                    null,
                    buildRequestToolCalls(assistantToolCallMessage.toolCalls()));
        }

        if (message instanceof ToolMessage toolMessage) {
            return new OpenAiMessage(
                    "tool",
                    toolMessage.content(),
                    toolMessage.toolCallId(),
                    null);
        }
        return new OpenAiMessage(
                message.role(),
                message.content(),
                null,
                null);

    }

    private static List<OpenAiRequestToolCall> buildRequestToolCalls(List<ToolCall> toolCalls) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            return List.of();
        }

        List<OpenAiRequestToolCall> result = new ArrayList<OpenAiRequestToolCall>();

        for (ToolCall toolCall : toolCalls) {
            OpenAiRequestToolFunction function = new OpenAiRequestToolFunction(
                    toolCall.name(),
                    toolCall.arguments());

            OpenAiRequestToolCall openAiRequestToolCall = new OpenAiRequestToolCall(
                    toolCall.id(),
                    "function",
                    function);
            result.add(openAiRequestToolCall);
        }
        return result;
    }

    private static List<OpenAiTool> buildTools(ChatRequest request) {
        if (request.tools() == null || request.tools().isEmpty()) {
            return null;
        }

        List<OpenAiTool> tools = new ArrayList<OpenAiTool>();

        request.tools().forEach(tool -> {
            OpenAiFunction function = new OpenAiFunction(
                    tool.name(),
                    tool.description(),
                    buildParameters(tool.parameters()));
            tools.add(new OpenAiTool("function", function));
        });

        return tools;
    }

    private static OpenAiParameters buildParameters(List<ToolParameterMetadata> parameters) {
        Map<String, OpenAiProperty> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        if (parameters != null) {
            parameters.forEach(parameter -> {
                properties.put(parameter.name(),
                        new OpenAiProperty(toJsonSchemaType(
                                parameter.type())));
                required.add(parameter.name());
            });
        }

        return new OpenAiParameters("object", properties, required);
    }

    private static String toJsonSchemaType(Class<?> type) {
        if (type == String.class) {
            return "string";
        }

        if (type == int.class || type == Integer.class || type == long.class || type == Long.class) {
            return "integer";
        }

        if (type == double.class || type == Double.class || type == float.class || type == Float.class) {
            return "number";
        }

        if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        }
        return "string";
    }

}
