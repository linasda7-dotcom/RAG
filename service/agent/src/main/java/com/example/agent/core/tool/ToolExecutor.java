package com.example.agent.core.tool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ToolExecutor {

    private final List<ToolMetadata> toolMetadataList;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ToolExecutor(List<ToolMetadata> toolMetadataList) {
        this.toolMetadataList = toolMetadataList;
    }

    // 工具执行器
    public String execute(ToolCall toolCall) {

        // 找到工具并且获取工具元数据
        ToolMetadata toolMetadata = findTool(toolCall.name());

        try {
            // 工具方法体
            Method method = toolMetadata.method();

            List<ToolParameterMetadata> parameters = toolMetadata.parameters();

            // 如果方法参数不是1抛出异常
            if (method.getParameterCount() != parameters.size()) {
                throw new RuntimeException("工具方法参数数量与元数据数量不一致:" + toolCall.name());
            }

            // 所有参数
            Map<String, Object> argumentMap = parseArguments(toolCall.arguments());

            Object[] args = new Object[parameters.size()];

            // 循环参数总长度拿到值
            for (int i = 0; i < parameters.size(); i++) {
                ToolParameterMetadata parameter = parameters.get(i);
                args[i] = convertArgument(argumentMap, parameter);
            }

            Object result = method.invoke(
                    toolMetadata.target(), // 方法元数据目标
                    args);// 方法参数

            // 返回工具执行结果
            return String.valueOf(result);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            throw new RuntimeException("工具内部执行失败:" + toolCall.name() + ",原因:" + targetException.getLocalizedMessage());
        } catch (Exception e) {
            throw new RuntimeException("工具执行失败:" + toolCall.name(), e);
        }
    }

    // 解析参数
    private Map<String, Object> parseArguments(String json) {
        try {
            return objectMapper.readValue(json,
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (Exception e) {
            throw new RuntimeException("工具参数JSON解析失败:" + json, e);
        }
    }

    // 转换参数
    private Object convertArgument(Map<String, Object> argumentMap, ToolParameterMetadata parameter) {

        Object value = argumentMap.get(parameter.name());

        if (value == null) {
            throw new RuntimeException("工具参数缺失:" + parameter.name());
        }

        // 判断返回值的类型
        Class<?> targetType = parameter.type();

        if (targetType == String.class) {
            return String.valueOf(value);
        }

        if (targetType == int.class || targetType == Integer.class) {
            if (value instanceof Number number) {
                return number.intValue();
            }
            return Integer.parseInt(String.valueOf(value));
        }

        if (targetType == long.class || targetType == Long.class) {
            if (value instanceof Number number) {
                return number.longValue();
            }
            return Long.parseLong(String.valueOf(value));
        }

        if (targetType == double.class || targetType == Double.class) {
            if (value instanceof Number number) {
                return number.doubleValue();
            }
            return Double.parseDouble(String.valueOf(value));
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Boolean bool) {
                return bool;
            }
            return Boolean.parseBoolean(String.valueOf(value));
        }

        throw new RuntimeException("暂不支持的工具参数类型:" + targetType.getName());
    }

    // 查找工具
    private ToolMetadata findTool(String toolName) {

        return toolMetadataList.stream()
                .filter(tool -> tool.name().equals(toolName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("找不到工具：" + toolName));
    }
}
