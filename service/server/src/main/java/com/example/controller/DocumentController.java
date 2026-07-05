package com.example.controller;

import com.example.dto.Result;
import com.example.dto.response.DocumentResponse;
import com.example.service.DocumentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/document")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public Result<DocumentResponse> upload(
            @RequestParam("kbId") Long kbId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            return Result.success(documentService.upload(kbId, file, userId));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list/{kbId}")
    public Result<List<DocumentResponse>> listByKbId(@PathVariable Long kbId) {
        return Result.success(documentService.listByKbId(kbId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            documentService.delete(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
