package com.example.agent.core.message;

public abstract class ChatMessage {
    private final String content;
    private final String role;

    protected ChatMessage(String role, String content) {
        this.content = content;
        this.role = role;
    }

    public String content() {
        return content;
    }

    public String role() {
        return role;
    };
}
