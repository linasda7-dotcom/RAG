package com.example.agent.core.tool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//TODO 后续需要升级减少map的使用
public class ToolSchemaBuilder {
    public static List<Map<String, Object>> build(List<ToolMetadata> tools) {
        List<Map<String, Object>> schemas = new ArrayList<>();

        // 如果数组为空那么直接返回空数组
        if (tools == null || tools.isEmpty()) {
            return schemas;
        }

        tools.forEach(tool -> {
            schemas.add(buildToolSchema(tool));
        });

        return schemas;
    }

    private static Map<String, Object> buildToolSchema(ToolMetadata tool) {
        // 根
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("type", "function");

        // 函数表述
        Map<String, Object> function = new LinkedHashMap<>();
        function.put("name", tool.name());
        function.put("description", tool.description());
        function.put("parameters", buildParametersSchema(tool.parameters()));

        root.put("function", function);
        return root;
    }

    private static Map<String, Object> buildParametersSchema(List<ToolParameterMetadata> parameters) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        if (parameters != null) {
            parameters.forEach(parameter -> {
                Map<String, Object> property = new LinkedHashMap<>();
                property.put("type", toJsonSchemaType(parameter.type()));

                properties.put(parameter.name(), property);
                required.add(parameter.name());
            });
        }

        schema.put("properties", properties);
        schema.put("required", required);

        return schema;
    }

    private static String toJsonSchemaType(Class<?> type) {
        if (type == String.class) {
            return "string";
        }

        if (type == int.class ||
                type == Integer.class || type == long.class || type == Long.class) {
            return "integer";
        }

        if (type == double.class ||
                type == Double.class || type == float.class || type == Float.class) {
            return "number";
        }

        if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        }
        return "string";
    }
}
