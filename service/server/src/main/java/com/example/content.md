`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\config\AgentConfig.java`n```
package com.example.config;

import com.example.agent.core.rag.embedding.EmbeddingModel;
import com.example.agent.core.rag.embedding.EmbeddingStore;
import com.example.agent.core.model.ChatModel;
import com.example.agent.core.model.StreamingChatModel;
import com.example.agent.infrastructure.rag.store.InMemoryEmbeddingStore;
import com.example.agent.provider.milvus.MilvusConnectionConfig;
import com.example.agent.provider.milvus.MilvusEmbeddingStore;
import com.example.agent.provider.openai.OpenAiChatModel;
import com.example.agent.provider.openai.OpenAiEmbeddingModel;
import com.example.agent.provider.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AgentConfig {

    @Value("${agent.openai.base-url:https://api.siliconflow.cn/v1}")
    private String baseUrl;

    @Value("${agent.openai.api-key}")
    private String apiKey;

    @Value("${agent.openai.chat-model:Qwen/Qwen3-8B}")
    private String chatModelName;

    @Value("${agent.openai.embedding-model:BAAI/bge-large-zh-v1.5}")
    private String embeddingModelName;

    @Value("${agent.openai.embedding-dimension:1024}")
    private int embeddingDimension;

    @Value("${agent.milvus.enabled:false}")
    private boolean milvusEnabled;

    @Value("${agent.milvus.uri:http://localhost:19530}")
    private String milvusUri;

    @Value("${agent.milvus.token:}")
    private String milvusToken;

    @Value("${agent.milvus.collection-name:knowledge_base}")
    private String milvusCollectionName;

    @Bean
    public ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .model(chatModelName)
                .build();
    }

    @Value("${agent.openai.streaming-base-url:https://api.siliconflow.cn/v1/chat/completions}")
    private String streamingBaseUrl;

    @Bean
    public StreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(streamingBaseUrl)
                .apiKey(apiKey)
                .model(chatModelName)
                .requestTimeout(Duration.ofSeconds(120))
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(embeddingModelName)
                .dimension(embeddingDimension)
                .build();
    }

    @Bean
    public EmbeddingStore embeddingStore() {
        if (milvusEnabled) {
            MilvusConnectionConfig config = MilvusConnectionConfig.builder()
                    .uri(milvusUri)
                    .token(milvusToken)
                    .collectionName(milvusCollectionName)
                    .dimension(embeddingDimension)
                    .build();
            return MilvusEmbeddingStore.builder().config(config).build();
        }
        return new InMemoryEmbeddingStore();
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\config\AuthInterceptor.java`n```
package com.example.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();

        // 放行登录注册接口
        if (uri.startsWith("/api/auth/")) {
            return true;
        }

        // 检查 X-User-Id 头
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}");
            return false;
        }

        return true;
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\config\CorsConfig.java`n```
package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\config\DataInitializer.java`n```
package com.example.config;

import com.example.entity.User;
import com.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            String hashedPassword = hashPassword("admin123");
            User admin = new User("admin", hashedPassword, "管理员", "admin");
            userRepository.save(admin);
            log.info("默认管理员账号已创建: admin / admin123");
        }

        if (!userRepository.existsByUsername("demo")) {
            String hashedPassword = hashPassword("demo123");
            User demo = new User("demo", hashedPassword, "演示用户", "user");
            userRepository.save(demo);
            log.info("默认演示账号已创建: demo / demo123");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\config\GlobalExceptionHandler.java`n```
package com.example.config;

import com.example.dto.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        return Result.error(400, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.error(400, e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return Result.error(400, "文件大小超出限制");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        return Result.error(500, "服务器内部错误: " + e.getMessage());
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\config\WebMvcConfig.java`n```
package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**");
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\controller\AuthController.java`n```
package com.example.controller;

import com.example.dto.Result;
import com.example.dto.request.LoginRequest;
import com.example.dto.request.RegisterRequest;
import com.example.dto.response.LoginResponse;
import com.example.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@RequestBody RegisterRequest request) {
        try {
            LoginResponse response = authService.register(request);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\controller\ChatController.java`n```
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

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\controller\DocumentController.java`n```
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

    @PostMapping("/batch-upload")
    public Result<List<DocumentResponse>> batchUpload(
            @RequestParam("kbId") Long kbId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            return Result.success(documentService.batchUpload(kbId, files, userId));
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

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\controller\KnowledgeBaseController.java`n```
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

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\dto\request\ChatRequest.java`n```
package com.example.dto.request;

public record ChatRequest(
        String question,
        Long kbId,
        String sessionId) {
    public ChatRequest {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("问题不能为空");
        }
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\dto\request\CreateKnowledgeBaseRequest.java`n```
package com.example.dto.request;

public record CreateKnowledgeBaseRequest(
        String name,
        String description) {
    public CreateKnowledgeBaseRequest {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("知识库名称不能为空");
        }
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\dto\request\LoginRequest.java`n```
package com.example.dto.request;

public record LoginRequest(
        String username,
        String password) {
    public LoginRequest {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\dto\request\RegisterRequest.java`n```
package com.example.dto.request;

public record RegisterRequest(
        String username,
        String password,
        String nickname) {
    public RegisterRequest {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\dto\response\ChatHistoryResponse.java`n```
package com.example.dto.response;

import java.time.LocalDateTime;

public record ChatHistoryResponse(
        Long id,
        String question,
        String answer,
        Long kbId,
        String sessionId,
        LocalDateTime createdAt) {
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\dto\response\ChatResponse.java`n```
package com.example.dto.response;

public record ChatResponse(
        String answer,
        String sessionId) {
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\dto\response\DocumentResponse.java`n```
package com.example.dto.response;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        Long kbId,
        String fileName,
        String fileType,
        Long fileSize,
        Integer chunkCount,
        String status,
        LocalDateTime createdAt) {
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\dto\response\KnowledgeBaseResponse.java`n```
package com.example.dto.response;

import java.time.LocalDateTime;

public record KnowledgeBaseResponse(
        Long id,
        String name,
        String description,
        Integer docCount,
        LocalDateTime createdAt) {
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\dto\response\LoginResponse.java`n```
package com.example.dto.response;

public record LoginResponse(
        String token,
        Long userId,
        String username,
        String nickname,
        String role) {
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\dto\Result.java`n```
package com.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    private int code;
    private String message;
    private T data;

    private Result() {
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "success";
        result.data = data;
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\entity\ChatHistory.java`n```
package com.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kb_chat_history")
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "kb_id")
    private Long kbId;

    @Column(name = "session_id", length = 50)
    private String sessionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public ChatHistory() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getKbId() {
        return kbId;
    }

    public void setKbId(Long kbId) {
        this.kbId = kbId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\entity\KnowledgeBase.java`n```
package com.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kb_knowledge_base")
public class KnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "doc_count")
    private Integer docCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public KnowledgeBase() {
    }

    public KnowledgeBase(String name, String description, Long createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getDocCount() {
        return docCount;
    }

    public void setDocCount(Integer docCount) {
        this.docCount = docCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\entity\KnowledgeDocument.java`n```
package com.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kb_document")
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kb_id", nullable = false)
    private Long kbId;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(length = 100)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "chunk_count")
    private Integer chunkCount = 0;

    @Column(length = 20)
    private String status = "processing";

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public KnowledgeDocument() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getKbId() {
        return kbId;
    }

    public void setKbId(Long kbId) {
        this.kbId = kbId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Long uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\entity\User.java`n```
package com.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(length = 20)
    private String role = "user";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public User() {
    }

    public User(String username, String password, String nickname, String role) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\entity\UserChatMemoryMessage.java`n```
package com.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_chat_memory_message", indexes = {
        @Index(name = "idx_user_chat_memory_user_order", columnList = "user_id,message_order")
})
public class UserChatMemoryMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "message_order", nullable = false)
    private Integer messageOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getMessageOrder() {
        return messageOrder;
    }

    public void setMessageOrder(Integer messageOrder) {
        this.messageOrder = messageOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\Main.java`n```
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\repository\ChatHistoryRepository.java`n```
package com.example.repository;

import com.example.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    List<ChatHistory> findByUserIdAndSessionIdOrderByCreatedAtAsc(Long userId, String sessionId);

    List<ChatHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ChatHistory> findByUserIdAndKbIdOrderByCreatedAtDesc(Long userId, Long kbId);
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\repository\KnowledgeBaseRepository.java`n```
package com.example.repository;

import com.example.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    List<KnowledgeBase> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    List<KnowledgeBase> findAllByOrderByCreatedAtDesc();
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\repository\KnowledgeDocumentRepository.java`n```
package com.example.repository;

import com.example.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findByKbIdOrderByCreatedAtDesc(Long kbId);

    long countByKbId(Long kbId);
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\repository\UserChatMemoryMessageRepository.java`n```
package com.example.repository;

import com.example.entity.UserChatMemoryMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserChatMemoryMessageRepository extends JpaRepository<UserChatMemoryMessage, Long> {

    List<UserChatMemoryMessage> findByUserIdOrderByMessageOrderAsc(Long userId);

    void deleteByUserId(Long userId);
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\repository\UserRepository.java`n```
package com.example.repository;

import com.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\service\AuthService.java`n```
package com.example.service;

import com.example.dto.request.LoginRequest;
import com.example.dto.request.RegisterRequest;
import com.example.dto.response.LoginResponse;
import com.example.entity.User;
import com.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        String hashedPassword = hashPassword(request.password());
        if (!hashedPassword.equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = generateToken(user);

        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole());
    }

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("用户名已存在");
        }

        String hashedPassword = hashPassword(request.password());
        String nickname = request.nickname() != null ? request.nickname() : request.username();

        User user = new User(
                request.username(),
                hashedPassword,
                nickname,
                "user");

        user = userRepository.save(user);

        String token = generateToken(user);

        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole());
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    private String generateToken(User user) {
        return Base64.getEncoder().encodeToString(
                (user.getId() + ":" + user.getUsername() + ":" + UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\service\ChatService.java`n```
package com.example.service;

import com.example.agent.core.memory.ChatMemory;
import com.example.agent.core.model.ChatModel;
import com.example.agent.core.model.StreamingChatModel;
import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.core.rag.embedding.EmbeddingModel;
import com.example.agent.core.rag.embedding.EmbeddingSearchRequest;
import com.example.agent.core.rag.embedding.EmbeddingSearchResult;
import com.example.agent.core.rag.embedding.EmbeddingStore;
import com.example.agent.core.rag.retriever.ContentRetriever;
import com.example.agent.core.service.AiService;
import com.example.agent.core.service.TokenStream;
import com.example.dto.request.ChatRequest;
import com.example.dto.response.ChatResponse;
import com.example.dto.response.ChatHistoryResponse;
import com.example.entity.ChatHistory;
import com.example.repository.ChatHistoryRepository;
import com.example.repository.KnowledgeBaseRepository;
import com.example.repository.UserChatMemoryMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore embeddingStore;
    private final ChatHistoryRepository chatHistoryRepository;
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final UserChatMemoryMessageRepository userChatMemoryMessageRepository;

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    public ChatService(ChatModel chatModel,
            StreamingChatModel streamingChatModel,
            EmbeddingModel embeddingModel,
            EmbeddingStore embeddingStore,
            ChatHistoryRepository chatHistoryRepository,
            KnowledgeBaseRepository knowledgeBaseRepository,
            UserChatMemoryMessageRepository userChatMemoryMessageRepository) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.chatHistoryRepository = chatHistoryRepository;
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.userChatMemoryMessageRepository = userChatMemoryMessageRepository;
    }

    public ChatResponse chat(ChatRequest request, Long userId) {
        String sessionId = request.sessionId() != null ? request.sessionId() : UUID.randomUUID().toString();

        // 创建带RAG的ContentRetriever
        ContentRetriever contentRetriever = createContentRetriever(request.kbId());

        ChatMemory memory = new MysqlChatMemory(userChatMemoryMessageRepository, userId, 100);

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
        ChatMemory memory = new MysqlChatMemory(userChatMemoryMessageRepository, userId, 100);

        StreamingKnowledgeAssistant assistant = AiService.builder(StreamingKnowledgeAssistant.class)
                .streamingChatModel(streamingChatModel)
                .chatMemory(memory)
                .contentRetriever(contentRetriever)
                .systemMessage("你是一个专业的企业知识库助手。请基于提供的知识库资料准确回答用户的问题。如果资料中没有相关信息，请如实告知用户。回答要简洁、准确、有条理。")
                .build();

        TokenStream originalStream = assistant.chatStream(request.question());

        // 创建包装的TokenStream，在完成时发送sessionId
        String sessionIdFinal = sessionId;
        SessionIdTokenStream tokenStream = new SessionIdTokenStream(originalStream, sessionIdFinal, () -> {
            // 这里会在流完成时被调用，但我们需要在Controller中保存完整的响应
            // 所以暂时留空，实际保存逻辑在Controller中处理
        });

        return tokenStream;
    }

    private ContentRetriever createContentRetriever(Long kbId) {
        return query -> {
            log.info("向量检索开始, query='{}', kbId={}", query, kbId);

            Embedding queryEmbedding = embeddingModel.embed(query);
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(20)
                    .minScore(0.3)
                    .build();

            EmbeddingSearchResult result = embeddingStore.search(searchRequest);
            int totalMatches = result.matches().size();

            var segments = result.matches().stream()
                    .map(match -> match.segment())
                    .limit(5)
                    .toList();

            log.info("向量检索完成, totalMatches={}, filteredMatches={}, kbId={}, query='{}'", totalMatches, segments.size(),
                    kbId, query);
            if (!segments.isEmpty() && log.isDebugEnabled()) {
                for (int i = 0; i < segments.size(); i++) {
                    var segment = segments.get(i);
                    log.debug("result[{}] score=?, id={}, kb_id={}, textSnippet={}",
                            i,
                            segment.id(),
                            segment.metadata().get("kb_id"),
                            segment.text().substring(0, Math.min(80, segment.text().length())).replaceAll("\n", " "));
                }
            }

            return segments;
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

    public void clearUserMemory(Long userId) {
        log.info("Clearing chat memory for userId={}", userId);
        userChatMemoryMessageRepository.deleteByUserId(userId);
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

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\service\DocumentService.java`n```
package com.example.service;

import com.example.dto.response.DocumentResponse;
import com.example.entity.KnowledgeDocument;
import com.example.repository.KnowledgeDocumentRepository;
import com.example.agent.core.rag.document.Document;
import com.example.agent.core.rag.document.DocumentSplitter;
import com.example.agent.core.rag.document.DocumentByCharacterSplitter;
import com.example.agent.core.rag.document.TextSegment;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        String fileName = file.getOriginalFilename();
        String fileType = fileName != null && fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf(".") + 1)
                : "txt";
        String storedFileName = UUID.randomUUID() + "." + fileType;
        Path filePath;

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            filePath = uploadPath.resolve(storedFileName);
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath);
            }
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

            Map<String, String> metadata = new HashMap<>();
            metadata.put("kb_id", String.valueOf(doc.getKbId()));
            metadata.put("file_name", doc.getFileName());
            Document document = Document.from(content, metadata);

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

    @Transactional
    public List<DocumentResponse> batchUpload(Long kbId, List<MultipartFile> files, Long userId) {
        List<DocumentResponse> results = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                results.add(upload(kbId, file, userId));
            } catch (Exception e) {
                // 单个文件失败不影响其他文件
                results.add(null);
            }
        }
        // 过滤掉失败的记录
        results.removeIf(r -> r == null);
        return results;
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

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\service\KnowledgeBaseService.java`n```
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

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\service\MysqlChatMemory.java`n```
package com.example.service;

import com.example.agent.core.memory.ChatMemory;
import com.example.agent.core.message.AssistantMessage;
import com.example.agent.core.message.ChatMessage;
import com.example.agent.core.message.UserMessage;
import com.example.entity.UserChatMemoryMessage;
import com.example.repository.UserChatMemoryMessageRepository;

import java.util.List;

public class MysqlChatMemory implements ChatMemory {

    private final UserChatMemoryMessageRepository repository;
    private final Long userId;
    private final int maxMessages;

    public MysqlChatMemory(UserChatMemoryMessageRepository repository, Long userId, int maxMessages) {
        this.repository = repository;
        this.userId = userId;
        this.maxMessages = maxMessages;
    }

    @Override
    public void add(ChatMessage message) {
        if (message == null) {
            return;
        }

        List<UserChatMemoryMessage> existing = repository.findByUserIdOrderByMessageOrderAsc(userId);
        int nextOrder = existing.isEmpty() ? 1 : existing.get(existing.size() - 1).getMessageOrder() + 1;

        UserChatMemoryMessage entity = new UserChatMemoryMessage();
        entity.setUserId(userId);
        entity.setRole(message.role());
        entity.setContent(message.content());
        entity.setMessageOrder(nextOrder);
        repository.save(entity);

        trimIfNeeded();
    }

    @Override
    public List<ChatMessage> messages() {
        return repository.findByUserIdOrderByMessageOrderAsc(userId)
                .stream()
                .map(this::toChatMessage)
                .toList();
    }

    private ChatMessage toChatMessage(UserChatMemoryMessage message) {
        if ("user".equals(message.getRole())) {
            return new UserMessage(message.getContent());
        }
        return new AssistantMessage(message.getContent());
    }

    private void trimIfNeeded() {
        List<UserChatMemoryMessage> existing = repository.findByUserIdOrderByMessageOrderAsc(userId);
        if (existing.size() <= maxMessages) {
            return;
        }

        int removeCount = existing.size() - maxMessages;
        for (int i = 0; i < removeCount; i++) {
            repository.delete(existing.get(i));
        }

        List<UserChatMemoryMessage> remaining = repository.findByUserIdOrderByMessageOrderAsc(userId);
        for (int i = 0; i < remaining.size(); i++) {
            UserChatMemoryMessage entity = remaining.get(i);
            entity.setMessageOrder(i + 1);
            repository.save(entity);
        }
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\service\server\src\main\java\com\example\service\SessionIdTokenStream.java`n```
package com.example.service;

import java.util.List;
import java.util.function.Consumer;

import com.example.agent.core.service.TokenStream;
import com.example.agent.core.tool.ToolCall;

/**
 * TokenStream包装类，在完成时发送sessionId而不是完整响应
 */
public class SessionIdTokenStream implements TokenStream {

    private final TokenStream originalStream;
    private final String sessionId;
    private final Runnable onComplete;

    public SessionIdTokenStream(TokenStream originalStream, String sessionId, Runnable onComplete) {
        this.originalStream = originalStream;
        this.sessionId = sessionId;
        this.onComplete = onComplete;
    }

    @Override
    public TokenStream onPartialResponse(Consumer<String> handler) {
        originalStream.onPartialResponse(handler);
        return this;
    }

    @Override
    public TokenStream onPartialReasoning(Consumer<String> handler) {
        originalStream.onPartialReasoning(handler);
        return this;
    }

    @Override
    public TokenStream onToolCalls(Consumer<List<ToolCall>> handler) {
        originalStream.onToolCalls(handler);
        return this;
    }

    @Override
    public TokenStream onCompleteResponse(Consumer<String> handler) {
        originalStream.onCompleteResponse(completeResponse -> {
            // 执行保存聊天记录的操作
            if (onComplete != null) {
                onComplete.run();
            }
            // 发送sessionId给前端
            handler.accept(sessionId);
        });
        return this;
    }

    @Override
    public TokenStream onError(Consumer<Throwable> handler) {
        originalStream.onError(handler);
        return this;
    }

    @Override
    public void start() {
        originalStream.start();
    }
}
```
