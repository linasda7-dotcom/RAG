package com.example.agent.core.message;

public class UserMessage extends ChatMessage {

    public UserMessage(String content) {
        super("user", content);
    }

    @Override
    public String role() {
        return "user";
    }

}