package com.example.agent.core.memory;

import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.message.ChatMessage;

public class MessageWindowChatMemory implements ChatMemory {
    private final int maxMessages;

    private final List<ChatMessage> messages = new ArrayList<>();

    public MessageWindowChatMemory() {
        this(10);
    }

    public MessageWindowChatMemory(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    @Override
    public void add(ChatMessage message) {
        messages.add(message);
        ensureMaxMessages();
    }

    @Override
    public List<ChatMessage> messages() {
        ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>(this.messages);
        return messages;
    }

    private void ensureMaxMessages() {
        while (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }

}
