package com.example.agent.provider.openai;

import java.util.List;

import com.example.agent.core.request.ChunkType;
import com.example.agent.provider.openai.dto.stream.OpenAiStreamChoice;
import com.example.agent.provider.openai.dto.stream.OpenAiStreamDelta;
import com.example.agent.provider.openai.dto.stream.OpenAiStreamResponse;
import com.example.agent.provider.openai.dto.stream.OpenAiStreamToolCall;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenAiStreamParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StreamChunk parse(String data) {
        try {

            if (data == null || data.isBlank()) {
                return StreamChunk.empty();
            }

            // 修剪
            String trimmed = data.trim();
            if ("[DONE]".equals(trimmed)) {
                return StreamChunk.done();
            }

            OpenAiStreamResponse response = objectMapper.readValue(trimmed, OpenAiStreamResponse.class);

            List<OpenAiStreamChoice> choices = response.choices();
            if (choices == null || choices.isEmpty()) {
                return StreamChunk.empty();
            }

            OpenAiStreamChoice openAiStreamChoice = choices.get(0);
            if (openAiStreamChoice == null || openAiStreamChoice.delta() == null) {
                return StreamChunk.empty();
            }

            OpenAiStreamDelta delta = openAiStreamChoice.delta();
            if (delta.toolCalls() != null && !delta.toolCalls().isEmpty()) {
                return StreamChunk.toolCallDelta(delta.toolCalls());
            }

            if (delta.reasoningContent() != null && !delta.reasoningContent().isEmpty()) {
                return StreamChunk.reasoningContent(delta.reasoningContent());
            }

            if (delta.content() != null && !delta.content().isEmpty()) {
                return StreamChunk.content(delta.content());
            }

            return StreamChunk.empty();

        } catch (Exception e) {
            throw new RuntimeException("解析 OpenAI Stream 数据失败: " + data, e);
        }
    }

    public record StreamChunk(
            String content,
            ChunkType type,
            List<OpenAiStreamToolCall> toolCalls) {

        public static StreamChunk reasoningContent(String reasoningContent) {
            return new StreamChunk(reasoningContent, ChunkType.REASONING, List.of());
        }

        public static StreamChunk content(String content) {
            return new StreamChunk(content, ChunkType.CONTENT, List.of());
        }

        public static StreamChunk toolCallDelta(List<OpenAiStreamToolCall> toolCalls) {
            return new StreamChunk(
                    "",
                    ChunkType.TOOL_CALL_DELTA,
                    toolCalls);
        }

        public static StreamChunk done() {
            return new StreamChunk("", ChunkType.DONE, List.of());
        }

        public static StreamChunk empty() {
            return new StreamChunk("", ChunkType.EMPTY, List.of());
        }

        public boolean isReasoning() {
            return type == ChunkType.REASONING;
        }

        public boolean isDone() {
            return type == ChunkType.DONE;
        }

        public boolean isContent() {
            return type == ChunkType.CONTENT;
        }

        public boolean isToolCallDelta() {
            return type == ChunkType.TOOL_CALL_DELTA;
        }

        public boolean hasContent() {
            return content != null && !content.isEmpty();
        }

        public boolean hasCalls() {
            return toolCalls != null && !toolCalls.isEmpty();
        }
    }
}