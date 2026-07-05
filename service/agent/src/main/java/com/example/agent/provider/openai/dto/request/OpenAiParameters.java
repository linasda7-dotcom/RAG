package com.example.agent.provider.openai.dto.request;

import java.util.List;
import java.util.Map;

public record OpenAiParameters(
        String type,
        Map<String, OpenAiProperty> properties,
        List<String> required) {

}
