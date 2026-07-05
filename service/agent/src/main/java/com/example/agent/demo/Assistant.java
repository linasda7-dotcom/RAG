package com.example.agent.demo;

import com.example.agent.core.service.TokenStream;

public interface Assistant {
    String chat(String message);

    TokenStream stream(String message);
}
