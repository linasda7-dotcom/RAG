package com.example.repository;

import com.example.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    List<KnowledgeBase> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    List<KnowledgeBase> findAllByOrderByCreatedAtDesc();
}
