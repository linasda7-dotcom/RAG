package com.example.agent.core.tool;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.annotation.Tool;
import com.example.agent.core.annotation.ToolParam;

public class ToolScanner {
    public static List<ToolMetadata> scan(Object toolObject) {
        List<ToolMetadata> tools = new ArrayList<>();

        Class<?> clazz = toolObject.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Tool.class)) {
                method.setAccessible(true);
                Tool tool = method.getAnnotation(Tool.class);

                String name = method.getName();
                String description = tool.value();
                List<ToolParameterMetadata> parameters = scanParameters(method);
                tools.add(new ToolMetadata(
                        name,
                        description,
                        toolObject,
                        method,
                        parameters));
            }
        }
        return tools;
    }

    private static List<ToolParameterMetadata> scanParameters(Method method) {
        List<ToolParameterMetadata> parameters = new ArrayList<ToolParameterMetadata>();

        Parameter[] reflectParameters = method.getParameters();

        for (Parameter parameter : reflectParameters) {
            // 方法参数
            ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
            if (toolParam == null) {
                throw new RuntimeException("工具方法参数缺少@ToolParam注解" + method.getName());
            }
            parameters.add(new ToolParameterMetadata(toolParam.value(), parameter.getType()));
        }
        return parameters;
    }
}
