package com.example.service;

import com.example.agent.core.memory.ChatMemory;
import com.example.agent.core.message.AssistantMessage;
import com.example.agent.core.message.ChatMessage;
import com.example.agent.core.message.UserMessage;
import com.example.entity.UserChatMemoryMessage;
import com.example.repository.UserChatMemoryMessageRepository;

import java.util.List;

public class MysqlChatMemory implements ChatMemory {

    private final UserChatMemoryMessageRepository repository;
    private final Long userId;
    private final int maxMessages;

    public MysqlChatMemory(UserChatMemoryMessageRepository repository, Long userId, int maxMessages) {
        this.repository = repository;
        this.userId = userId;
        this.maxMessages = maxMessages;
    }

    @Override
    public void add(ChatMessage message) {
        if (message == null) {
            return;
        }

        List<UserChatMemoryMessage> existing = repository.findByUserIdOrderByMessageOrderAsc(userId);
        int nextOrder = existing.isEmpty() ? 1 : existing.get(existing.size() - 1).getMessageOrder() + 1;

        UserChatMemoryMessage entity = new UserChatMemoryMessage();
        entity.setUserId(userId);
        entity.setRole(message.role());
        entity.setContent(message.content());
        entity.setMessageOrder(nextOrder);
        repository.save(entity);

        trimIfNeeded();
    }

    @Override
    public List<ChatMessage> messages() {
        return repository.findByUserIdOrderByMessageOrderAsc(userId)
                .stream()
                .map(this::toChatMessage)
                .toList();
    }

    private ChatMessage toChatMessage(UserChatMemoryMessage message) {
        if ("user".equals(message.getRole())) {
            return new UserMessage(message.getContent());
        }
        return new AssistantMessage(message.getContent());
    }

    private void trimIfNeeded() {
        List<UserChatMemoryMessage> existing = repository.findByUserIdOrderByMessageOrderAsc(userId);
        if (existing.size() <= maxMessages) {
            return;
        }

        int removeCount = existing.size() - maxMessages;
        for (int i = 0; i < removeCount; i++) {
            repository.delete(existing.get(i));
        }

        List<UserChatMemoryMessage> remaining = repository.findByUserIdOrderByMessageOrderAsc(userId);
        for (int i = 0; i < remaining.size(); i++) {
            UserChatMemoryMessage entity = remaining.get(i);
            entity.setMessageOrder(i + 1);
            repository.save(entity);
        }
    }
}
