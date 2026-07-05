package com.example.controller;

import com.example.dto.Result;
import com.example.dto.request.ChatRequest;
import com.example.dto.response.ChatHistoryResponse;
import com.example.dto.response.ChatResponse;
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
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
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
                chatService.chatStream(request, userId)
                        .onPartialResponse(token -> {
                            try {
                                emitter.send(SseEmitter.event().name("token").data(token));
                            } catch (Exception e) {
                                emitter.completeWithError(e);
                            }
                        })
                        .onCompleteResponse(completeResponse -> {
                            try {
                                emitter.send(SseEmitter.event().name("done").data(completeResponse));
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
}
