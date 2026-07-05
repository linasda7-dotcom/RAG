package com.example.agent.demo;

import com.example.agent.core.service.TokenStream;

public interface StreamingAssistant {
    TokenStream chat(String message);
}
