package com.example.agent.demo;

import com.example.agent.core.annotation.Tool;
import com.example.agent.core.annotation.ToolParam;

public class CalculatorTool {
    @Tool("计算两个整数之和")
    public int add(@ToolParam("a") int a, @ToolParam("b") int b) {
        return a + b;
    }
}
