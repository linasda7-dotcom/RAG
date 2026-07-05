package com.example.agent.provider.openai;

import java.util.List;

import com.example.agent.core.model.ChatModelResponse;
import com.example.agent.core.parser.ChatResponseParser;
import com.example.agent.core.tool.ToolCall;
import com.example.agent.provider.openai.dto.response.OpenAiChatResponse;
import com.example.agent.provider.openai.dto.response.OpenAiChoice;
import com.example.agent.provider.openai.dto.response.OpenAiResponseMessage;
import com.example.agent.provider.openai.dto.response.OpenAiToolFunction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenAiChatResponseParser implements ChatResponseParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatModelResponse parse(String responseJson) {
        try {
            OpenAiChatResponse response = objectMapper.readValue(responseJson, OpenAiChatResponse.class);
            OpenAiChoice choice = firstChoice(response);

            OpenAiResponseMessage message = choice.message();

            if (message.toolCalls() != null && !message.toolCalls().isEmpty()) {
                List<ToolCall> toolCalls = message.toolCalls()
                        .stream()
                        .map(toolCall -> {
                            if (toolCall.function() == null) {
                                throw new RuntimeException("OpenAI 工具调用中没有function");
                            }
                            OpenAiToolFunction function = toolCall.function();
                            return new ToolCall(toolCall.id(), function.name(), function.arguments());
                        }).toList();

                return ChatModelResponse.toolCall(message.content(), toolCalls);
            }

            return new ChatModelResponse(
                    message.content() == null ? "" : message.content(),
                    List.of(),
                    choice.finishReason());

        } catch (JsonMappingException e) {
            throw new RuntimeException("字段映射失败：" + e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析失败：" + e);
        }
    }

    private OpenAiChoice firstChoice(OpenAiChatResponse response) {
        if (response == null) {
            throw new RuntimeException("OpenAI 响应为空");
        }

        List<OpenAiChoice> choices = response.choices();

        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("OpenAI 响应中没有 choices");
        }

        OpenAiChoice firstChoice = choices.get(0);

        if (firstChoice == null) {
            throw new RuntimeException("OpenAI 响应中 choices[0] 为空");
        }

        if (firstChoice.message() == null) {
            throw new RuntimeException("OpenAI 响应中 choices[0].message 为空");
        }
        return firstChoice;
    }
}