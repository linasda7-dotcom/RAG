package com.example.agent.demo;

import com.example.agent.core.annotation.Tool;
import com.example.agent.core.annotation.ToolParam;

public class WeatherTool {

    @Tool("查询城市天气")
    public String weather(@ToolParam("city") String city, @ToolParam("date") String date) {
        return city + date + "多云，28°C";
    }
}
