package com.example.repository;

import com.example.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    List<KnowledgeBase> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    List<KnowledgeBase> findAllByOrderByCreatedAtDesc();
}
