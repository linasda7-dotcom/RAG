package com.example.service;

import com.example.dto.request.CreateKnowledgeBaseRequest;
import com.example.dto.response.KnowledgeBaseResponse;
import com.example.entity.KnowledgeBase;
import com.example.repository.KnowledgeBaseRepository;
import com.example.repository.KnowledgeDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final KnowledgeDocumentRepository documentRepository;

    public KnowledgeBaseService(KnowledgeBaseRepository knowledgeBaseRepository,
            KnowledgeDocumentRepository documentRepository) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional
    public KnowledgeBaseResponse create(CreateKnowledgeBaseRequest request, Long userId) {
        KnowledgeBase kb = new KnowledgeBase(request.name(), request.description(), userId);
        kb = knowledgeBaseRepository.save(kb);
        return toResponse(kb);
    }

    public List<KnowledgeBaseResponse> listByUser(Long userId) {
        return knowledgeBaseRepository.findByCreatedByOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<KnowledgeBaseResponse> listAll() {
        return knowledgeBaseRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public KnowledgeBaseResponse getById(Long id) {
        KnowledgeBase kb = knowledgeBaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("知识库不存在"));
        return toResponse(kb);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        KnowledgeBase kb = knowledgeBaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("知识库不存在"));
        if (!kb.getCreatedBy().equals(userId)) {
            throw new RuntimeException("无权删除此知识库");
        }
        knowledgeBaseRepository.deleteById(id);
    }

    @Transactional
    public void updateDocCount(Long kbId) {
        KnowledgeBase kb = knowledgeBaseRepository.findById(kbId)
                .orElseThrow(() -> new RuntimeException("知识库不存在"));
        long count = documentRepository.countByKbId(kbId);
        kb.setDocCount((int) count);
        knowledgeBaseRepository.save(kb);
    }

    private KnowledgeBaseResponse toResponse(KnowledgeBase kb) {
        return new KnowledgeBaseResponse(
                kb.getId(),
                kb.getName(),
                kb.getDescription(),
                kb.getDocCount(),
                kb.getCreatedAt());
    }
}
