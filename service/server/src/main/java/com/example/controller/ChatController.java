package com.example.controller;

import com.example.dto.Result;
import com.example.dto.request.ChatRequest;
import com.example.dto.response.ChatHistoryResponse;
import com.example.dto.response.ChatResponse;
import com.example.entity.ChatHistory;
import com.example.repository.ChatHistoryRepository;
import com.example.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatHistoryRepository chatHistoryRepository;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ChatController(ChatService chatService, ChatHistoryRepository chatHistoryRepository) {
        this.chatService = chatService;
        this.chatHistoryRepository = chatHistoryRepository;
    }

    @PostMapping("/ask")
    public Result<ChatResponse> ask(@RequestBody ChatRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            return Result.success(chatService.chat(request, userId));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody ChatRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        SseEmitter emitter = new SseEmitter(120000L);

        executorService.execute(() -> {
            try {
                // 用于收集完整的响应内容
                StringBuilder fullResponse = new StringBuilder();

                chatService.chatStream(request, userId)
                        .onPartialResponse(token -> {
                            try {
                                fullResponse.append(token);
                                emitter.send(SseEmitter.event().name("token").data(token));
                            } catch (Exception e) {
                                emitter.completeWithError(e);
                            }
                        })
                        .onCompleteResponse(sessionId -> {
                            try {
                                // 保存聊天记录
                                ChatHistory history = new ChatHistory();
                                history.setUserId(userId);
                                history.setKbId(request.kbId());
                                history.setSessionId(sessionId);
                                history.setQuestion(request.question());
                                history.setAnswer(fullResponse.toString());
                                chatHistoryRepository.save(history);

                                // 发送sessionId给前端
                                emitter.send(SseEmitter.event().name("done").data(sessionId));
                                emitter.complete();
                            } catch (Exception e) {
                                emitter.completeWithError(e);
                            }
                        })
                        .onError(error -> emitter.completeWithError(error))
                        .start();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @GetMapping("/history")
    public Result<List<ChatHistoryResponse>> getHistory(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String sessionId) {
        return Result.success(chatService.getHistory(userId, sessionId));
    }

    @GetMapping("/history/kb/{kbId}")
    public Result<List<ChatHistoryResponse>> getHistoryByKb(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long kbId) {
        return Result.success(chatService.getHistoryByKb(userId, kbId));
    }

    @DeleteMapping("/memory")
    public Result<Void> clearMemory(@RequestHeader("X-User-Id") Long userId) {
        chatService.clearUserMemory(userId);
        return Result.success(null);
    }
}
