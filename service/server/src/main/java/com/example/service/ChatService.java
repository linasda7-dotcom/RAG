package com.example.service;

import com.example.agent.core.memory.ChatMemory;
import com.example.agent.core.memory.MessageWindowChatMemory;
import com.example.agent.core.model.ChatModel;
import com.example.agent.core.model.StreamingChatModel;
import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.core.rag.embedding.EmbeddingModel;
import com.example.agent.core.rag.embedding.EmbeddingSearchRequest;
import com.example.agent.core.rag.embedding.EmbeddingSearchResult;
import com.example.agent.core.rag.embedding.EmbeddingStore;
import com.example.agent.core.rag.retriever.ContentRetriever;
import com.example.agent.core.rag.document.TextSegment;
import com.example.agent.core.service.AiService;
import com.example.agent.core.service.TokenStream;
import com.example.dto.request.ChatRequest;
import com.example.dto.response.ChatResponse;
import com.example.dto.response.ChatHistoryResponse;
import com.example.entity.ChatHistory;
import com.example.entity.KnowledgeBase;
import com.example.repository.ChatHistoryRepository;
import com.example.repository.KnowledgeBaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore embeddingStore;
    private final ChatHistoryRepository chatHistoryRepository;
    private final KnowledgeBaseRepository knowledgeBaseRepository;

    private final Map<String, ChatMemory> sessionMemories = new ConcurrentHashMap<>();

    public ChatService(ChatModel chatModel,
            StreamingChatModel streamingChatModel,
            EmbeddingModel embeddingModel,
            EmbeddingStore embeddingStore,
            ChatHistoryRepository chatHistoryRepository,
            KnowledgeBaseRepository knowledgeBaseRepository) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.chatHistoryRepository = chatHistoryRepository;
        this.knowledgeBaseRepository = knowledgeBaseRepository;
    }

    public ChatResponse chat(ChatRequest request, Long userId) {
        String sessionId = request.sessionId() != null ? request.sessionId() : UUID.randomUUID().toString();

        // 创建带RAG的ContentRetriever
        ContentRetriever contentRetriever = createContentRetriever(request.kbId());

        // 获取或创建会话记忆
        ChatMemory memory = sessionMemories.computeIfAbsent(sessionId, k -> new MessageWindowChatMemory(20));

        // 构建AiService
        KnowledgeAssistant assistant = AiService.builder(KnowledgeAssistant.class)
                .chatModel(chatModel)
                .chatMemory(memory)
                .contentRetriever(contentRetriever)
                .systemMessage("你是一个专业的企业知识库助手。请基于提供的知识库资料准确回答用户的问题。如果资料中没有相关信息，请如实告知用户。回答要简洁、准确、有条理。")
                .build();

        String answer = assistant.chat(request.question());

        // 保存聊天记录
        ChatHistory history = new ChatHistory();
        history.setUserId(userId);
        history.setKbId(request.kbId());
        history.setSessionId(sessionId);
        history.setQuestion(request.question());
        history.setAnswer(answer);
        chatHistoryRepository.save(history);

        return new ChatResponse(answer, sessionId);
    }

    public TokenStream chatStream(ChatRequest request, Long userId) {
        String sessionId = request.sessionId() != null ? request.sessionId() : UUID.randomUUID().toString();

        ContentRetriever contentRetriever = createContentRetriever(request.kbId());
        ChatMemory memory = sessionMemories.computeIfAbsent(sessionId, k -> new MessageWindowChatMemory(20));

        StreamingKnowledgeAssistant assistant = AiService.builder(StreamingKnowledgeAssistant.class)
                .streamingChatModel(streamingChatModel)
                .chatMemory(memory)
                .contentRetriever(contentRetriever)
                .systemMessage("你是一个专业的企业知识库助手。请基于提供的知识库资料准确回答用户的问题。如果资料中没有相关信息，请如实告知用户。回答要简洁、准确、有条理。")
                .build();

        TokenStream tokenStream = assistant.chatStream(request.question());

        // 保存聊天记录（在流完成后）
        String sessionIdFinal = sessionId;
        tokenStream.onCompleteResponse(completeResponse -> {
            ChatHistory history = new ChatHistory();
            history.setUserId(userId);
            history.setKbId(request.kbId());
            history.setSessionId(sessionIdFinal);
            history.setQuestion(request.question());
            history.setAnswer(completeResponse);
            chatHistoryRepository.save(history);
        });

        return tokenStream;
    }

    private ContentRetriever createContentRetriever(Long kbId) {
        if (kbId == null) {
            return null;
        }

        return query -> {
            Embedding queryEmbedding = embeddingModel.embed(query);
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(5)
                    .minScore(0.3)
                    .build();

            EmbeddingSearchResult result = embeddingStore.search(searchRequest);
            return result.matches().stream()
                    .map(match -> match.segment())
                    .toList();
        };
    }

    public List<ChatHistoryResponse> getHistory(Long userId, String sessionId) {
        if (sessionId != null) {
            return chatHistoryRepository.findByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId)
                    .stream()
                    .map(this::toHistoryResponse)
                    .toList();
        }
        return chatHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    public List<ChatHistoryResponse> getHistoryByKb(Long userId, Long kbId) {
        return chatHistoryRepository.findByUserIdAndKbIdOrderByCreatedAtDesc(userId, kbId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private ChatHistoryResponse toHistoryResponse(ChatHistory h) {
        return new ChatHistoryResponse(
                h.getId(),
                h.getQuestion(),
                h.getAnswer(),
                h.getKbId(),
                h.getSessionId(),
                h.getCreatedAt());
    }

    /** 非流式对话接口 */
    public interface KnowledgeAssistant {
        String chat(String message);
    }

    /** 流式对话接口 */
    public interface StreamingKnowledgeAssistant {
        TokenStream chatStream(String message);
    }
}
