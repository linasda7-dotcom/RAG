package com.example.controller;

import com.example.dto.Result;
import com.example.dto.request.CreateKnowledgeBaseRequest;
import com.example.dto.response.KnowledgeBaseResponse;
import com.example.service.KnowledgeBaseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kb")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @PostMapping
    public Result<KnowledgeBaseResponse> create(
            @RequestBody CreateKnowledgeBaseRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            return Result.success(knowledgeBaseService.create(request, userId));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping
    public Result<List<KnowledgeBaseResponse>> list(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(knowledgeBaseService.listByUser(userId));
    }

    @GetMapping("/all")
    public Result<List<KnowledgeBaseResponse>> listAll() {
        return Result.success(knowledgeBaseService.listAll());
    }

    @GetMapping("/{id}")
    public Result<KnowledgeBaseResponse> getById(@PathVariable Long id) {
        try {
            return Result.success(knowledgeBaseService.getById(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        try {
            knowledgeBaseService.delete(id, userId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
