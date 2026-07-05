package com.example.agent.provider.openai;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.example.agent.core.tool.ToolCall;
import com.example.agent.provider.openai.dto.stream.OpenAiStreamToolCall;
import com.example.agent.provider.openai.dto.stream.OpenAiStreamToolFunction;

public class OpenAiStreamToolCallAccumulator {

    private final Map<Integer, ToolCallBuilder> builders = new HashMap<>();

    public void append(List<OpenAiStreamToolCall> toolCallDeltas) {
        if (toolCallDeltas == null || toolCallDeltas.isEmpty()) {
            return;
        }

        for (OpenAiStreamToolCall delta : toolCallDeltas) {
            if (delta == null) {
                continue;
            }

            Integer index = delta.index();
            if (index == null) {
                index = 0;
            }

            ToolCallBuilder builder = builders.computeIfAbsent(
                    index,
                    ignored -> new ToolCallBuilder());

            if (delta.id() != null && !delta.id().isBlank()) {
                builder.id = delta.id();
            }

            if (delta.type() != null && !delta.type().isBlank()) {
                builder.type = delta.type();
            }

            OpenAiStreamToolFunction function = delta.function();

            if (function == null) {
                continue;
            }

            if (function.name() != null && !function.name().isBlank()) {
                builder.name = function.name();
            }

            if (function.arguments() != null && !function.arguments().isEmpty()) {
                builder.arguments.append(function.arguments());
            }
        }
    }

    public boolean hasToolCalls() {
        return !builders.isEmpty();
    }

    public List<ToolCall> toToolCalls() {
        return builders.entrySet()
                .stream()
                .sorted(Comparator.comparingInt((Entry<Integer, ?> entry) -> entry.getKey()))
                .map(entry -> entry.getValue().build())
                .toList();
    }

    public void clear() {
        builders.clear();
    }

    private static class ToolCallBuilder {
        private String id;
        @SuppressWarnings("unused")
        private String type;
        private String name;
        private final StringBuilder arguments = new StringBuilder();

        private ToolCall build() {
            if (id == null || id.isBlank()) {
                throw new RuntimeException("流式工具调用缺少 id");
            }

            if (name == null || name.isBlank()) {
                throw new RuntimeException("流式工具调用缺少 function.name");
            }

            return new ToolCall(
                    id,
                    name,
                    arguments.toString());
        }

    }
}