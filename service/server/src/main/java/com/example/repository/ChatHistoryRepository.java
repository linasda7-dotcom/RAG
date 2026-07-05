package com.example.repository;

import com.example.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    List<ChatHistory> findByUserIdAndSessionIdOrderByCreatedAtAsc(Long userId, String sessionId);

    List<ChatHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ChatHistory> findByUserIdAndKbIdOrderByCreatedAtDesc(Long userId, Long kbId);
}
