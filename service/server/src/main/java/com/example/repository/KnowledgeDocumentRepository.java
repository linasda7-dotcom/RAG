package com.example.repository;

import com.example.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findByKbIdOrderByCreatedAtDesc(Long kbId);

    long countByKbId(Long kbId);
}
