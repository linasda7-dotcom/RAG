package com.example.agent.core.tool;

import java.lang.reflect.Method;
import java.util.List;

public record ToolMetadata(String name, String description, Object target, Method method,
                List<ToolParameterMetadata> parameters) {
}
