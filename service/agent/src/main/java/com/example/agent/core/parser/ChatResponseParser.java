package com.example.agent.core.parser;

import com.example.agent.core.model.ChatModelResponse;

public interface ChatResponseParser {
    ChatModelResponse parse(String responseJson);
}
