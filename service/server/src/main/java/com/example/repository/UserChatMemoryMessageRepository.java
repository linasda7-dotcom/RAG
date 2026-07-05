package com.example.repository;

import com.example.entity.UserChatMemoryMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserChatMemoryMessageRepository extends JpaRepository<UserChatMemoryMessage, Long> {

    List<UserChatMemoryMessage> findByUserIdOrderByMessageOrderAsc(Long userId);

    void deleteByUserId(Long userId);
}
