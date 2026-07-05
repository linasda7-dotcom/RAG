package com.example.service;

import com.example.dto.response.DocumentResponse;
import com.example.entity.KnowledgeDocument;
import com.example.repository.KnowledgeDocumentRepository;
import com.example.agent.core.rag.document.Document;
import com.example.agent.core.rag.document.DocumentSplitter;
import com.example.agent.core.rag.document.DocumentByCharacterSplitter;
import com.example.agent.core.rag.document.TextSegment;
import com.example.agent.core.rag.document.parser.TextDocumentParser;
import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.core.rag.embedding.EmbeddingModel;
import com.example.agent.core.rag.embedding.EmbeddingStore;
import com.example.agent.core.rag.ingestion.EmbeddingStoreIngestor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeBaseService knowledgeBaseService;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore embeddingStore;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public DocumentService(KnowledgeDocumentRepository documentRepository,
            KnowledgeBaseService knowledgeBaseService,
            EmbeddingModel embeddingModel,
            EmbeddingStore embeddingStore) {
        this.documentRepository = documentRepository;
        this.knowledgeBaseService = knowledgeBaseService;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    @Transactional
    public DocumentResponse upload(Long kbId, MultipartFile file, Long userId) {
        // 保存文件到磁盘
        String fileName = file.getOriginalFilename();
        String fileType = fileName != null && fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf(".") + 1)
                : "txt";
        String storedFileName = UUID.randomUUID() + "." + fileType;
        Path filePath;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            filePath = uploadPath.resolve(storedFileName);
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage(), e);
        }

        // 创建文档记录
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setKbId(kbId);
        doc.setFileName(fileName);
        doc.setFileType(fileType);
        doc.setFileSize(file.getSize());
        doc.setFilePath(filePath.toString());
        doc.setUploadedBy(userId);
        doc.setStatus("processing");
        doc = documentRepository.save(doc);

        // 异步处理文档向量化
        try {
            processDocument(doc);
            doc.setStatus("completed");
        } catch (Exception e) {
            doc.setStatus("failed");
        }
        doc = documentRepository.save(doc);

        // 更新知识库文档计数
        knowledgeBaseService.updateDocCount(kbId);

        return toResponse(doc);
    }

    private void processDocument(KnowledgeDocument doc) {
        try {
            Path path = Paths.get(doc.getFilePath());
            String content = Files.readString(path);

            Document document = Document.from(content);

            DocumentSplitter splitter = new DocumentByCharacterSplitter(500, 50);
            List<TextSegment> segments = splitter.split(document);

            doc.setChunkCount(segments.size());

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(splitter)
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            ingestor.ingest(document);

        } catch (IOException e) {
            throw new RuntimeException("文档处理失败: " + e.getMessage(), e);
        }
    }

    public List<DocumentResponse> listByKbId(Long kbId) {
        return documentRepository.findByKbIdOrderByCreatedAtDesc(kbId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        KnowledgeDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        documentRepository.deleteById(id);
        knowledgeBaseService.updateDocCount(doc.getKbId());
    }

    private DocumentResponse toResponse(KnowledgeDocument doc) {
        return new DocumentResponse(
                doc.getId(),
                doc.getKbId(),
                doc.getFileName(),
                doc.getFileType(),
                doc.getFileSize(),
                doc.getChunkCount(),
                doc.getStatus(),
                doc.getCreatedAt());
    }
}
