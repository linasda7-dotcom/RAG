package com.example.agent.core.memory;

import java.util.List;

import com.example.agent.core.message.ChatMessage;

public interface ChatMemory {
    void add(ChatMessage message);

    List<ChatMessage> messages();
}
