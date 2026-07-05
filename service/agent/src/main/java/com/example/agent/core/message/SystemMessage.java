package com.example.agent.core.message;

public class SystemMessage extends ChatMessage {

    public SystemMessage(String content) {
        super("system",content);
    }

    @Override
    public String role() {
        return "system";
    }

}
