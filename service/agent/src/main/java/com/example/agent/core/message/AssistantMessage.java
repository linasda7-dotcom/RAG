package com.example.agent.core.message;

public class AssistantMessage extends ChatMessage {

    public AssistantMessage(String content) {
        super("assistant", content);
    }

    @Override
    public String role() {
        return "assistant";
    }

}
