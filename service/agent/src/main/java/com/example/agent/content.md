`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\agent\AgentLogger.java`n```
package com.example.agent.core.agent;

/**
 * Agent 运行日志
 */
public class AgentLogger {
    private final boolean enabled;

    public AgentLogger(boolean enabled) {
        this.enabled = enabled;
    }

    public void info(String message) {
        if (!enabled) {
            return;
        }
        System.out.println("[Agent] " + message);
    }

    public void step(int step, String message) {
        if (!enabled) {
            return;
        }
        System.out.println("[Agent Step " + step + "] " + message);
    }

    public void tool(String toolName, String message) {
        if (!enabled) {
            return;
        }
        System.out.println("[Tool:" + toolName + "] " + message);
    }

    public void error(String message, Throwable e) {
        if (!enabled) {
            return;
        }
        System.out.println("[Agent Error] " + message);
        if (e != null) {
            System.out.println("[Agent Error] "
                    + e.getClass().getSimpleName()
                    + ":"
                    + e.getMessage());
        }
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\agent\AgentOptions.java`n```
package com.example.agent.core.agent;

/**
 * 保存 Agent 运行时配置。
 */
public class AgentOptions {

    private final int maxAgentSteps;
    private final double temperature;
    private final boolean logEnabled;
    private final boolean failFastOnToolError;
    private final Integer maxTokens;
    private final Boolean enableThinking;

    private AgentOptions(Builder builder) {
        this.maxAgentSteps = builder.maxAgentSteps;
        this.temperature = builder.temperature;
        this.logEnabled = builder.logEnabled;
        this.failFastOnToolError = builder.failFastOnToolError;
        this.maxTokens = builder.maxTokens;
        this.enableThinking = builder.enableThinking;
    }

    public int maxAgentSteps() {
        return maxAgentSteps;
    }

    public double temperature() {
        return temperature;
    }

    public boolean logEnabled() {
        return logEnabled;
    }

    public boolean failFastOnToolError() {
        return failFastOnToolError;
    }

    public Integer maxTokens() {
        return maxTokens;
    }

    public Boolean enableThinking() {
        return enableThinking;
    }

    public static AgentOptions defaultOptions() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return builder()
                .temperature(temperature)
                .logEnabled(logEnabled)
                .enableThinking(enableThinking)
                .maxTokens(maxTokens)
                .failFastOnToolError(failFastOnToolError)
                .maxAgentSteps(maxAgentSteps);
    }

    public static class Builder {
        private int maxAgentSteps = 5;
        private double temperature = 0.7;
        private boolean logEnabled = true;
        private boolean failFastOnToolError = true;
        private Integer maxTokens = 1024;
        private Boolean enableThinking = true;

        public Builder maxAgentSteps(int maxAgentSteps) {
            if (maxAgentSteps <= 0) {
                throw new IllegalArgumentException("maxAgentSteps 必须大于 0");
            }
            this.maxAgentSteps = maxAgentSteps;
            return this;
        }

        public Builder temperature(double temperature) {
            if (temperature < 0 || temperature > 2) {
                throw new IllegalArgumentException("temperature 必须在 0 到 2 之间");
            }
            this.temperature = temperature;
            return this;
        }

        public Builder logEnabled(boolean logEnabled) {
            this.logEnabled = logEnabled;
            return this;
        }

        public Builder failFastOnToolError(boolean failFastOnToolError) {
            this.failFastOnToolError = failFastOnToolError;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            if (maxTokens != null && maxTokens <= 0) {
                throw new IllegalArgumentException("maxTokens 必须大于0");
            }
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder enableThinking(Boolean enableThinking) {
            this.enableThinking = enableThinking;
            return this;
        }

        public AgentOptions build() {
            return new AgentOptions(this);
        }
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\annotation\Tool.java`n```
package com.example.agent.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Tool {
    String value() default "";
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\annotation\ToolParam.java`n```
package com.example.agent.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 元数据
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ToolParam {
    String value();
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\memory\ChatMemory.java`n```
package com.example.agent.core.memory;

import java.util.List;

import com.example.agent.core.message.ChatMessage;

public interface ChatMemory {
    void add(ChatMessage message);

    List<ChatMessage> messages();
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\memory\MessageWindowChatMemory.java`n```
package com.example.agent.core.memory;

import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.message.ChatMessage;

public class MessageWindowChatMemory implements ChatMemory {
    private final int maxMessages;

    private final List<ChatMessage> messages = new ArrayList<>();

    public MessageWindowChatMemory() {
        this(10);
    }

    public MessageWindowChatMemory(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    @Override
    public void add(ChatMessage message) {
        messages.add(message);
        ensureMaxMessages();
    }

    @Override
    public List<ChatMessage> messages() {
        ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>(this.messages);
        return messages;
    }

    private void ensureMaxMessages() {
        while (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\message\AssistantMessage.java`n```
package com.example.agent.core.message;

public class AssistantMessage extends ChatMessage {

    public AssistantMessage(String content) {
        super("assistant", content);
    }

    @Override
    public String role() {
        return "assistant";
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\message\AssistantToolCallMessage.java`n```
package com.example.agent.core.message;

import java.util.List;

import com.example.agent.core.tool.ToolCall;

public class AssistantToolCallMessage extends ChatMessage {
    private final List<ToolCall> toolCalls;

    public AssistantToolCallMessage(String content, List<ToolCall> toolCalls) {
        super("assistant", content);
        this.toolCalls = toolCalls;
    }

    @Override
    public String role() {
        return "assistant";
    }

    public List<ToolCall> toolCalls() {
        return toolCalls;
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\message\ChatMessage.java`n```
package com.example.agent.core.message;

public abstract class ChatMessage {
    private final String content;
    private final String role;

    protected ChatMessage(String role, String content) {
        this.content = content;
        this.role = role;
    }

    public String content() {
        return content;
    }

    public String role() {
        return role;
    };
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\message\SystemMessage.java`n```
package com.example.agent.core.message;

public class SystemMessage extends ChatMessage {

    public SystemMessage(String content) {
        super("system",content);
    }

    @Override
    public String role() {
        return "system";
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\message\ToolMessage.java`n```
package com.example.agent.core.message;

public class ToolMessage extends ChatMessage {
    private final String name;
    private final String toolCallId;

    public ToolMessage(String toolCallId, String name, String content) {
        super("tool", content);
        this.name = name;
        this.toolCallId = toolCallId;
    }

    @Override
    public String role() {
        return "tool";
    }

    public String name() {
        return name;
    }

    public String toolCallId() {
        return toolCallId;
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\message\UserMessage.java`n```
package com.example.agent.core.message;

public class UserMessage extends ChatMessage {

    public UserMessage(String content) {
        super("user", content);
    }

    @Override
    public String role() {
        return "user";
    }

}
```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\model\ChatModel.java`n```
package com.example.agent.core.model;

import com.example.agent.core.request.ChatRequest;

public interface ChatModel {

    ChatModelResponse chat(ChatRequest request);

    default String modelName() {
        return "";
    };
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\model\ChatModelResponse.java`n```
package com.example.agent.core.model;

import java.util.List;

import com.example.agent.core.tool.ToolCall;

public record ChatModelResponse(
        String content,
        List<ToolCall> toolCalls,
        String finishReason) {
    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    public String contentOrEmpty() {
        return content == null ? "" : content;
    }

    // 普通回复
    public static ChatModelResponse content(String content) {
        return new ChatModelResponse(content, List.of(), "stop");
    }

    // 工具调用回复
    public static ChatModelResponse toolCall(String content, List<ToolCall> toolCalls) {
        return new ChatModelResponse(content, toolCalls, "tool_calls");
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\model\StreamingChatModel.java`n```
package com.example.agent.core.model;

import com.example.agent.core.request.ChatRequest;

public interface StreamingChatModel {
    void chat(ChatRequest request, StreamingResponseHandler handler);

    default String modelName() {
        return "";
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\model\StreamingResponseHandler.java`n```
package com.example.agent.core.model;

import java.util.List;

import com.example.agent.core.tool.ToolCall;

/**
 * 流式响应的生命周期
 */
public interface StreamingResponseHandler {
    /**
     * 当返回部分流的时候
     * 
     * @param partialResponse 部分回应
     */
    void onPartialResponse(String partialResponse);

    /**
     * 当模型思考的时候
     * 
     * @param partialReasoning 部分思考
     */
    default void onPartialReasoning(String partialReasoning) {

    };

    /**
     * 当工具调用的时候
     * 
     * @param toolCalls 工具列表
     */
    default void onToolCalls(List<ToolCall> toolCalls) {

    };

    /**
     * 响应完成的时候
     * 
     * @param completeResponse 最终回复
     */
    void onCompleteResponse(String completeResponse);

    /**
     * 当发生错误的时候
     * 
     * @param error 错误抛出的异常
     */
    void onError(Throwable error);
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\parser\ChatResponseParser.java`n```
package com.example.agent.core.parser;

import com.example.agent.core.model.ChatModelResponse;

public interface ChatResponseParser {
    ChatModelResponse parse(String responseJson);
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\prompt\PromptBuilder.java`n```
package com.example.agent.core.prompt;

import java.util.List;

import com.example.agent.core.message.ChatMessage;

import com.example.agent.core.request.ChatRequest;
import com.example.agent.core.tool.ToolMetadata;

/**
 * 这个类只负责构造ChatRequest请求并不负责存储消息
 */
public class PromptBuilder {

    public ChatRequest buildRequest(
            String model,
            String systemMessage,
            List<ChatMessage> messages,
            List<ToolMetadata> toolMetadata,
            Double temperature,
            Integer maxTokens,
            Boolean enableThinking) {
        return ChatRequest.builder()
                .model(model)
                .systemMessage(systemMessage)
                .messages(messages)
                .tools(toolMetadata)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .enableThinking(enableThinking)
                .build();
    }
}
```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\document\Document.java`n```
package com.example.agent.core.rag.document;

import java.util.Map;
import java.util.UUID;

public record Document(
        String id,
        String text,
        Map<String, String> metadata) {

    public static final String FILE_NAME = "file_name";
    public static final String ABSOLUTE_DIRECTORY_PATH = "absolute_directory_path";

    public Document {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("document text 不能为空");
        }

        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public static Document from(String text) {
        return new Document(UUID.randomUUID().toString(), text, Map.of());
    }

    public static Document from(String text, Map<String, String> metadata) {
        return new Document(UUID.randomUUID().toString(), text, metadata);
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\document\DocumentByCharacterSplitter.java`n```
package com.example.agent.core.rag.document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DocumentByCharacterSplitter implements DocumentSplitter {
    private final int maxSegmentSize;
    private final int overlapSize;

    public DocumentByCharacterSplitter(int maxSegmentSize) {
        this(maxSegmentSize, 0);
    }

    public DocumentByCharacterSplitter(int maxSegmentSize, int overlapSize) {

        if (maxSegmentSize <= 0) {
            throw new IllegalArgumentException(
                    "maxSegmentSize 必须大于0");
        }

        if (overlapSize < 0) {
            throw new IllegalArgumentException(
                    "overlapSize 不能小于0");
        }

        if (overlapSize >= maxSegmentSize) {
            throw new IllegalArgumentException(
                    "overlapSize 必须小于maxSegmentSize");
        }

        this.maxSegmentSize = maxSegmentSize;
        this.overlapSize = overlapSize;
    }

    @Override
    public List<TextSegment> split(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("document 不能为空");
        }

        List<TextSegment> segments = new ArrayList<TextSegment>();
        String text = document.text();

        int start = 0;
        int segmentIndex = 0;

        while (start < text.length()) {
            int end = Math.min(
                    start + maxSegmentSize,
                    text.length());

            String segmentText = text.substring(start, end);

            if (!segmentText.isBlank()) {
                Map<String, String> metadata = new LinkedHashMap<>(document.metadata());

                metadata.put("document_id", document.id());
                metadata.put(
                        "segment_index",
                        String.valueOf(segmentIndex));

                TextSegment textSegment = new TextSegment(
                        null,
                        segmentText,
                        Map.copyOf(metadata));

                segments.add(textSegment);

                segmentIndex++;
            }

            if (end == text.length()) {
                break;
            }
            start = end - overlapSize;
        }
        return List.copyOf(segments);
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\document\DocumentParseException.java`n```
package com.example.agent.core.rag.document;

/**
 * 
 * DocumentParseException
 */
public class DocumentParseException extends RuntimeException {

    /**
     * 
     * @param message exception message
     */
    public DocumentParseException(String message) {
        super(message);
    }

    /**
     * 
     * @param message exception message
     * @param cause
     */
    public DocumentParseException(String message, Throwable cause) {
        super(message, cause);
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\document\DocumentParser.java`n```
package com.example.agent.core.rag.document;

import java.io.InputStream;

/**
 * 
 * DocumentParser
 * 
 * parse the input stream into a document
 * 
 * DocumentParser is not responsible for closing the inputStream
 * the lifecycle of the input stream is managed by the caller
 */
public interface DocumentParser {
    /**
     * 
     * parse document
     * 
     * @param inputStream document input stream
     * @return parsed document
     */
    Document parse(InputStream inputStream);
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\document\DocumentSplitter.java`n```
package com.example.agent.core.rag.document;

import java.util.List;

/**
 * 
 * DocumentSplitter
 */
public interface DocumentSplitter {

    /**
     * split document
     * 
     * @param document
     * @return TextSegmentArray
     */
    List<TextSegment> split(Document document);

    /**
     * slit all documents
     * 
     * @param documents documentsArray
     * @return TextSegmentArray
     */
    default List<TextSegment> splitAll(List<Document> documents) {

        if (documents == null || documents.isEmpty()) {
            return List.of();
        }

        return documents.stream()
                .flatMap(document -> split(document).stream())
                .toList();
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\document\loader\DocumentLoadingException.java`n```
package com.example.agent.core.rag.document.loader;

/**
 * 
 * DocumentLoadingException
 */
public class DocumentLoadingException extends RuntimeException {

    /**
     * 
     * @param message exception message
     */
    public DocumentLoadingException(String message) {
        super(message);
    }

    /**
     * 
     * @param message exception message
     * @param cause
     */
    public DocumentLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\document\loader\FileSystemDocumentLoader.java`n```
package com.example.agent.core.rag.document.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import com.example.agent.core.rag.document.Document;
import com.example.agent.core.rag.document.DocumentParser;
import com.example.agent.core.rag.document.parser.TextDocumentParser;

public final class FileSystemDocumentLoader {
    private static final PathMatcher DEFAULT_TEXT_MATCHER = path -> {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return false;
        }
        String name = fileName.toString()
                .toLowerCase(Locale.ROOT);
        return name.endsWith(".txt") || name.endsWith(".md") || name.endsWith(".markdown");
    };

    private FileSystemDocumentLoader() {
    }

    public static Document loadDocument(
            Path filePath,
            DocumentParser documentParser) {

        if (filePath == null) {
            throw new IllegalArgumentException("filePath 不能为空");
        }

        if (documentParser == null) {
            throw new IllegalArgumentException("documentParser 不能为空");
        }

        Path normalizedPath = filePath.toAbsolutePath().normalize();

        if (!Files.isRegularFile(normalizedPath)) {
            throw new IllegalArgumentException("指定路径不是文件：" + normalizedPath);
        }

        try (
                InputStream inputStream = Files.newInputStream(normalizedPath);) {
            Document parsedDocument = documentParser.parse(inputStream);

            Map<String, String> metadata = new LinkedHashMap<String, String>(parsedDocument.metadata());

            metadata.put(
                    Document.FILE_NAME,
                    normalizedPath.getFileName().toString());

            Path parent = normalizedPath.getParent();

            if (parent != null) {
                metadata.put(
                        Document.ABSOLUTE_DIRECTORY_PATH,
                        parent.toString());
            }

            return new Document(parsedDocument.id(), parsedDocument.text(), Map.copyOf(metadata));

        } catch (IOException e) {
            throw new DocumentLoadingException(
                    "加载文档失败：" + normalizedPath, e);
        }
    }

    public static Document loadDocument(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException(
                    "filePath 不能为空");
        }

        if (!DEFAULT_TEXT_MATCHER.matches(filePath)) {
            throw new IllegalArgumentException(
                    "默认解析器只支持 txt、md、markdown: "
                            + filePath);
        }

        return loadDocument(
                filePath,
                new TextDocumentParser());
    }

    public static Document loadDocument(String filePath, DocumentParser documentParser) {
        return loadDocument(toPath(filePath, "filePath"), documentParser);
    }

    public static Document loadDocument(String filePath) {
        return loadDocument(toPath(filePath, "filePath"));
    }

    public static List<Document> loadDocuments(
            Path directoryPath,
            DocumentParser documentParser) {
        return loadDocuments(directoryPath, path -> true, documentParser);
    }

    public static List<Document> loadDocuments(Path directoryPath) {
        return loadDocuments(
                directoryPath,
                DEFAULT_TEXT_MATCHER,
                new TextDocumentParser());
    }

    public static List<Document> loadDocuments(String directoryPath, DocumentParser documentParser) {
        return loadDocuments(toPath(directoryPath, "directoryPath"), documentParser);
    }

    public static List<Document> loadDocuments(String directory) {
        return loadDocuments(toPath(directory, "directoryPath"));
    }

    public static List<Document> loadDocuments(Path directory, PathMatcher pathMatcher) {
        return loadDocuments(
                directory,
                pathMatcher,
                new TextDocumentParser());
    }

    public static List<Document> loadDocuments(String directoryPath, PathMatcher pathMatcher,
            DocumentParser documentParser) {

        return loadDocuments(
                toPath(directoryPath, "directoryPath"),
                pathMatcher,
                documentParser);
    }

    public static List<Document> loadDocuments(
            Path directoryPath,
            PathMatcher pathMatcher,
            DocumentParser documentParser) {

        return loadDocumentsInternal(
                directoryPath,
                pathMatcher,
                documentParser,
                false);
    }

    public static List<Document> loadDocuments(String directoryPath, PathMatcher pathMatcher) {

        return loadDocuments(toPath(directoryPath, "directoryPath"), pathMatcher);
    }

    public static List<Document> loadDocumentsRecursively(
            Path directoryPath,
            DocumentParser documentParser) {

        return loadDocumentsRecursively(
                directoryPath,
                path -> true,
                documentParser);
    }

    public static List<Document> loadDocumentsRecursively(
            Path directoryPath) {

        return loadDocumentsRecursively(
                directoryPath,
                DEFAULT_TEXT_MATCHER,
                new TextDocumentParser());
    }

    public static List<Document> loadDocumentsRecursively(
            Path directoryPath, PathMatcher pathMatcher) {

        return loadDocumentsRecursively(
                directoryPath,
                pathMatcher,
                new TextDocumentParser());
    }

    public static List<Document> loadDocumentsRecursively(
            Path directoryPath,
            PathMatcher pathMatcher,
            DocumentParser documentParser) {

        return loadDocumentsInternal(
                directoryPath,
                pathMatcher,
                documentParser,
                true);
    }

    public static List<Document> loadDocumentsRecursively(String directoryPath) {
        return loadDocumentsRecursively(toPath(directoryPath, "directoryPath"));
    }

    public static List<Document> loadDocumentsRecursively(String directoryPath, DocumentParser documentParser) {
        return loadDocumentsRecursively(toPath(directoryPath, "directoryPath"), documentParser);
    }

    public static List<Document> loadDocumentsRecursively(String directoryPath, PathMatcher pathMatcher) {
        return loadDocumentsRecursively(toPath(directoryPath, "directoryPath"), pathMatcher);
    }

    public static List<Document> loadDocumentsRecursively(String directoryPath, PathMatcher pathMatcher,
            DocumentParser documentParser) {

        return loadDocumentsRecursively(
                toPath(directoryPath, "directoryPath"),
                pathMatcher,
                documentParser);
    }

    private static List<Document> loadDocumentsInternal(Path directoryPath, PathMatcher pathMatcher,
            DocumentParser documentParser, boolean recursive) {

        if (directoryPath == null) {
            throw new IllegalArgumentException("directory 不能为空");
        }

        if (pathMatcher == null) {
            throw new IllegalArgumentException("pathMatcher 不能为空");
        }

        if (documentParser == null) {
            throw new IllegalArgumentException("documentParser 不能为空 ");
        }

        Path normalizePath = directoryPath.toAbsolutePath().normalize();

        if (!Files.isDirectory(normalizePath)) {
            throw new IllegalArgumentException("指定路径不是目录:" + normalizePath);
        }

        try (Stream<Path> paths = recursive
                ? Files.walk(normalizePath)
                : Files.list(normalizePath)) {

            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> pathMatcher
                            .matches(
                                    normalizePath
                                            .relativize(path)))
                    .sorted(Comparator.comparing(
                            path -> normalizePath
                                    .relativize(path)
                                    .toString()))

                    .map(path -> loadDocument(
                            path,
                            documentParser))
                    .toList();

        } catch (IOException e) {
            throw new DocumentLoadingException("加载文档失败：" + normalizePath, e);
        }
    }

    private static Path toPath(String path,
            String parameterName) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException(parameterName + " 不能为空");
        }

        return Path.of(path);
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\document\parser\TextDocumentParser.java`n```
package com.example.agent.core.rag.document.parser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.example.agent.core.rag.document.Document;
import com.example.agent.core.rag.document.DocumentParseException;
import com.example.agent.core.rag.document.DocumentParser;

public final class TextDocumentParser implements DocumentParser {
    private final Charset charset;

    public TextDocumentParser() {
        this(StandardCharsets.UTF_8);
    }

    public TextDocumentParser(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charset 不能为空");
        }
        this.charset = charset;
    }

    @Override
    public Document parse(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream 不能为空");
        }

        try {
            String text = new String(
                    inputStream.readAllBytes(),
                    charset);

            if (text.isBlank()) {
                throw new DocumentParseException("文档内容不能为空");
            }

            return Document.from(text);
        } catch (IOException e) {
            throw new DocumentParseException("读取文档内容失败", e);
        }

    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\document\TextSegment.java`n```
package com.example.agent.core.rag.document;

import java.util.Map;
import java.util.UUID;

/**
 * 
 * TextSegment
 * 
 * @param id       textSegment id
 * @param text     textSegment
 * @param metadata text metadata
 */
public record TextSegment(

        String id,
        String text,
        Map<String, String> metadata) {

    public TextSegment {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("textSegment text 不能为空");
        }

        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public static TextSegment from(String text) {
        return new TextSegment(
                UUID.randomUUID().toString(),
                text,
                Map.of());
    }

    public static TextSegment from(String text, Map<String, String> metadata) {
        return new TextSegment(
                UUID.randomUUID().toString(),
                text,
                metadata);
    }

    public static TextSegment from(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("document 不能为空");
        }

        return new TextSegment(
                UUID.randomUUID().toString(),
                document.text(),
                document.metadata());
    }
}
```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\embedding\Embedding.java`n```
package com.example.agent.core.rag.embedding;

import java.util.Arrays;

public final class Embedding {
    private final float[] vector;

    public Embedding(float[] vector) {

        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("embedding vector");
        }

        this.vector = Arrays.copyOf(vector, vector.length);

    }

    public float[] vector() {
        return Arrays.copyOf(vector, vector.length);
    }

    public int dimension() {
        return vector.length;
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\embedding\EmbeddingMatch.java`n```
package com.example.agent.core.rag.embedding;

import com.example.agent.core.rag.document.TextSegment;

/**
 * 
 * EmbeddingMatch
 * 一条向量检索匹配结果
 * 
 * @param score       相关度分数
 * @param embeddingId 向量存储中的记录id
 * @param embedding   命中的向量 允许为空
 * @param segment     命中的文本片段
 */
public record EmbeddingMatch(
        double score,
        String embeddingId,
        Embedding embedding,
        TextSegment segment

) {

    public EmbeddingMatch {

        if (!Double.isFinite(score)) {
            throw new IllegalArgumentException(
                    "embedding 不能为空");
        }

        if (embeddingId == null || embeddingId.isBlank()) {
            throw new IllegalArgumentException(
                    "embedding 不能为空");
        }

        if (segment == null) {
            throw new IllegalArgumentException(
                    "segment 不能为空");
        }

    }

    /**
     * @deprecated 我们已经决定在1.4版本中弃用此构造器
     *             请用新的构造器 {@link #EmbeddingMatch(double, String, Embedding,
     *             TextSegment)}
     * @param score
     * @param segment
     */
    @Deprecated(since = "1.4", forRemoval = true)
    public EmbeddingMatch(double score, TextSegment segment) {
        this(
                score,
                segment == null ? null : segment.id(),
                null,
                segment);
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\embedding\EmbeddingModel.java`n```
package com.example.agent.core.rag.embedding;

import java.util.List;

import com.example.agent.core.rag.document.TextSegment;

public interface EmbeddingModel {
    Embedding embed(String text);

    default List<Embedding> embeddingAll(List<TextSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return List.of();
        }

        return segments.stream()
                .map(segment -> embed(segment.text()))
                .toList();
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\embedding\EmbeddingSearchRequest.java`n```
package com.example.agent.core.rag.embedding;

public final class EmbeddingSearchRequest {
    private final Embedding queryEmbedding;
    private final int maxResults;
    private final double minScore;

    private EmbeddingSearchRequest(Builder builder) {
        this.queryEmbedding = builder.queryEmbedding;
        this.maxResults = builder.maxResults;
        this.minScore = builder.minScore;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Embedding queryEmbedding;
        private int maxResults = 3;
        private double minScore;

        public Builder queryEmbedding(Embedding queryEmbedding) {
            if (queryEmbedding == null) {
                throw new IllegalArgumentException("queryEmbedding 不能为空");
            }
            this.queryEmbedding = queryEmbedding;
            return this;
        }

        public Builder maxResults(int maxResults) {
            if (maxResults <= 0) {
                throw new IllegalArgumentException("maxResult 必须大于0");
            }

            this.maxResults = maxResults;
            return this;

        }

        public Builder minScore(double minScore) {

            if (!Double.isFinite(minScore) || minScore < -1 || minScore > 1) {
                throw new IllegalArgumentException(
                        "minScore 必须在 -1 到 1 之间");
            }
            this.minScore = minScore;
            return this;

        }

        public EmbeddingSearchRequest build() {
            if (queryEmbedding == null) {
                throw new IllegalStateException("必须装配 queryEmbedding");
            }

            return new EmbeddingSearchRequest(this);
        }

    }

    public Embedding queryEmbedding() {
        return queryEmbedding;
    }

    public int maxResults() {
        return maxResults;
    }

    public double minScore() {
        return minScore;
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\embedding\EmbeddingSearchResult.java`n```
package com.example.agent.core.rag.embedding;

import java.util.List;

/**
 * 一次向量检索的结果
 * 
 * EmbeddingSearchResult
 * 
 * @param matches 文本分段
 */
public record EmbeddingSearchResult(
        List<EmbeddingMatch> matches) {

    public EmbeddingSearchResult {

        if (matches == null) {
            throw new IllegalArgumentException("matches 不能为空");
        }

        matches = List.copyOf(matches);
    }

    public static EmbeddingSearchResult empty() {
        return new EmbeddingSearchResult(List.of());
    }

    public boolean isEmpty() {
        return matches.isEmpty();
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\embedding\EmbeddingStore.java`n```
package com.example.agent.core.rag.embedding;

import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.rag.document.TextSegment;

public interface EmbeddingStore {

    /**
     * add embedding
     * 
     * @param embedding textSegment vector
     * @param segment   textSegment
     */
    String add(Embedding embedding, TextSegment segment);

    /**
     * addAll embedding
     * 
     * @param embeddings embeddings vectors
     * @param segments   textSegments
     */
    default List<String> addAll(
            List<Embedding> embeddings,
            List<TextSegment> segments) {

        if (embeddings == null) {
            throw new IllegalArgumentException("embedding 不能为空");
        }

        if (segments == null) {
            throw new IllegalArgumentException("segment 不能为空");
        }

        if (embeddings.size() != segments.size()) {
            throw new IllegalArgumentException("embeddings 和 segments数量必须一致");
        }

        List<String> ids = new ArrayList<String>();

        for (int i = 0; i < embeddings.size(); i++) {
            ids.add(add(embeddings.get(i), segments.get(i)));
        }

        return ids;
    }

    EmbeddingSearchResult search(EmbeddingSearchRequest request);
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\embedding\EmbeddingStoreException.java`n```
package com.example.agent.core.rag.embedding;

public class EmbeddingStoreException extends RuntimeException {

    public EmbeddingStoreException(String message) {
        super(message);
    }

    public EmbeddingStoreException(String message, Throwable cause) {
        super(message, cause);
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\ingestion\EmbeddingStoreIngestor.java`n```
package com.example.agent.core.rag.ingestion;

import java.util.List;

import com.example.agent.core.rag.document.Document;
import com.example.agent.core.rag.document.DocumentSplitter;
import com.example.agent.core.rag.document.TextSegment;
import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.core.rag.embedding.EmbeddingModel;
import com.example.agent.core.rag.embedding.EmbeddingStore;

/**
 * 
 * EmbeddingStoreIngestor
 */
public final class EmbeddingStoreIngestor {

    private final DocumentSplitter documentSplitter;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore embeddingStore;

    private EmbeddingStoreIngestor(Builder builder) {
        this.documentSplitter = builder.documentSplitter;
        this.embeddingModel = builder.embeddingModel;
        this.embeddingStore = builder.embeddingStore;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void ingest(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("document 不能为空");
        }

        ingestAll(List.of(document));
    }

    public void ingestAll(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        List<TextSegment> segments = documentSplitter.splitAll(documents);

        if (segments.isEmpty()) {
            return;
        }

        List<Embedding> embeddings = embeddingModel.embeddingAll(segments);
        embeddingStore.addAll(embeddings, segments);
    }

    public static final class Builder {
        private DocumentSplitter documentSplitter;
        private EmbeddingModel embeddingModel;
        private EmbeddingStore embeddingStore;

        public Builder documentSplitter(DocumentSplitter documentSplitter) {
            if (documentSplitter == null) {
                throw new IllegalArgumentException("documentSplitter 不能为空");
            }

            this.documentSplitter = documentSplitter;
            return this;
        }

        public Builder embeddingModel(EmbeddingModel embeddingModel) {
            if (embeddingModel == null) {
                throw new IllegalArgumentException("embeddingModel 不能为空");
            }

            this.embeddingModel = embeddingModel;
            return this;
        }

        public Builder embeddingStore(EmbeddingStore embeddingStore) {
            if (embeddingStore == null) {
                throw new IllegalArgumentException("embeddingStore 不能为空");
            }

            this.embeddingStore = embeddingStore;
            return this;
        }

        public EmbeddingStoreIngestor build() {

            if (documentSplitter == null) {
                throw new IllegalStateException("必须装配 documentSplitter");
            }

            if (embeddingModel == null) {
                throw new IllegalStateException("必须装配 embeddingModel");
            }

            if (embeddingStore == null) {
                throw new IllegalStateException("必须装配 embeddingStore");
            }

            return new EmbeddingStoreIngestor(this);
        }
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\retriever\ContentRetriever.java`n```
package com.example.agent.core.rag.retriever;

import java.util.List;

import com.example.agent.core.rag.document.TextSegment;

/**
 * 
 * ContentRetriever
 */
public interface ContentRetriever {
    List<TextSegment> retrieve(String query);
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\retriever\EmbeddingStoreContentRetriever.java`n```
package com.example.agent.core.rag.retriever;

import java.util.List;

import com.example.agent.core.rag.document.TextSegment;
import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.core.rag.embedding.EmbeddingModel;
import com.example.agent.core.rag.embedding.EmbeddingSearchRequest;
import com.example.agent.core.rag.embedding.EmbeddingSearchResult;
import com.example.agent.core.rag.embedding.EmbeddingStore;

public final class EmbeddingStoreContentRetriever implements ContentRetriever {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore embeddingStore;
    private final int maxResults;
    private final double minScore;

    private EmbeddingStoreContentRetriever(Builder builder) {
        this.embeddingModel = builder.embeddingModel;
        this.embeddingStore = builder.embeddingStore;
        this.maxResults = builder.maxResults;
        this.minScore = builder.minScore;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<TextSegment> retrieve(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        Embedding queryEmbedding = embeddingModel.embed(query);

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest
                .builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();

        EmbeddingSearchResult searchResult = embeddingStore.search(searchRequest);

        return searchResult.matches()
                .stream()
                .map(match -> match.segment())
                .toList();
    }

    public static class Builder {
        private EmbeddingModel embeddingModel;
        private EmbeddingStore embeddingStore;
        private int maxResults = 3;
        private double minScore = 0.7;

        public Builder() {
        }

        public Builder embeddingModel(EmbeddingModel embeddingModel) {
            if (embeddingModel == null) {
                throw new IllegalArgumentException("embeddingModel 不能为空");
            }
            this.embeddingModel = embeddingModel;
            return this;
        }

        public Builder embeddingStore(EmbeddingStore embeddingStore) {
            if (embeddingStore == null) {
                throw new IllegalArgumentException("embeddingStore 不能为空");
            }
            this.embeddingStore = embeddingStore;
            return this;
        }

        public Builder maxResults(int maxResults) {
            if (maxResults <= 0) {
                throw new IllegalArgumentException("maxResults 必须大于 0");
            }
            this.maxResults = maxResults;
            return this;
        }

        public Builder minScore(double minScore) {
            if (!Double.isFinite(minScore)
                    || minScore < -1
                    || minScore > 1) {

                throw new IllegalArgumentException(
                        "minScore 必须在 -1 到 1 之间");
            }

            this.minScore = minScore;
            return this;
        }

        public EmbeddingStoreContentRetriever build() {
            if (embeddingModel == null) {
                throw new IllegalStateException(
                        "必须配置 embeddingModel");
            }

            if (embeddingStore == null) {
                throw new IllegalStateException(
                        "必须配置 embeddingStore");
            }
            return new EmbeddingStoreContentRetriever(this);
        }
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\rag\retriever\RagPromptAugmentor.java`n```
package com.example.agent.core.rag.retriever;

import java.util.List;

import com.example.agent.core.rag.document.TextSegment;

public class RagPromptAugmentor {
    private final ContentRetriever contentRetriever;

    public RagPromptAugmentor(ContentRetriever contentRetriever) {
        this.contentRetriever = contentRetriever;
    }

    /**
     * augment content
     * 
     * @param userMessage userMessage
     * @return augment content
     */
    public String augment(String userMessage) {
        if (contentRetriever == null) {
            return userMessage;
        }

        List<TextSegment> segments = contentRetriever.retrieve(userMessage);

        if (segments == null || segments.isEmpty()) {
            return userMessage;
        }

        StringBuffer builder = new StringBuffer();

        builder.append("请基于以下资料回答用户问题。\n\n");
        builder.append("资料:\n");

        for (int i = 0; i < segments.size(); i++) {
            builder.append("[")
                    .append(i + 1)
                    .append("]")
                    .append(segments.get(i).text())
                    .append("\n");
        }

        builder.append("\n用户问题:\n");
        builder.append(userMessage);

        return builder.toString();
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\request\ChatRequest.java`n```
package com.example.agent.core.request;

import java.util.List;

import com.example.agent.core.message.ChatMessage;
import com.example.agent.core.tool.ToolMetadata;

public final class ChatRequest {
    private final String model;
    private final String systemMessage;
    private final List<ChatMessage> messages;
    private final List<ToolMetadata> tools;
    private final Double temperature;
    private final Boolean enableThinking;
    private final Integer maxTokens;

    public static Builder builder() {
        return new Builder();
    }

    private ChatRequest(Builder builder) {
        this.model = builder.model;
        this.systemMessage = builder.systemMessage;
        this.messages = List.copyOf(builder.messages);
        this.tools = List.copyOf(builder.tools);
        this.temperature = builder.temperature;
        this.maxTokens = builder.maxTokens;
        this.enableThinking = builder.enableThinking;
    }

    public static final class Builder {
        private String model;
        private String systemMessage;
        private List<ChatMessage> messages = List.of();
        private List<ToolMetadata> tools = List.of();
        private Double temperature = 0.7;
        private Boolean enableThinking = true;
        private Integer maxTokens;

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder systemMessage(String systemMessage) {
            this.systemMessage = systemMessage;
            return this;
        }

        public Builder messages(List<ChatMessage> message) {
            if (message == null) {
                throw new IllegalArgumentException("message 不能为空");
            }
            this.messages = List.copyOf(message);
            return this;
        }

        public Builder tools(List<ToolMetadata> tools) {
            this.tools = tools == null
                    ? List.of()
                    : List.copyOf(tools);
            return this;
        }

        public Builder temperature(Double temperature) {
            if (temperature != null && (temperature < 0 || temperature > 2)) {
                throw new IllegalArgumentException(
                        "temperature 必须在 0 到 2 之间");
            }
            this.temperature = temperature == null
                    ? 0.7
                    : temperature;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            if (maxTokens != null && maxTokens <= 0) {
                throw new IllegalArgumentException(
                        "maxTokens 必须大于 0");
            }
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder enableThinking(Boolean enableThinking) {
            this.enableThinking = enableThinking;
            return this;
        }

        public ChatRequest build() {

            if (messages.isEmpty()) {
                throw new IllegalStateException(
                        "必须配置 messages");
            }
            return new ChatRequest(this);
        }
    }

    public ChatRequest withDefaultModel(String defaultModel) {

        if (this.model != null && !this.model.isBlank()) {

            return this;
        }
        if (defaultModel == null || defaultModel.isBlank()) {
            throw new IllegalArgumentException(
                    "defaultModel 不能为空");
        }

        return toBuilder()
                .model(defaultModel)
                .build();
    }

    public Builder toBuilder() {
        return builder()
                .model(model)
                .systemMessage(systemMessage)
                .messages(messages)
                .tools(tools)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .enableThinking(enableThinking);
    }

    public String model() {
        return model;
    }

    public String systemMessage() {
        return systemMessage;
    }

    public List<ChatMessage> messages() {
        return messages;
    }

    public List<ToolMetadata> tools() {
        return tools;
    }

    public Double temperature() {
        return temperature;
    }

    public Integer maxTokens() {
        return maxTokens;
    }

    public Boolean enableThinking() {
        return enableThinking;
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\request\ChatRequestBuilder.java`n```
package com.example.agent.core.request;

public interface ChatRequestBuilder<T> {
    T  build(ChatRequest request);
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\request\ChunkType.java`n```
package com.example.agent.core.request;

public enum ChunkType {
    CONTENT,
    REASONING,
    TOOL_CALL_DELTA,
    DONE,
    EMPTY
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\service\AgentRunner.java`n```
package com.example.agent.core.service;

import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.agent.AgentLogger;
import com.example.agent.core.agent.AgentOptions;
import com.example.agent.core.memory.ChatMemory;
import com.example.agent.core.message.AssistantMessage;
import com.example.agent.core.message.AssistantToolCallMessage;
import com.example.agent.core.message.ChatMessage;

import com.example.agent.core.message.UserMessage;
import com.example.agent.core.model.ChatModel;
import com.example.agent.core.model.ChatModelResponse;
import com.example.agent.core.prompt.PromptBuilder;
import com.example.agent.core.rag.retriever.ContentRetriever;
import com.example.agent.core.rag.retriever.RagPromptAugmentor;
import com.example.agent.core.request.ChatRequest;

import com.example.agent.core.tool.ToolMetadata;

public class AgentRunner {
    private final ChatModel chatModel;
    private final ChatMemory chatMemory;
    private final String systemMessage;
    private final List<ToolMetadata> toolMetadataList = new ArrayList<ToolMetadata>();
    private final AgentOptions agentOptions;
    private final AgentLogger agentLogger;
    private final AgentToolExecutor agentToolExecutor;
    private final PromptBuilder promptBuilder = new PromptBuilder();
    private final RagPromptAugmentor ragPromptAugmentor;

    public AgentRunner(
            ChatModel chatModel,
            ChatMemory chatMemory,
            String systemMessage,
            List<ToolMetadata> toolMetadataList,
            AgentOptions agentOptions,
            ContentRetriever contentRetriever) {

        if (chatModel == null) {
            throw new IllegalArgumentException("ChatModel 不能为空");
        }

        this.chatModel = chatModel;
        this.chatMemory = chatMemory;
        this.systemMessage = systemMessage;
        this.agentOptions = agentOptions == null
                ? AgentOptions.defaultOptions()
                : agentOptions;

        this.ragPromptAugmentor = new RagPromptAugmentor(contentRetriever);
        this.agentLogger = new AgentLogger(this.agentOptions.logEnabled());

        if (toolMetadataList != null) {
            this.toolMetadataList.addAll(toolMetadataList);
        }

        this.agentToolExecutor = new AgentToolExecutor(
                this.toolMetadataList,
                this.agentOptions,
                this.agentLogger);
    }

    public String chat(String userMessage) {
        List<ChatMessage> messages = new ArrayList<ChatMessage>();

        if (chatMemory != null) {
            messages.addAll(chatMemory.messages());
        }

        int newMessageStartIndex = messages.size();

        String augmentUserMessage = ragPromptAugmentor.augment(userMessage);

        messages.add(new UserMessage(augmentUserMessage));

        String finalResponse = runAgentLoop(messages);

        saveNewMessage(messages, newMessageStartIndex, new UserMessage(userMessage));

        return finalResponse;

    }

    private String runAgentLoop(List<ChatMessage> messages) {
        agentLogger.info("""
                Agent Loop 启动,maxAgentSteps= %s,temperature= %s,enableThinking= %s,tools= %s
                """.formatted(agentOptions.maxAgentSteps(), agentOptions.temperature(), agentOptions.enableThinking(),
                toolMetadataList.size()));

        for (int step = 1; step <= agentOptions.maxAgentSteps(); step++) {

            if (messages.size() > 50) {
                throw new RuntimeException("Agent 消息数量异常增长，已终止。message.size=" + messages.size());
            }

            agentLogger.step(step, "构建ChatRequest,messages.size=" + messages.size());

            ChatRequest request = promptBuilder.buildRequest(
                    chatModel.modelName(),
                    systemMessage,
                    messages,
                    toolMetadataList,
                    agentOptions.temperature(),
                    agentOptions.maxTokens(),
                    agentOptions.enableThinking());

            agentLogger.step(step, "开始请求模型");

            ChatModelResponse response = chatModel.chat(request);

            agentLogger.step(step, "模型响应完成，finishResponse=" + response.finishReason());

            if (!response.hasToolCalls()) {
                String aiResult = response.contentOrEmpty();

                agentLogger.step(step, "模型返回最终结果Agent Loop");

                messages.add(new AssistantMessage(aiResult));
                return aiResult;

            }

            agentLogger.step(step, "模型请求调用工具，数量=" + response.toolCalls().size());

            AssistantToolCallMessage assistantToolCallMessage = new AssistantToolCallMessage(
                    response.contentOrEmpty(),
                    response.toolCalls());

            messages.add(assistantToolCallMessage);

            messages.addAll(agentToolExecutor.execute(response.toolCalls(), step));

        }

        throw new RuntimeException(
                "Agent Loop 超过最大执行步数:"
                        + agentOptions.maxAgentSteps()
                        + "可能出现了循环调用工具");
    }

    private void saveNewMessage(
            List<ChatMessage> messages,
            int startIndex,
            UserMessage originalUserMessage) {

        if (chatMemory == null) {
            return;
        }

        chatMemory.add(originalUserMessage);

        for (int i = startIndex + 1; i < messages.size(); i++) {
            chatMemory.add(messages.get(i));
        }
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\service\AgentToolExecutor.java`n```
package com.example.agent.core.service;

import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.agent.AgentLogger;
import com.example.agent.core.agent.AgentOptions;
import com.example.agent.core.message.ToolMessage;
import com.example.agent.core.tool.ToolCall;
import com.example.agent.core.tool.ToolExecutor;
import com.example.agent.core.tool.ToolMetadata;

public class AgentToolExecutor {
    private final AgentOptions agentOptions;
    private final AgentLogger agentLogger;
    private final ToolExecutor toolExecutor;

    public AgentToolExecutor(
            List<ToolMetadata> toolMetadataList,
            AgentOptions agentOptions,
            AgentLogger agentLogger) {

        List<ToolMetadata> safeToolMetadataList = toolMetadataList == null
                ? List.of()
                : new ArrayList<>(toolMetadataList);

        this.agentOptions = agentOptions == null
                ? AgentOptions.defaultOptions()
                : agentOptions;

        this.agentLogger = agentLogger == null
                ? new AgentLogger(this.agentOptions.logEnabled())
                : agentLogger;

        this.toolExecutor = new ToolExecutor(safeToolMetadataList);
    }

    public List<ToolMessage> execute(
            List<ToolCall> toolCalls,
            int step) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            throw new RuntimeException("模型返回了工具调用，但toolCalls为空");
        }

        agentLogger.step(step, "开始执行工具调用");

        List<ToolMessage> toolMessages = new ArrayList<ToolMessage>();

        for (ToolCall toolCall : toolCalls) {
            String toolResult = executeSingleToolCall(toolCall);

            ToolMessage toolMessage = new ToolMessage(
                    toolCall.id(),
                    toolCall.name(),
                    toolResult);

            toolMessages.add(toolMessage);

        }
        agentLogger.step(step, "工具调用执行完成");

        return toolMessages;
    }

    private String executeSingleToolCall(ToolCall toolCall) {
        if (toolCall == null) {
            throw new RuntimeException("toolCall不能为空");
        }

        try {
            agentLogger.tool(toolCall.name(), "开始执行，arguments=" + toolCall.arguments());

            String result = toolExecutor.execute(toolCall);

            agentLogger.tool(toolCall.name(), "执行成功,result=" + result);

            return result;
        } catch (Exception e) {
            agentLogger.error("工具执行失败:" + toolCall.name(), e);

            if (agentOptions.failFastOnToolError()) {
                throw new RuntimeException(
                        "工具执行失败:"
                                + toolCall.name()
                                + ",arguments="
                                + toolCall.arguments(),
                        e);
            }

            return "工具执行失败:" + e.getMessage();
        }
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\service\AiService.java`n```
package com.example.agent.core.service;

public final class AiService {

    private AiService() {
    }

    public static <T> AiServiceBuilder<T> builder(Class<T> serviceClass) {
        return new AiServiceBuilder<>(serviceClass);
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\service\AiServiceBuilder.java`n```
package com.example.agent.core.service;

import java.lang.reflect.Proxy;

import com.example.agent.core.agent.AgentOptions;
import com.example.agent.core.memory.ChatMemory;
import com.example.agent.core.model.ChatModel;
import com.example.agent.core.model.StreamingChatModel;
import com.example.agent.core.rag.retriever.ContentRetriever;

public class AiServiceBuilder<T> {
    private final Class<T> serviceClass;

    private ChatModel chatModel;
    private StreamingChatModel streamingChatModel;
    private ChatMemory chatMemory;
    private ContentRetriever contentRetriever;
    private String systemMessage;
    private Object[] tools = new Object[0];

    private AgentOptions agentOptions = AgentOptions.defaultOptions();

    public AiServiceBuilder(Class<T> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public AiServiceBuilder<T> systemMessage(String systemMessage) {
        this.systemMessage = systemMessage;
        return this;
    }

    public AiServiceBuilder<T> chatModel(ChatModel chatModel) {
        this.chatModel = chatModel;
        return this;
    }

    public AiServiceBuilder<T> streamingChatModel(StreamingChatModel streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
        return this;
    }

    public AiServiceBuilder<T> chatMemory(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
        return this;
    }

    public AiServiceBuilder<T> tools(Object... tools) {
        this.tools = tools == null ? new Object[0] : tools;
        return this;
    }

    public AiServiceBuilder<T> agentOptions(AgentOptions agentOptions) {
        if (agentOptions == null) {
            throw new IllegalArgumentException("agentOptions 不能为空");
        }
        this.agentOptions = agentOptions;
        return this;
    }

    public AiServiceBuilder<T> maxAgentSteps(int maxAgentSteps) {
        this.agentOptions = this.agentOptions
                .toBuilder()
                .maxAgentSteps(maxAgentSteps)
                .build();
        return this;
    }

    public AiServiceBuilder<T> temperature(double temperature) {
        this.agentOptions = this.agentOptions
                .toBuilder()
                .temperature(temperature)
                .build();
        return this;
    }

    public AiServiceBuilder<T> logEnabled(boolean logEnabled) {
        this.agentOptions = this.agentOptions
                .toBuilder()
                .logEnabled(logEnabled)
                .build();
        return this;
    }

    public AiServiceBuilder<T> failFastOnToolError(boolean failFastOnToolError) {
        this.agentOptions = this.agentOptions
                .toBuilder()
                .failFastOnToolError(failFastOnToolError)
                .build();
        return this;
    }

    public AiServiceBuilder<T> maxTokens(Integer maxTokens) {
        this.agentOptions = this.agentOptions
                .toBuilder()
                .maxTokens(maxTokens)
                .build();
        return this;
    }

    public AiServiceBuilder<T> enableThinking(Boolean enableThinking) {
        this.agentOptions = this.agentOptions
                .toBuilder()
                .enableThinking(enableThinking)
                .build();
        return this;
    }

    public AiServiceBuilder<T> contentRetriever(ContentRetriever contentRetriever) {
        if (contentRetriever == null) {
            throw new IllegalArgumentException("contentRetriever 不能为空");
        }
        this.contentRetriever = contentRetriever;
        // TODO 待完善
        // this.agentOptions = this.agentOptions
        // .toBuilder()
        // .contentRetriever(contentRetriever)
        // .build();
        return this;
    }

    @SuppressWarnings("unchecked")
    public T build() {
        validate();
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[] { serviceClass },
                new AiServiceInvocationHandler(
                        chatModel,
                        streamingChatModel,
                        chatMemory,
                        systemMessage,
                        tools,
                        agentOptions,
                        contentRetriever));
    }

    private void validate() {

        if (serviceClass == null) {
            throw new IllegalArgumentException("serviceClass 不能为空");
        }

        if (!serviceClass.isInterface()) {
            throw new IllegalArgumentException("AiService 必须是接口");
        }

        if (chatModel == null && streamingChatModel == null) {
            throw new IllegalArgumentException("chatModel 或 streamingChatModel 不能为空");
        }
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\service\AiServiceInvocationHandler.java`n```
package com.example.agent.core.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.agent.AgentOptions;
import com.example.agent.core.memory.ChatMemory;

import com.example.agent.core.model.ChatModel;

import com.example.agent.core.model.StreamingChatModel;
import com.example.agent.core.rag.retriever.ContentRetriever;
import com.example.agent.core.tool.ToolMetadata;
import com.example.agent.core.tool.ToolScanner;

public class AiServiceInvocationHandler implements InvocationHandler {
    /**
     * 防止模型无限循环调用工具
     */

    private final AgentOptions agentOptions;

    private final ChatModel chatModel;
    private final ChatMemory chatMemory;
    private final StreamingChatModel streamingChatModel;
    private final String systemMessage;
    private final List<ToolMetadata> toolMetadataList = new ArrayList<>();
    private final ContentRetriever contentRetriever;

    public AiServiceInvocationHandler(
            ChatModel chatModel,
            StreamingChatModel streamingChatModel,
            ChatMemory chatMemory,
            String systemMessage,
            Object[] tools,
            AgentOptions agentOptions,
            ContentRetriever contentRetriever) {

        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
        this.chatMemory = chatMemory;
        this.systemMessage = systemMessage;
        this.contentRetriever = contentRetriever;

        this.agentOptions = agentOptions == null ? AgentOptions.defaultOptions() : agentOptions;

        if (tools != null) {
            for (Object tool : tools) {
                if (tool != null) {
                    toolMetadataList.addAll(ToolScanner.scan(tool));
                }
            }
        }

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

        if (isTokenStreamMethod(method)) {
            return createTokenStream(args);
        }

        return invokeChat(args, method);
    }

    private Object invokeChat(Object[] args, Method method) {
        if (chatModel == null) {
            throw new IllegalStateException(
                    "chatModel 不能为空，无法执行非流响应:" + method.getName());
        }
        String userMessage = buildUserMessage(args);

        AgentRunner agentRunner = new AgentRunner(
                chatModel,
                chatMemory,
                systemMessage,
                toolMetadataList,
                agentOptions,
                contentRetriever);

        return agentRunner.chat(userMessage);
    }

    private boolean isTokenStreamMethod(Method method) {
        return TokenStream.class.isAssignableFrom(method.getReturnType());
    }

    private Object createTokenStream(Object[] args) {

        if (streamingChatModel == null) {
            throw new IllegalStateException("streamingChatModel 不能为空，无法执行 TokenStream");
        }

        String userMessage = buildUserMessage(args);

        StreamingAgentRunner runner = new StreamingAgentRunner(
                streamingChatModel,
                chatMemory,
                systemMessage,
                toolMetadataList,
                agentOptions,
                contentRetriever);
        return new AiServiceTokenStream(runner, userMessage);
    }

    /**
     * 构造用户消息
     * 
     * @param args 用户本次消息
     * @return 用户本次消息
     */
    private String buildUserMessage(Object[] args) {

        if (args == null || args.length == 0) {
            return "";
        }

        if (args.length == 1) {
            return String.valueOf(args[0]);
        }

        StringBuilder builder = new StringBuilder();

        for (Object arg : args) {
            builder.append(arg).append("\n");
        }
        return builder.toString().trim();
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\service\AiServiceTokenStream.java`n```
package com.example.agent.core.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.example.agent.core.model.StreamingResponseHandler;
import com.example.agent.core.tool.ToolCall;

/**
 * TokenStream 的默认实现
 * 
 * 它本身不直接请求模型，而是持有StreamingAgentRunner。
 * 用户注册完回调后，调用start() 才真正正式启动流 Agent Loop
 */
public class AiServiceTokenStream implements TokenStream {

    private final StreamingAgentRunner runner;
    private final String userMessage;

    private Consumer<String> partialResponseHandler = ignored -> {
    };
    private Consumer<String> partialReasoningHandler = ignored -> {
    };
    private Consumer<List<ToolCall>> toolCallsHandler = ignored -> {
    };
    private Consumer<String> completeResponseHandler = ignored -> {
    };
    private Consumer<Throwable> errorHandler = error -> {
        throw new RuntimeException("TokenStream 执行失败", error);
    };

    private boolean started = false;

    public AiServiceTokenStream(StreamingAgentRunner runner, String userMessage) {
        this.runner = Objects.requireNonNull(runner, "runner 不能为空");
        this.userMessage = userMessage == null ? "" : userMessage;
    }

    @Override
    public TokenStream onPartialResponse(Consumer<String> handler) {
        partialResponseHandler = Objects.requireNonNull(handler, "partialResponse handler 不能为空");
        return this;
    }

    @Override
    public TokenStream onPartialReasoning(Consumer<String> handler) {
        partialReasoningHandler = Objects.requireNonNull(handler, "partialReasoning handler 不能为空");
        return this;
    }

    @Override
    public TokenStream onToolCalls(Consumer<List<ToolCall>> handler) {
        toolCallsHandler = Objects.requireNonNull(handler, "toolCalls handler 不能为空");
        return this;
    }

    @Override
    public TokenStream onCompleteResponse(Consumer<String> handler) {
        completeResponseHandler = Objects.requireNonNull(handler, "completeResponse 不能为空");
        return this;
    }

    @Override
    public TokenStream onError(Consumer<Throwable> handler) {
        errorHandler = Objects.requireNonNull(handler, "error handler 不能为空");
        return this;
    }

    @Override
    public void start() {
        if (started) {
            throw new IllegalStateException("TokenStream 已经启动，不能重复启动");
        }

        started = true;

        runner.chat(userMessage, new StreamingResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                partialResponseHandler.accept(partialResponse);
            }

            @Override
            public void onPartialReasoning(String partialReasoning) {
                partialReasoningHandler.accept(partialReasoning);
            }

            @Override
            public void onToolCalls(List<ToolCall> toolCalls) {
                toolCallsHandler.accept(toolCalls);
            }

            @Override
            public void onCompleteResponse(String completeResponse) {
                completeResponseHandler.accept(completeResponse);
            }

            @Override
            public void onError(Throwable error) {
                errorHandler.accept(error);
            }

        });
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\service\StreamingAgentRunner.java`n```
package com.example.agent.core.service;

import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.agent.AgentLogger;
import com.example.agent.core.agent.AgentOptions;
import com.example.agent.core.memory.ChatMemory;
import com.example.agent.core.message.AssistantMessage;
import com.example.agent.core.message.AssistantToolCallMessage;
import com.example.agent.core.message.ChatMessage;

import com.example.agent.core.message.UserMessage;
import com.example.agent.core.model.StreamingChatModel;
import com.example.agent.core.model.StreamingResponseHandler;
import com.example.agent.core.prompt.PromptBuilder;
import com.example.agent.core.rag.retriever.ContentRetriever;
import com.example.agent.core.rag.retriever.RagPromptAugmentor;
import com.example.agent.core.request.ChatRequest;
import com.example.agent.core.tool.ToolCall;

import com.example.agent.core.tool.ToolMetadata;

public class StreamingAgentRunner {
    private final StreamingChatModel streamingChatModel;
    private final ChatMemory chatMemory;
    private final String systemMessage;
    private final AgentOptions agentOptions;
    private final AgentLogger agentLogger;
    private final List<ToolMetadata> toolMetadataList = new ArrayList<>();
    private final AgentToolExecutor agentToolExecutor;
    private final PromptBuilder promptBuilder = new PromptBuilder();
    private final RagPromptAugmentor ragPromptAugmentor;

    public StreamingAgentRunner(
            StreamingChatModel streamingChatModel,
            ChatMemory chatMemory,
            String systemMessage,
            List<ToolMetadata> toolsMetadataList,
            AgentOptions agentOptions,
            ContentRetriever contentRetriever) {

        if (streamingChatModel == null) {
            throw new IllegalArgumentException("streamingChatModel 不能为空");
        }

        this.streamingChatModel = streamingChatModel;
        this.chatMemory = chatMemory;
        this.systemMessage = systemMessage;
        this.agentOptions = agentOptions == null
                ? AgentOptions.defaultOptions()
                : agentOptions;
        this.ragPromptAugmentor = new RagPromptAugmentor(contentRetriever);
        this.agentLogger = new AgentLogger(this.agentOptions.logEnabled());

        if (toolsMetadataList != null) {
            this.toolMetadataList.addAll(toolsMetadataList);
        }

        this.agentToolExecutor = new AgentToolExecutor(
                this.toolMetadataList,
                this.agentOptions,
                this.agentLogger);
    }

    public void chat(String userMessage, StreamingResponseHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler 不能为空");
        }

        List<ChatMessage> messages = new ArrayList<ChatMessage>();

        if (chatMemory != null) {
            messages.addAll(chatMemory.messages());
        }

        int newMessageStartIndex = messages.size();
        String augmentUserMessage = ragPromptAugmentor.augment(userMessage);

        messages.add(new UserMessage(augmentUserMessage));

        try {
            runStreamingAgentLoop(messages, handler);
            saveNewMessages(messages, newMessageStartIndex, new UserMessage(userMessage));
        } catch (Exception e) {
            handler.onError(e);
        }

    }

    private void runStreamingAgentLoop(
            List<ChatMessage> messages,
            StreamingResponseHandler outerHandler) {

        agentLogger.info(
                "Streaming Agent Loop 启动"
                        + ",maxAgentSteps=" + agentOptions.maxAgentSteps()
                        + ",temperature=" + agentOptions.temperature()
                        + ",enableThinking=" + agentOptions.enableThinking()
                        + ",tools=" + toolMetadataList.size());

        for (int step = 1; step <= agentOptions.maxAgentSteps(); step++) {
            agentLogger.step(step, "构建流式 ChatRequest,messages.size=" + messages.size());

            ChatRequest request = promptBuilder.buildRequest(
                    streamingChatModel.modelName(),
                    systemMessage,
                    messages,
                    toolMetadataList,
                    agentOptions.temperature(),
                    agentOptions.maxTokens(),
                    agentOptions.enableThinking());

            CapturingStreamingResponseHandler stepHandler = new CapturingStreamingResponseHandler(
                    outerHandler);

            agentLogger.step(step, "开始流式请求模型");

            streamingChatModel.chat(request, stepHandler);

            if (stepHandler.error != null) {
                throw new RuntimeException("Streaming 模型调用失败", stepHandler.error);
            }

            agentLogger.step(step, "流式模型响应完成"
                    + ",content.length=" + stepHandler.completeResponse.length()
                    + ",toolCalls=" + stepHandler.toolCalls.size());

            if (stepHandler.toolCalls.isEmpty()) {
                String finalAnswer = stepHandler.completeResponse.toString();

                messages.add(new AssistantMessage(finalAnswer));

                agentLogger.step(step, "模型最终返回答案，Streaming Agent Loop");

                outerHandler.onCompleteResponse(finalAnswer);
                return;
            }

            outerHandler.onToolCalls(stepHandler.toolCalls);

            AssistantToolCallMessage assistantToolCallMessage = new AssistantToolCallMessage(
                    stepHandler.completeResponse.toString(),
                    stepHandler.toolCalls);

            messages.add(assistantToolCallMessage);

            messages.addAll(agentToolExecutor.execute(stepHandler.toolCalls, step));
        }
        throw new RuntimeException(
                "Streaming Agent Loop 超过最大执行步数:"
                        + agentOptions.maxAgentSteps()
                        + ",可能出现循环工具调用");
    }

    private void saveNewMessages(
            List<ChatMessage> messages,
            int startIndex,
            UserMessage originalUserMessage) {
        if (chatMemory == null) {
            return;
        }
        chatMemory.add(originalUserMessage);
        for (int i = startIndex + 1; i < messages.size(); i++) {
            chatMemory.add(messages.get(i));
        }
    }

    private static class CapturingStreamingResponseHandler implements StreamingResponseHandler {
        private final StreamingResponseHandler outerHandler;

        private final StringBuilder completeResponse = new StringBuilder();
        private final List<ToolCall> toolCalls = new ArrayList<ToolCall>();
        private Throwable error;

        private CapturingStreamingResponseHandler(StreamingResponseHandler outerHandler) {
            this.outerHandler = outerHandler;
        }

        @Override
        public void onPartialResponse(String partialResponse) {
            completeResponse.append(partialResponse);
            outerHandler.onPartialResponse(partialResponse);
        }

        @Override
        public void onPartialReasoning(String partialReasoning) {
            outerHandler.onPartialReasoning(partialReasoning);
        }

        @Override
        public void onToolCalls(List<ToolCall> toolCalls) {
            if (toolCalls != null && !toolCalls.isEmpty()) {
                this.toolCalls.addAll(toolCalls);
            }
        }

        @Override
        public void onCompleteResponse(String completeResponse) {

        }

        @Override
        public void onError(Throwable error) {
            this.error = error;
        }

    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\service\TokenStream.java`n```
package com.example.agent.core.service;

import java.util.List;
import java.util.function.Consumer;

import com.example.agent.core.tool.ToolCall;

/**
 * AI Service 层的流返回对象
 * 
 * 设计定位：
 * - 不是底层 Provider
 * - 不是 StreamingChatModel
 * - 是用户拿到的可订阅流
 */
public interface TokenStream {
    /**
     * 部分响应的时候
     * 
     * @param handler
     * @return
     */
    TokenStream onPartialResponse(Consumer<String> handler);

    /**
     * 思考/推理的时候
     * 
     * @param handler
     * @return
     */
    TokenStream onPartialReasoning(Consumer<String> handler);

    /**
     * 工具调用的时候
     * 
     * @param handler
     * @return
     */
    TokenStream onToolCalls(Consumer<List<ToolCall>> handler);

    /**
     * 完全响应的时候
     * 
     * @param handler
     * @return
     */
    TokenStream onCompleteResponse(Consumer<String> handler);

    /**
     * 在错误的时候
     * 
     * @param handler
     * @return
     */
    TokenStream onError(Consumer<Throwable> handler);

    void start();
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\tool\ToolCall.java`n```
package com.example.agent.core.tool;

public record ToolCall(String id,String name, String arguments) {
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\tool\ToolExecutor.java`n```
package com.example.agent.core.tool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ToolExecutor {

    private final List<ToolMetadata> toolMetadataList;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ToolExecutor(List<ToolMetadata> toolMetadataList) {
        this.toolMetadataList = toolMetadataList;
    }

    // 工具执行器
    public String execute(ToolCall toolCall) {

        // 找到工具并且获取工具元数据
        ToolMetadata toolMetadata = findTool(toolCall.name());

        try {
            // 工具方法体
            Method method = toolMetadata.method();

            List<ToolParameterMetadata> parameters = toolMetadata.parameters();

            // 如果方法参数不是1抛出异常
            if (method.getParameterCount() != parameters.size()) {
                throw new RuntimeException("工具方法参数数量与元数据数量不一致:" + toolCall.name());
            }

            // 所有参数
            Map<String, Object> argumentMap = parseArguments(toolCall.arguments());

            Object[] args = new Object[parameters.size()];

            // 循环参数总长度拿到值
            for (int i = 0; i < parameters.size(); i++) {
                ToolParameterMetadata parameter = parameters.get(i);
                args[i] = convertArgument(argumentMap, parameter);
            }

            Object result = method.invoke(
                    toolMetadata.target(), // 方法元数据目标
                    args);// 方法参数

            // 返回工具执行结果
            return String.valueOf(result);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            throw new RuntimeException("工具内部执行失败:" + toolCall.name() + ",原因:" + targetException.getLocalizedMessage());
        } catch (Exception e) {
            throw new RuntimeException("工具执行失败:" + toolCall.name(), e);
        }
    }

    // 解析参数
    private Map<String, Object> parseArguments(String json) {
        try {
            return objectMapper.readValue(json,
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (Exception e) {
            throw new RuntimeException("工具参数JSON解析失败:" + json, e);
        }
    }

    // 转换参数
    private Object convertArgument(Map<String, Object> argumentMap, ToolParameterMetadata parameter) {

        Object value = argumentMap.get(parameter.name());

        if (value == null) {
            throw new RuntimeException("工具参数缺失:" + parameter.name());
        }

        // 判断返回值的类型
        Class<?> targetType = parameter.type();

        if (targetType == String.class) {
            return String.valueOf(value);
        }

        if (targetType == int.class || targetType == Integer.class) {
            if (value instanceof Number number) {
                return number.intValue();
            }
            return Integer.parseInt(String.valueOf(value));
        }

        if (targetType == long.class || targetType == Long.class) {
            if (value instanceof Number number) {
                return number.longValue();
            }
            return Long.parseLong(String.valueOf(value));
        }

        if (targetType == double.class || targetType == Double.class) {
            if (value instanceof Number number) {
                return number.doubleValue();
            }
            return Double.parseDouble(String.valueOf(value));
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Boolean bool) {
                return bool;
            }
            return Boolean.parseBoolean(String.valueOf(value));
        }

        throw new RuntimeException("暂不支持的工具参数类型:" + targetType.getName());
    }

    // 查找工具
    private ToolMetadata findTool(String toolName) {

        return toolMetadataList.stream()
                .filter(tool -> tool.name().equals(toolName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("找不到工具：" + toolName));
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\tool\ToolMetadata.java`n```
package com.example.agent.core.tool;

import java.lang.reflect.Method;
import java.util.List;

public record ToolMetadata(String name, String description, Object target, Method method,
                List<ToolParameterMetadata> parameters) {
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\tool\ToolParameterMetadata.java`n```
package com.example.agent.core.tool;

public record ToolParameterMetadata(String name, Class<?> type) {

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\tool\ToolScanner.java`n```
package com.example.agent.core.tool;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.annotation.Tool;
import com.example.agent.core.annotation.ToolParam;

public class ToolScanner {
    public static List<ToolMetadata> scan(Object toolObject) {
        List<ToolMetadata> tools = new ArrayList<>();

        Class<?> clazz = toolObject.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Tool.class)) {
                method.setAccessible(true);
                Tool tool = method.getAnnotation(Tool.class);

                String name = method.getName();
                String description = tool.value();
                List<ToolParameterMetadata> parameters = scanParameters(method);
                tools.add(new ToolMetadata(
                        name,
                        description,
                        toolObject,
                        method,
                        parameters));
            }
        }
        return tools;
    }

    private static List<ToolParameterMetadata> scanParameters(Method method) {
        List<ToolParameterMetadata> parameters = new ArrayList<ToolParameterMetadata>();

        Parameter[] reflectParameters = method.getParameters();

        for (Parameter parameter : reflectParameters) {
            // 方法参数
            ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
            if (toolParam == null) {
                throw new RuntimeException("工具方法参数缺少@ToolParam注解" + method.getName());
            }
            parameters.add(new ToolParameterMetadata(toolParam.value(), parameter.getType()));
        }
        return parameters;
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\core\tool\ToolSchemaBuilder.java`n```
package com.example.agent.core.tool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//TODO 后续需要升级减少map的使用
public class ToolSchemaBuilder {
    public static List<Map<String, Object>> build(List<ToolMetadata> tools) {
        List<Map<String, Object>> schemas = new ArrayList<>();

        // 如果数组为空那么直接返回空数组
        if (tools == null || tools.isEmpty()) {
            return schemas;
        }

        tools.forEach(tool -> {
            schemas.add(buildToolSchema(tool));
        });

        return schemas;
    }

    private static Map<String, Object> buildToolSchema(ToolMetadata tool) {
        // 根
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("type", "function");

        // 函数表述
        Map<String, Object> function = new LinkedHashMap<>();
        function.put("name", tool.name());
        function.put("description", tool.description());
        function.put("parameters", buildParametersSchema(tool.parameters()));

        root.put("function", function);
        return root;
    }

    private static Map<String, Object> buildParametersSchema(List<ToolParameterMetadata> parameters) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        if (parameters != null) {
            parameters.forEach(parameter -> {
                Map<String, Object> property = new LinkedHashMap<>();
                property.put("type", toJsonSchemaType(parameter.type()));

                properties.put(parameter.name(), property);
                required.add(parameter.name());
            });
        }

        schema.put("properties", properties);
        schema.put("required", required);

        return schema;
    }

    private static String toJsonSchemaType(Class<?> type) {
        if (type == String.class) {
            return "string";
        }

        if (type == int.class ||
                type == Integer.class || type == long.class || type == Long.class) {
            return "integer";
        }

        if (type == double.class ||
                type == Double.class || type == float.class || type == Float.class) {
            return "number";
        }

        if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        }
        return "string";
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\demo\Assistant.java`n```
package com.example.agent.demo;

import com.example.agent.core.service.TokenStream;

public interface Assistant {
    String chat(String message);

    TokenStream stream(String message);
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\demo\CalculatorTool.java`n```
package com.example.agent.demo;

import com.example.agent.core.annotation.Tool;
import com.example.agent.core.annotation.ToolParam;

public class CalculatorTool {
    @Tool("计算两个整数之和")
    public int add(@ToolParam("a") int a, @ToolParam("b") int b) {
        return a + b;
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\demo\FakeEmbeddingModel.java`n```
package com.example.agent.demo;

public class FakeEmbeddingModel {

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\demo\StreamingAssistant.java`n```
package com.example.agent.demo;

import com.example.agent.core.service.TokenStream;

public interface StreamingAssistant {
    TokenStream chat(String message);
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\demo\WeatherTool.java`n```
package com.example.agent.demo;

import com.example.agent.core.annotation.Tool;
import com.example.agent.core.annotation.ToolParam;

public class WeatherTool {

    @Tool("查询城市天气")
    public String weather(@ToolParam("city") String city, @ToolParam("date") String date) {
        return city + date + "多云，28°C";
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\infrastructure\rag\store\InMemoryEmbeddingStore.java`n```
package com.example.agent.infrastructure.rag.store;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.example.agent.core.rag.document.TextSegment;
import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.core.rag.embedding.EmbeddingMatch;
import com.example.agent.core.rag.embedding.EmbeddingSearchRequest;
import com.example.agent.core.rag.embedding.EmbeddingSearchResult;
import com.example.agent.core.rag.embedding.EmbeddingStore;

public class InMemoryEmbeddingStore implements EmbeddingStore {

    private final List<Entry> entries = new ArrayList<Entry>();

    @Override
    public String add(Embedding embedding, TextSegment segment) {
        if (embedding == null) {
            throw new IllegalArgumentException("embedding 不能为空");
        }

        if (segment == null) {
            throw new IllegalArgumentException("segment 不能为空");
        }
        entries.add(new Entry(embedding, segment));
        return segment.id();
    }

    @Override
    public EmbeddingSearchResult search(EmbeddingSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request 不能为空");
        }

        Embedding queryEmbedding = request.queryEmbedding();

        List<EmbeddingMatch> matches = entries.stream()
                .map(entry -> new EmbeddingMatch(
                        cosineSimilarity(queryEmbedding, entry.embedding),
                        entry.segment.id(),
                        entry.embedding,
                        entry.segment()))
                .filter(match -> match.score() >= request.minScore())
                .sorted(Comparator
                        .comparingDouble(
                                (EmbeddingMatch match) -> match.score())
                        .reversed())
                .limit(request.maxResults())
                .toList();

        return new EmbeddingSearchResult(matches);
    }

    /**
     * calculation the cosine similarity tow embedding
     *
     * @param a the first embedding
     * @param b embedding embedding
     * @return the cosine similarity between {@code a} and {@code b}
     */
    private double cosineSimilarity(Embedding a, Embedding b) {

        float[] av = a.vector();
        float[] bv = b.vector();

        if (av.length != bv.length) {
            throw new IllegalArgumentException(
                    "embedding 维度不一致:"
                            + av.length
                            + "!="
                            + bv.length);
        }

        double dot = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < av.length; i++) {
            dot += av[i] * bv[i];
            normA += av[i] * av[i];
            normB += bv[i] * bv[i];
        }

        if (normA == 0 || normB == 0) {
            return 0;
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));

    }

    /**
     * 
     * Entry 向量以及文本
     * 
     * @param embedding 向量
     * @param segment   原始文本
     */
    public record Entry(
            Embedding embedding,
            TextSegment segment) {

    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\FakeChatModel.java`n```
package com.example.agent.provider;

import com.example.agent.core.model.ChatModel;
import com.example.agent.core.model.ChatModelResponse;
import com.example.agent.core.request.ChatRequest;

public class FakeChatModel implements ChatModel {
    private ChatRequest lastRequest;

    @Override
    public ChatModelResponse chat(ChatRequest request) {
        lastRequest = request;
        return ChatModelResponse.content("fake response");
    }

    @Override
    public String modelName() {
        return "fake-chat-model";
    }

    public ChatRequest lastRequest() {
        return lastRequest;
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\FakeEmbeddingModel.java`n```
package com.example.agent.provider;

import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.core.rag.embedding.EmbeddingModel;

public class FakeEmbeddingModel implements EmbeddingModel {
    private final int dimension;

    public FakeEmbeddingModel() {
        this(1024);
    }

    public FakeEmbeddingModel(int dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("dimension 必须大于0");
        }
        this.dimension = dimension;
    }

    @Override
    public Embedding embed(String text) {
        float[] vector = new float[dimension];

        if (text == null || text.isBlank()) {
            return new Embedding(vector);
        }

        String normalized = text.toLowerCase();

        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);

            if (Character.isWhitespace(ch)
                    || Character.isISOControl(ch)
                    || isPunctuation(ch)) {
                continue;
            }

            int index = Math.floorMod(ch, dimension);
            vector[index] += 1.0f;
        }

        normalized(vector);

        return new Embedding(vector);
    }

    private boolean isPunctuation(char ch) {
        return "，。！？；：,.!?;:、()（）[]【】{}<>《》\"'`".indexOf(ch) >= 0;
    }

    private void normalized(float[] vector) {
        // 向量的长度
        double norm = 0;

        for (float value : vector) {
            norm += value * value;
        }

        if (norm == 0) {
            return;
        }

        double sqrt = Math.sqrt(norm);

        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) (vector[i] / sqrt);
        }
    }

    public int dimension() {
        return dimension;
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\FakeStreamingChatModel.java`n```
package com.example.agent.provider;

import com.example.agent.core.model.StreamingChatModel;
import com.example.agent.core.model.StreamingResponseHandler;
import com.example.agent.core.request.ChatRequest;

public class FakeStreamingChatModel implements StreamingChatModel {
    private ChatRequest lastRequest;
    private int callCount;

    @Override
    public void chat(ChatRequest request, StreamingResponseHandler handler) {
        this.lastRequest = request;
        callCount++;
        handler.onPartialResponse("fake");
        handler.onCompleteResponse("fake");
    }

    @Override
    public String modelName() {
        return "fake-streaming-model";
    }

    public ChatRequest lastRequest() {
        return lastRequest;
    }

    public int callCount() {
        return callCount;
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\milvus\MilvusConnectionConfig.java`n```
package com.example.agent.provider.milvus;

public class MilvusConnectionConfig {
    private final String uri;
    private final String token;
    private final String databaseName;
    private final String collectionName;
    private final String idFieldName;
    private final String contentFieldName;
    private final String vectorFieldName;
    private final int dimension;
    private final MilvusPrimaryKeyMode primaryKeyMode;
    private final boolean flushAfterInsert;
    private final String metadataFieldName;

    private MilvusConnectionConfig(Builder builder) {
        this.uri = builder.uri;
        this.token = builder.token;
        this.databaseName = builder.databaseName;
        this.collectionName = builder.collectionName;
        this.idFieldName = builder.idFieldName;
        this.contentFieldName = builder.contentFieldName;
        this.vectorFieldName = builder.vectorFieldName;
        this.dimension = builder.dimension;
        this.primaryKeyMode = builder.primaryKeyMode;
        this.flushAfterInsert = builder.flushAfterInsert;
        this.metadataFieldName = builder.metadataFieldName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String uri = "http://localhost:19530";
        private String token;
        private String databaseName = "default";
        private String collectionName;
        private String idFieldName = "id";
        private String contentFieldName = "content";
        private String vectorFieldName = "vector";
        private int dimension = 1024;
        private MilvusPrimaryKeyMode primaryKeyMode = MilvusPrimaryKeyMode.INT64_AUTO;
        private String metadataFieldName = "metadata";

        public boolean flushAfterInsert = false;

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;

        }

        public Builder databaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        public Builder flushAfterInsert(boolean flushAfterInsert) {
            this.flushAfterInsert = flushAfterInsert;
            return this;
        }

        public Builder collectionName(String collectionName) {
            if (collectionName == null || collectionName.isBlank()) {
                throw new IllegalArgumentException("collectionName 不能为空");
            }
            this.collectionName = collectionName;
            return this;
        }

        public Builder idFieldName(String idFieldName) {
            this.idFieldName = idFieldName;
            return this;
        }

        public Builder contentFieldName(String contentFieldName) {
            this.contentFieldName = contentFieldName;
            return this;
        }

        public Builder vectorFieldName(String vectorFieldName) {
            this.vectorFieldName = vectorFieldName;
            return this;
        }

        public Builder dimension(int dimension) {
            this.dimension = dimension;
            return this;
        }

        public Builder primaryKeyMode(MilvusPrimaryKeyMode primaryKeyMode) {
            this.primaryKeyMode = primaryKeyMode;
            return this;
        }

        public Builder metadataFieldName(String metadataFieldName) {
            this.metadataFieldName = metadataFieldName;
            return this;
        }

        public MilvusConnectionConfig build() {
            if (uri == null || uri.isBlank()) {
                throw new IllegalArgumentException("uri 不能为空");
            }

            if (databaseName == null || databaseName.isBlank()) {
                throw new IllegalArgumentException("databaseName 不能为空");
            }

            if (collectionName == null || collectionName.isBlank()) {
                throw new IllegalArgumentException("collectionName 不能为空");
            }

            if (idFieldName == null || idFieldName.isBlank()) {
                throw new IllegalArgumentException("idFieldName 不能为空");
            }

            if (contentFieldName == null || contentFieldName.isBlank()) {
                throw new IllegalArgumentException("contentFieldName 不能为空");
            }

            if (vectorFieldName == null || vectorFieldName.isBlank()) {
                throw new IllegalArgumentException("vectorFieldName 不能为空");
            }

            if (dimension <= 0) {
                throw new IllegalArgumentException("dimension 必须大于0");
            }

            if (primaryKeyMode == null) {
                throw new IllegalArgumentException("primaryKeyMode 不能为空");
            }

            if (metadataFieldName == null || metadataFieldName.isBlank()) {
                throw new IllegalArgumentException("metadataFieldName 不能为空");
            }

            return new MilvusConnectionConfig(this);
        }

    }

    public String uri() {
        return uri;
    }

    public String token() {
        return token;
    }

    public String databaseName() {
        return databaseName;
    }

    public String collectionName() {
        return collectionName;
    }

    public String idFieldName() {
        return idFieldName;
    }

    public String contentFieldName() {
        return contentFieldName;
    }

    public String vectorFieldName() {
        return vectorFieldName;
    }

    public int dimension() {
        return dimension;
    }

    public MilvusPrimaryKeyMode primaryKeyMode() {
        return primaryKeyMode;
    }

    public boolean flushAfterInsert() {
        return flushAfterInsert;
    }

    public String metadataFieldName() {
        return metadataFieldName;
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\milvus\MilvusEmbeddingStore.java`n```
package com.example.agent.provider.milvus;

import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.rag.document.TextSegment;
import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.core.rag.embedding.EmbeddingMatch;
import com.example.agent.core.rag.embedding.EmbeddingSearchRequest;
import com.example.agent.core.rag.embedding.EmbeddingSearchResult;
import com.example.agent.core.rag.embedding.EmbeddingStore;
import com.example.agent.core.rag.embedding.EmbeddingStoreException;

public class MilvusEmbeddingStore implements EmbeddingStore {

    private final MilvusJavaClientAdapter adapter;

    private MilvusEmbeddingStore(Builder builder) {
        this.adapter = new MilvusJavaClientAdapter(builder.config);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String add(Embedding embedding, TextSegment segment) {
        return addAll(List.of(embedding), List.of(segment)).get(0);
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings, List<TextSegment> segments) {

        if (embeddings == null) {
            throw new IllegalArgumentException("embedding 不能为空");
        }

        if (segments == null) {
            throw new IllegalArgumentException("segment 不能为空");
        }

        if (embeddings.size() != segments.size()) {
            throw new IllegalArgumentException("embedding 与 segment 数量必须一致");
        }

        List<MilvusInsertRow> rows = new ArrayList<MilvusInsertRow>();

        for (int i = 0; i < embeddings.size(); i++) {
            TextSegment segment = segments.get(i);
            Embedding embedding = embeddings.get(i);

            rows.add(
                    new MilvusInsertRow(
                            segments.get(i).id(),
                            segments.get(i).text(),
                            segment.metadata(),
                            embedding.vector()));
        }

        try {
            return adapter.insert(rows);
        } catch (RuntimeException e) {
            throw new EmbeddingStoreException("Milvus insert 失败", e);
        }
    }

    @Override
    public EmbeddingSearchResult search(EmbeddingSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request 不能为空");
        }

        MilvusSearchRequest searchRequest = MilvusSearchRequest.builder()
                .queryVector(request.queryEmbedding().vector())
                .maxResults(request.maxResults())
                .minScore(request.minScore())
                .build();

        try {
            List<MilvusSearchHit> hits = adapter.search(searchRequest);

            List<EmbeddingMatch> matches = hits.stream()
                    .map(searchHit -> new EmbeddingMatch(
                            searchHit.score(),
                            searchHit.id(),
                            null,
                            new TextSegment(
                                    searchHit.id(),
                                    searchHit.text(),
                                    searchHit.metadata())))
                    .toList();
            return new EmbeddingSearchResult(matches);
        } catch (Exception e) {
            throw new EmbeddingStoreException("Milvus search 失败", e);
        }

    }

    public static class Builder {

        private MilvusConnectionConfig config;

        public Builder config(MilvusConnectionConfig config) {
            this.config = config;
            return this;
        }

        public MilvusEmbeddingStore build() {
            if (config == null) {
                throw new IllegalArgumentException("config 不能为空");
            }
            return new MilvusEmbeddingStore(this);
        }
    }

    public MilvusJavaClientAdapter getAdapter() {
        return adapter;
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\milvus\MilvusInsertRow.java`n```
package com.example.agent.provider.milvus;

import java.util.Map;

public record MilvusInsertRow(
        String id,
        String text,
        Map<String, String> metadata,
        float[] vector) {

    public MilvusInsertRow {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\milvus\MilvusJavaClientAdapter.java`n```
package com.example.agent.provider.milvus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.utility.request.FlushReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.SearchReq.SearchReqBuilder;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;
import io.milvus.v2.service.vector.response.SearchResp.SearchResult;

public class MilvusJavaClientAdapter {

    private final MilvusClientV2 client;
    private final MilvusConnectionConfig config;
    private final Gson gson = new Gson();

    public MilvusJavaClientAdapter(MilvusConnectionConfig config) {
        this.config = config;

        ConnectConfig comConfig = ConnectConfig.builder()
                .uri(config.uri())
                .token(config.token())
                .dbName(config.databaseName())
                .build();

        this.client = new MilvusClientV2(
                comConfig);
    }

    public List<String> insert(List<MilvusInsertRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        List<JsonObject> data = new ArrayList<JsonObject>();

        for (MilvusInsertRow row : rows) {
            validateRow(row);
            data.add(toJsonObject(row));
        }

        InsertReq insertReq = InsertReq.builder()
                .databaseName(config.databaseName())
                .collectionName(config.collectionName())
                .data(data)
                .build();

        InsertResp insertRes = client.insert(insertReq);

        if (config.flushAfterInsert()) {
            client.flush(FlushReq.builder()
                    .databaseName(config.databaseName())
                    .collectionNames(List.of(config.collectionName()))
                    .build());
        }

        return insertRes
                .getPrimaryKeys()
                .stream()
                .map(String::valueOf)
                .toList();
    }

    private void validateRow(MilvusInsertRow row) {
        if (row == null) {
            throw new IllegalArgumentException("row must 不能为空");
        }

        if (row.vector() == null) {
            throw new IllegalArgumentException("row vector 不能为空");
        }

        if (row.text() == null || row.text().isBlank()) {
            throw new IllegalArgumentException("row vector 不能为空");
        }

        if (row.vector().length != config.dimension()) {
            throw new IllegalArgumentException("vector dimension mismatch 维度不一致config向量维度：" + config.dimension()
                    + "!=vector维度：" + row.vector().length);
        }
    }

    private JsonObject toJsonObject(MilvusInsertRow row) {
        JsonObject object = new JsonObject();

        writePrimaryKey(object, row);
        object.addProperty(config.contentFieldName(), row.text());

        if (config.metadataFieldName() != null && !config.metadataFieldName().isBlank()) {
            object.add(config.metadataFieldName(), gson.toJsonTree(row.metadata()));
        }

        object.add(config.vectorFieldName(), gson.toJsonTree(row.vector()));

        return object;
    }

    private void writePrimaryKey(JsonObject object, MilvusInsertRow row) {
        switch (config.primaryKeyMode()) {
            case INT64_AUTO -> {

            }
            case INT64_MANUAL -> {
                object.addProperty(config.idFieldName(), Long.parseLong(row.id()));
            }
            case VARCHAR_MANUAL -> {
                object.addProperty(config.idFieldName(), row.id());
            }

        }
    }

    public List<MilvusSearchHit> search(MilvusSearchRequest request) {
        validateSearchRequest(request);

        ArrayList<String> outputFields = new ArrayList<String>();
        outputFields.add(config.idFieldName());
        outputFields.add(config.contentFieldName());

        if (config.metadataFieldName() != null && !config.metadataFieldName().isBlank()) {
            outputFields.add(config.metadataFieldName());
        }

        SearchReqBuilder builder = SearchReq.builder()
                .databaseName(config.databaseName())
                .collectionName(config.collectionName())
                .annsField(config.vectorFieldName())
                .data(Collections.singletonList(new FloatVec(request.queryVector())))
                .limit(request.maxResults())
                .outputFields(outputFields);

        SearchResp searchResp = client.search(builder.build());

        return toSearchHits(searchResp, request.minScore());
    }

    private List<MilvusSearchHit> toSearchHits(SearchResp searchResp, double minScore) {
        List<MilvusSearchHit> hits = new ArrayList<MilvusSearchHit>();

        if (searchResp == null || searchResp.getSearchResults().isEmpty()) {
            return hits;
        }

        List<SearchResult> results = searchResp.getSearchResults()
                .get(0);

        for (SearchResult result : results) {
            double score = result.getScore();

            if (score < minScore) {
                continue;
            }

            Object id = result.getId();
            if (id == null) {
                id = result.getEntity().get(config.idFieldName());
            }

            Object text = result.getEntity().get(config.contentFieldName());

            if (text == null || id == null) {
                continue;
            }

            hits.add(new MilvusSearchHit(
                    String.valueOf(id),
                    text.toString(),
                    readMetadata(result),
                    score));
        }

        return hits;

    }

    private Map<String, String> readMetadata(SearchResult result) {
        if (config.metadataFieldName() == null || config.metadataFieldName().isBlank()) {
            return Map.of();
        }

        Object metadata = result.getEntity().get(config.metadataFieldName());

        if (metadata == null) {
            return Map.of();
        }

        if (metadata instanceof Map<?, ?> map) {
            return map.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            entry -> String.valueOf(entry.getKey()),
                            entry -> String.valueOf(entry.getValue())));
        }
        return Map.of();

    }

    private void validateSearchRequest(MilvusSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request 不能为空");
        }

        if (request.queryVector() == null) {
            throw new IllegalArgumentException("queryVector 不能为空");
        }

        if (request.queryVector().length != config.dimension()) {
            throw new IllegalArgumentException(
                    "查询的向量维度与集合的向量维度不一致" + config.dimension() + "!=" + request.queryVector().length);
        }
    }

    public MilvusConnectionConfig config() {
        return config;
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\milvus\MilvusPrimaryKeyMode.java`n```
package com.example.agent.provider.milvus;

public enum MilvusPrimaryKeyMode {
    INT64_AUTO,
    INT64_MANUAL,
    VARCHAR_MANUAL
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\milvus\MilvusSearchHit.java`n```
package com.example.agent.provider.milvus;

import java.util.Map;

public record MilvusSearchHit(
                String id,
                String text,
                Map<String, String> metadata,
                double score) {

        public MilvusSearchHit {
                if (id == null || id.isBlank()) {
                        throw new IllegalArgumentException("id 不能为空");
                }

                if (text == null || text.isBlank()) {
                        throw new IllegalArgumentException("text 不能为空");
                }

                if (!Double.isFinite(score)) {
                        throw new IllegalArgumentException("score 必须是有限数值");
                }
                metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\milvus\MilvusSearchRequest.java`n```
package com.example.agent.provider.milvus;

public class MilvusSearchRequest {
    private final float[] queryVector;
    private final int maxResults;
    private final double minScore;
    private final String filter;

    private MilvusSearchRequest(Builder builder) {
        this.queryVector = builder.queryVector;
        this.maxResults = builder.maxResults;
        this.minScore = builder.minScore;
        this.filter = builder.filter;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private float[] queryVector;
        private int maxResults = 3;
        private double minScore = 0.7;
        private String filter;

        public Builder queryVector(float[] queryVector) {
            this.queryVector = queryVector;
            return this;
        }

        public Builder maxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public Builder minScore(double minScore) {
            this.minScore = minScore;
            return this;
        }

        public Builder filter(String filter) {
            this.filter = filter;
            return this;
        }

        public MilvusSearchRequest build() {
            if (queryVector == null) {
                throw new IllegalArgumentException("queryVector 不能为空");
            }
            if (maxResults <= 0) {
                throw new IllegalArgumentException("maxResults 不能为空");
            }
            if (minScore < 0 || minScore > 1) {
                throw new IllegalArgumentException("minScore 只能在0-1之间");
            }
            return new MilvusSearchRequest(this);
        }
    }

    public float[] queryVector() {
        return queryVector;
    }

    public int maxResults() {
        return maxResults;
    }

    public double minScore() {
        return minScore;
    }

    public String filter() {
        return filter;
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\request\OpenAiChatRequest.java`n```
package com.example.agent.provider.openai.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiChatRequest(
                String model,
                Double temperature,
                Integer maxTokens,
                Boolean enableThinking,
                Boolean stream,
                List<OpenAiMessage> messages,
                List<OpenAiTool> tools) {
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\request\OpenAiEmbeddingRequest.java`n```
package com.example.agent.provider.openai.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenAiEmbeddingRequest(
                String model,
                List<String> input,
                @JsonProperty("encoding_format") String encodingFormat,
                int dimensions) {
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\request\OpenAiFunction.java`n```
package com.example.agent.provider.openai.dto.request;

public record OpenAiFunction(
        String name,
        String description,
        OpenAiParameters parameters) {

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\request\OpenAiMessage.java`n```
package com.example.agent.provider.openai.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiMessage(
                String role,
                String content,
                String toolCallId,
                List<OpenAiRequestToolCall> toolCalls) {
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\request\OpenAiParameters.java`n```
package com.example.agent.provider.openai.dto.request;

import java.util.List;
import java.util.Map;

public record OpenAiParameters(
        String type,
        Map<String, OpenAiProperty> properties,
        List<String> required) {

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\request\OpenAiProperty.java`n```
package com.example.agent.provider.openai.dto.request;

public record OpenAiProperty(String type) {

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\request\OpenAiRequestToolCall.java`n```
package com.example.agent.provider.openai.dto.request;

public record OpenAiRequestToolCall(
        String id, String type, OpenAiRequestToolFunction function) {

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\request\OpenAiRequestToolFunction.java`n```
package com.example.agent.provider.openai.dto.request;

public record OpenAiRequestToolFunction(
        String name, String arguments) {
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\request\OpenAiTool.java`n```
package com.example.agent.provider.openai.dto.request;

public record OpenAiTool(String type, OpenAiFunction function) {

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\response\OpenAiChatResponse.java`n```
package com.example.agent.provider.openai.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiChatResponse(
                String id,
                String object,
                long created,
                String model,
                List<OpenAiChoice> choices,
                OpenAiUsage usage,
                String systemFingerprint) {

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\response\OpenAiChoice.java`n```
package com.example.agent.provider.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiChoice(
                int index,
                OpenAiResponseMessage message,

                String finishReason) {
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\response\OpenAiCompletionTokensDetails.java`n```
package com.example.agent.provider.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiCompletionTokensDetails(
                int reasoningTokens) {
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\response\OpenAiEmbeddingData.java`n```
package com.example.agent.provider.openai.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiEmbeddingData(
        String object,
        List<Float> embedding,
        int index) {

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\response\OpenAiEmbeddingResponse.java`n```
package com.example.agent.provider.openai.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiEmbeddingResponse(
        String object,
        List<OpenAiEmbeddingData> data,
        String model,
        OpenAiEmbeddingUsage usage) {

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\response\OpenAiEmbeddingUsage.java`n```
package com.example.agent.provider.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiEmbeddingUsage(
        @JsonProperty("prompt_tokens") int promptTokens,
        @JsonProperty("total_tokens") int totalTokens) {
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\response\OpenAiPromptTokensDetails.java`n```
package com.example.agent.provider.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiPromptTokensDetails(
                int cachedTokens) {
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\response\OpenAiResponseMessage.java`n```
package com.example.agent.provider.openai.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiResponseMessage(
        String role,
        String content,
        String reasoningContent,
        List<OpenAiToolCall> toolCalls) {
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\response\OpenAiToolCall.java`n```
package com.example.agent.provider.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiToolCall(
        String id,
        String type,
        OpenAiToolFunction function) {
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\response\OpenAiToolFunction.java`n```
package com.example.agent.provider.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiToolFunction(
        String name, String arguments) {
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\response\OpenAiUsage.java`n```
package com.example.agent.provider.openai.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiUsage(
                int promptTokens,
                int completionTokens,
                int totalTokens,
                OpenAiCompletionTokensDetails completionTokensDetails,
                OpenAiPromptTokensDetails promptTokensDetails,
                int promptCacheHitTokens,
                int promptCacheMissTokens) {
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\stream\OpenAiStreamChoice.java`n```
package com.example.agent.provider.openai.dto.stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiStreamChoice(
        Integer index,
        OpenAiStreamDelta delta,
        String finishReason) {

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\stream\OpenAiStreamDelta.java`n```
package com.example.agent.provider.openai.dto.stream;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiStreamDelta(
                String role,
                String content,
                String reasoningContent,
                List<OpenAiStreamToolCall> toolCalls) {

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\stream\OpenAiStreamResponse.java`n```
package com.example.agent.provider.openai.dto.stream;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiStreamResponse(
    String id,
    String object,
    Long created,
    String model,
    List<OpenAiStreamChoice> choices
) {
    
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\stream\OpenAiStreamToolCall.java`n```
package com.example.agent.provider.openai.dto.stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiStreamToolCall(
        Integer index,
        String id,
        String type,
        OpenAiStreamToolFunction function) {

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\dto\stream\OpenAiStreamToolFunction.java`n```
package com.example.agent.provider.openai.dto.stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiStreamToolFunction(
        String name,
        String arguments) {

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\OpenAiChatModel.java`n```
package com.example.agent.provider.openai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.example.agent.core.model.ChatModel;
import com.example.agent.core.model.ChatModelResponse;
import com.example.agent.core.request.ChatRequest;
import com.example.agent.provider.openai.dto.request.OpenAiChatRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class OpenAiChatModel implements ChatModel {
    private static final String DEFAULT_BASE_URL = "https://api.siliconflow.cn/v1";
    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    private Duration requestTimeout = Duration.ofSeconds(60);

    private final HttpClient httpClient;
    private final OpenAiChatRequestBuilder requestBuilder;
    private final OpenAiChatResponseParser responseParser;

    private OpenAiChatModel(Builder builder) {
        this.baseUrl = normalizeBaseUrl(builder.baseUrl);
        this.apiKey = builder.apiKey;
        this.model = builder.model;

        this.httpClient = builder.httpClient;
        this.requestBuilder = builder.requestBuilder;
        this.responseParser = builder.responseParser;
        this.requestTimeout = builder.requestTimeout;
    }

    public final static Builder builder() {
        return new Builder();
    }

    @Override
    public ChatModelResponse chat(ChatRequest message) {
        ChatRequest actualRequest = withDefaultModel(message);

        OpenAiChatRequest openAiChatRequest = requestBuilder.build(actualRequest);
        String requestBody = toJson(openAiChatRequest);

        HttpRequest httpRequest;
        try {
            httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + CHAT_COMPLETIONS_PATH))
                    .timeout(requestTimeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode < 200 || statusCode >= 300) {
                throw new RuntimeException("""
                        OpenAI HTTP 调用失败
                        statusCode: %s
                        responsesBody: %s
                        """.formatted(statusCode, responseBody));
            }

            ChatModelResponse chatModelResponse = responseParser.parse(responseBody);
            return chatModelResponse;
        } catch (HttpTimeoutException e) {
            throw new RuntimeException("""
                    OpenAI HTTP 请求超时
                    baseUrl:%s
                    model:%s
                    timeout:%s
                    """.formatted(baseUrl, model, requestTimeout), e);
        } catch (IOException e) {
            throw new RuntimeException("""
                    OpenAI HTTP 请求失败
                    BaseUrl:%s
                    model:%s
                    reason:%s
                    """.formatted(baseUrl, model, e.getMessage()), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("OpenAI HTTP 请求被中断", e);
        }

    }

    private ChatRequest withDefaultModel(ChatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("ChatRequest 不能为空");
        }
        if (request.model() != null && !request.model().isBlank()) {
            return request;
        }
        return ChatRequest.builder()
                .model(model)
                .systemMessage(request.systemMessage())
                .messages(request.messages())
                .tools(request.tools())
                .temperature(request.temperature())
                .maxTokens(request.maxTokens())
                .enableThinking(request.enableThinking())
                .build();
    }

    private String toJson(OpenAiChatRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("OpenAI 请求体序列化失败", e);
        }
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return DEFAULT_BASE_URL;
        }
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    public static class Builder {
        private String baseUrl = DEFAULT_BASE_URL;
        private String apiKey;
        private String model;
        private Duration requestTimeout = Duration.ofSeconds(60);

        private HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        private OpenAiChatRequestBuilder requestBuilder = new OpenAiChatRequestBuilder();
        private OpenAiChatResponseParser responseParser = new OpenAiChatResponseParser();

        public Builder baseUrl(String baseUrl) {
            if (baseUrl != null && !baseUrl.isBlank()) {
                this.baseUrl = baseUrl;
            }
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder requestBuilder(OpenAiChatRequestBuilder requestBuilder) {
            this.requestBuilder = requestBuilder;
            return this;
        }

        public Builder requestTimeout(Duration requestTimeout) {
            if (requestTimeout != null) {
                this.requestTimeout = requestTimeout;
            }
            return this;
        }

        public Builder responseParser(OpenAiChatResponseParser responseParser) {
            this.responseParser = responseParser;
            return this;
        }

        public OpenAiChatModel build() {
            if (apiKey == null || apiKey.isBlank()) {
                throw new RuntimeException("apiKey 不能为空");
            }

            if (model == null || model.isBlank()) {
                throw new RuntimeException("model不能为空");
            }

            return new OpenAiChatModel(this);
        }
    }

    public static String getDefaultBaseUrl() {
        return DEFAULT_BASE_URL;
    }

    public static String getChatCompletionsPath() {
        return CHAT_COMPLETIONS_PATH;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public OpenAiChatRequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

    public OpenAiChatResponseParser getResponseParser() {
        return responseParser;
    }

    @Override
    public String modelName() {
        return model;
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\OpenAiChatRequestBuilder.java`n```
package com.example.agent.provider.openai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.agent.core.message.AssistantToolCallMessage;
import com.example.agent.core.message.ChatMessage;
import com.example.agent.core.message.ToolMessage;
import com.example.agent.core.request.ChatRequest;
import com.example.agent.core.request.ChatRequestBuilder;
import com.example.agent.core.tool.ToolCall;
import com.example.agent.core.tool.ToolParameterMetadata;
import com.example.agent.provider.openai.dto.request.OpenAiChatRequest;
import com.example.agent.provider.openai.dto.request.OpenAiFunction;
import com.example.agent.provider.openai.dto.request.OpenAiMessage;
import com.example.agent.provider.openai.dto.request.OpenAiParameters;
import com.example.agent.provider.openai.dto.request.OpenAiProperty;
import com.example.agent.provider.openai.dto.request.OpenAiRequestToolCall;
import com.example.agent.provider.openai.dto.request.OpenAiRequestToolFunction;
import com.example.agent.provider.openai.dto.request.OpenAiTool;

public class OpenAiChatRequestBuilder implements ChatRequestBuilder<OpenAiChatRequest> {

    @Override
    public OpenAiChatRequest build(ChatRequest request) {
        return build(request, null);
    }

    public OpenAiChatRequest build(ChatRequest request, Boolean stream) {
        OpenAiChatRequest openAiChatRequest = new OpenAiChatRequest(
                request.model(),
                request.temperature(),
                request.maxTokens(),
                request.enableThinking(),
                stream,
                buildMessage(request),
                buildTools(request));
        return openAiChatRequest;
    }

    private static List<OpenAiMessage> buildMessage(ChatRequest request) {
        List<OpenAiMessage> messages = new ArrayList<>();

        // 构造系统提示词
        if (request.systemMessage() != null && !request.systemMessage().isBlank()) {
            messages.add(new OpenAiMessage(
                    "system",
                    request.systemMessage(),
                    null,
                    null));
        }

        // 构造消息
        if (request.messages() != null) {
            request.messages().forEach(msg -> {
                messages.add(buildMessage(msg));
            });
        }
        return messages;
    }

    private static OpenAiMessage buildMessage(ChatMessage message) {
        if (message instanceof AssistantToolCallMessage assistantToolCallMessage) {
            return new OpenAiMessage(
                    "assistant",
                    assistantToolCallMessage.content(),
                    null,
                    buildRequestToolCalls(assistantToolCallMessage.toolCalls()));
        }

        if (message instanceof ToolMessage toolMessage) {
            return new OpenAiMessage(
                    "tool",
                    toolMessage.content(),
                    toolMessage.toolCallId(),
                    null);
        }
        return new OpenAiMessage(
                message.role(),
                message.content(),
                null,
                null);

    }

    private static List<OpenAiRequestToolCall> buildRequestToolCalls(List<ToolCall> toolCalls) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            return List.of();
        }

        List<OpenAiRequestToolCall> result = new ArrayList<OpenAiRequestToolCall>();

        for (ToolCall toolCall : toolCalls) {
            OpenAiRequestToolFunction function = new OpenAiRequestToolFunction(
                    toolCall.name(),
                    toolCall.arguments());

            OpenAiRequestToolCall openAiRequestToolCall = new OpenAiRequestToolCall(
                    toolCall.id(),
                    "function",
                    function);
            result.add(openAiRequestToolCall);
        }
        return result;
    }

    private static List<OpenAiTool> buildTools(ChatRequest request) {
        if (request.tools() == null || request.tools().isEmpty()) {
            return null;
        }

        List<OpenAiTool> tools = new ArrayList<OpenAiTool>();

        request.tools().forEach(tool -> {
            OpenAiFunction function = new OpenAiFunction(
                    tool.name(),
                    tool.description(),
                    buildParameters(tool.parameters()));
            tools.add(new OpenAiTool("function", function));
        });

        return tools;
    }

    private static OpenAiParameters buildParameters(List<ToolParameterMetadata> parameters) {
        Map<String, OpenAiProperty> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        if (parameters != null) {
            parameters.forEach(parameter -> {
                properties.put(parameter.name(),
                        new OpenAiProperty(toJsonSchemaType(
                                parameter.type())));
                required.add(parameter.name());
            });
        }

        return new OpenAiParameters("object", properties, required);
    }

    private static String toJsonSchemaType(Class<?> type) {
        if (type == String.class) {
            return "string";
        }

        if (type == int.class || type == Integer.class || type == long.class || type == Long.class) {
            return "integer";
        }

        if (type == double.class || type == Double.class || type == float.class || type == Float.class) {
            return "number";
        }

        if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        }
        return "string";
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\OpenAiChatResponseParser.java`n```
package com.example.agent.provider.openai;

import java.util.List;

import com.example.agent.core.model.ChatModelResponse;
import com.example.agent.core.parser.ChatResponseParser;
import com.example.agent.core.tool.ToolCall;
import com.example.agent.provider.openai.dto.response.OpenAiChatResponse;
import com.example.agent.provider.openai.dto.response.OpenAiChoice;
import com.example.agent.provider.openai.dto.response.OpenAiResponseMessage;
import com.example.agent.provider.openai.dto.response.OpenAiToolFunction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenAiChatResponseParser implements ChatResponseParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatModelResponse parse(String responseJson) {
        try {
            OpenAiChatResponse response = objectMapper.readValue(responseJson, OpenAiChatResponse.class);
            OpenAiChoice choice = firstChoice(response);

            OpenAiResponseMessage message = choice.message();

            if (message.toolCalls() != null && !message.toolCalls().isEmpty()) {
                List<ToolCall> toolCalls = message.toolCalls()
                        .stream()
                        .map(toolCall -> {
                            if (toolCall.function() == null) {
                                throw new RuntimeException("OpenAI 工具调用中没有function");
                            }
                            OpenAiToolFunction function = toolCall.function();
                            return new ToolCall(toolCall.id(), function.name(), function.arguments());
                        }).toList();

                return ChatModelResponse.toolCall(message.content(), toolCalls);
            }

            return new ChatModelResponse(
                    message.content() == null ? "" : message.content(),
                    List.of(),
                    choice.finishReason());

        } catch (JsonMappingException e) {
            throw new RuntimeException("字段映射失败：" + e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析失败：" + e);
        }
    }

    private OpenAiChoice firstChoice(OpenAiChatResponse response) {
        if (response == null) {
            throw new RuntimeException("OpenAI 响应为空");
        }

        List<OpenAiChoice> choices = response.choices();

        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("OpenAI 响应中没有 choices");
        }

        OpenAiChoice firstChoice = choices.get(0);

        if (firstChoice == null) {
            throw new RuntimeException("OpenAI 响应中 choices[0] 为空");
        }

        if (firstChoice.message() == null) {
            throw new RuntimeException("OpenAI 响应中 choices[0].message 为空");
        }
        return firstChoice;
    }
}
```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\OpenAiEmbeddingException.java`n```
package com.example.agent.provider.openai;

public class OpenAiEmbeddingException extends RuntimeException {

    public OpenAiEmbeddingException(String message) {
        super(message);
    }

    public OpenAiEmbeddingException(String message, Throwable cause) {
        super(message, cause);
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\OpenAiEmbeddingModel.java`n```
package com.example.agent.provider.openai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.example.agent.core.rag.document.TextSegment;
import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.core.rag.embedding.EmbeddingModel;
import com.example.agent.provider.openai.dto.request.OpenAiEmbeddingRequest;
import com.example.agent.provider.openai.parser.OpenAiEmbeddingResponseParser;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class OpenAiEmbeddingModel implements EmbeddingModel {
    private static final String DEFAULT_BASE_URL = "https://api.siliconflow.cn/v1";
    private static final String EMBEDDING_PATH = "/embeddings";
    private final String baseUrl;
    private final String apiKey;
    private final String modelName;
    private final Duration requestTimeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OpenAiEmbeddingResponseParser responseParser;
    private final int dimension;

    private OpenAiEmbeddingModel(Builder builder) {
        this.baseUrl = normalizeBaseUrl(builder.baseUrl);
        this.apiKey = builder.apiKey;
        this.modelName = builder.modelName;
        this.requestTimeout = builder.requestTimeout;
        this.dimension = builder.dimension;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(requestTimeout)
                .build();

        this.objectMapper = new ObjectMapper();
        this.responseParser = new OpenAiEmbeddingResponseParser(objectMapper);
    }

    @Override
    public Embedding embed(String text) {
        return embedTexts(List.of(text)).get(0);
    }

    @Override
    public List<Embedding> embeddingAll(List<TextSegment> segments) {

        if (segments == null || segments.isEmpty()) {
            return List.of();
        }

        List<String> inputs = new ArrayList<String>(segments.size());

        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            if (segment == null) {
                throw new IllegalArgumentException("segments[" + i + "]不能为空");
            }
            inputs.add(segment.text());
        }
        return embedTexts(inputs);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String baseUrl = DEFAULT_BASE_URL;
        private String apiKey;
        private String modelName;
        private Duration requestTimeout = Duration.ofSeconds(30);
        private int dimension = 1024;

        public Builder baseUrl(String baseUrl) {
            if (baseUrl == null || baseUrl.isBlank()) {
                throw new IllegalArgumentException("");
            }
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder apiKey(String apiKey) {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalArgumentException("");
            }
            this.apiKey = apiKey;
            return this;

        }

        public Builder modelName(String modelName) {
            if (modelName == null || modelName.isBlank()) {
                throw new IllegalArgumentException("");
            }
            this.modelName = modelName;
            return this;

        }

        public Builder requestTimeout(Duration requestTimeout) {
            if (requestTimeout == null || requestTimeout.isZero() || requestTimeout.isNegative()) {
                throw new IllegalArgumentException("");
            }
            this.requestTimeout = requestTimeout;
            return this;

        }

        public OpenAiEmbeddingModel build() {
            if (apiKey == null) {
                throw new IllegalStateException(" 必须装配 apiKey");
            }

            if (modelName == null) {
                throw new IllegalStateException("必须装配 modelName");
            }

            return new OpenAiEmbeddingModel(this);
        }

        public Builder dimension(int dimension) {
            this.dimension = dimension;
            return this;
        }

    }

    private static String normalizeBaseUrl(String baseUrl) {
        String normalized = baseUrl.trim();

        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(
                    0, normalized.length() - 1);
        }

        return normalized;
    }

    private List<Embedding> embedTexts(List<String> inputs) {
        validateInputs(inputs);

        OpenAiEmbeddingRequest requestBody = new OpenAiEmbeddingRequest(
                modelName,
                List.copyOf(inputs),
                "float",
                this.dimension);

        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + EMBEDDING_PATH))
                    .timeout(requestTimeout)
                    .header(
                            "Authorization",
                            "Bearer " + apiKey)
                    .header(
                            "Content-Type",
                            "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            jsonBody,
                            StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new OpenAiEmbeddingException(
                        "Embedding 请求被失败,status="
                                + response.statusCode()
                                + ",body="
                                + response.body());
            }

            List<Embedding> embeddings = responseParser.parse(response.body());

            if (embeddings.size() != inputs.size()) {
                throw new OpenAiEmbeddingException(
                        "Embedding 返回数量与输入数量不一致，input="
                                + inputs.size()
                                + ",output="
                                + embeddings.size());
            }

            return embeddings;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenAiEmbeddingException("Embedding 请求中断", e);
        } catch (IOException e) {
            throw new OpenAiEmbeddingException("Embedding HTTP 请求失败", e);
        }

    }

    public int dimension() {
        return dimension;
    }

    private void validateInputs(List<String> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException("inputs 不能为空");
        }

        for (int i = 0; i < inputs.size(); i++) {
            String input = inputs.get(i);

            if (input == null || input.isBlank()) {
                throw new IllegalArgumentException("inputs[" + i + "]不能为空");
            }
        }
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\OpenAiHttpException.java`n```
package com.example.agent.provider.openai;

public class OpenAiHttpException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public OpenAiHttpException(int statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int statusCode() {
        return statusCode;
    }

    public String responseBody() {
        return responseBody;
    }

}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\OpenAiStreamingChatModel.java`n```
package com.example.agent.provider.openai;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import java.util.Objects;

import com.example.agent.core.model.StreamingChatModel;
import com.example.agent.core.model.StreamingResponseHandler;
import com.example.agent.core.request.ChatRequest;
import com.example.agent.provider.openai.OpenAiStreamParser.StreamChunk;
import com.example.agent.provider.openai.dto.request.OpenAiChatRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenAiStreamingChatModel implements StreamingChatModel {

    private static final String DEFAULT_BASE_URL = "https://api.siliconflow.cn/v1/chat/completions";

    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final Duration requestTimeout;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OpenAiStreamParser streamParser;

    public OpenAiStreamingChatModel(Builder builder) {
        this.apiKey = builder.apiKey;
        this.model = builder.model;
        this.baseUrl = builder.baseUrl;
        this.requestTimeout = builder.requestTimeout;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(builder.requestTimeout)
                .build();

        this.objectMapper = new ObjectMapper();
        this.streamParser = new OpenAiStreamParser();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void chat(ChatRequest request, StreamingResponseHandler handler) {
        Objects.requireNonNull(request, "request不能为空");
        Objects.requireNonNull(handler, "handler不能为空");

        StringBuilder completeResponse = new StringBuilder();
        OpenAiStreamToolCallAccumulator toolCallAccumulator = new OpenAiStreamToolCallAccumulator();

        try {
            ChatRequest finalRequest = request.withDefaultModel(model);

            OpenAiChatRequestBuilder openAiChatRequestBuilder = new OpenAiChatRequestBuilder();
            OpenAiChatRequest openAiRequest = openAiChatRequestBuilder.build(finalRequest, true);

            String requestBody = objectMapper.writeValueAsString(openAiRequest);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            requestBody,
                            StandardCharsets.UTF_8));

            if (requestTimeout != null) {
                requestBuilder.timeout(requestTimeout);
            }

            // 发送请求
            HttpResponse<InputStream> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());

            // 当发生错误时
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new RuntimeException("OpenAI STream请求失败,status="
                        + response.statusCode()
                        + ",body="
                        + errorBody);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }

                    if (!line.startsWith("data:")) {
                        continue;
                    }

                    String data = line.substring("data:".length()).trim();

                    // 得到响应块
                    StreamChunk chunk = streamParser.parse(data);
                    if (chunk.isDone()) {
                        break;
                    }

                    if (chunk.isToolCallDelta()) {
                        toolCallAccumulator.append(chunk.toolCalls());
                        continue;
                    }

                    if (chunk.isReasoning()) {
                        handler.onPartialReasoning(chunk.content());
                        continue;
                    }

                    if (chunk.isContent()) {
                        completeResponse.append(chunk.content());
                        handler.onPartialResponse(chunk.content());
                    }

                }
            }

            if (toolCallAccumulator.hasToolCalls()) {
                handler.onToolCalls(toolCallAccumulator.toToolCalls());
            }

            handler.onCompleteResponse(completeResponse.toString());
        } catch (Throwable e) {
            handler.onError(e);
        }
    }

    public static class Builder {
        private String apiKey;
        private String model;
        private String baseUrl = DEFAULT_BASE_URL;
        private Duration requestTimeout = Duration.ofSeconds(60);

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        public OpenAiStreamingChatModel build() {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalArgumentException("apiKey不能为空");
            }

            if (model == null || model.isBlank()) {
                throw new IllegalArgumentException("model不能为空");
            }

            if (baseUrl == null || baseUrl.isBlank()) {
                throw new IllegalArgumentException("baseUrl不能为空");
            }

            return new OpenAiStreamingChatModel(this);
        }
    }

    @Override
    public String modelName() {
        return model;
    }
}

```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\OpenAiStreamParser.java`n```
package com.example.agent.provider.openai;

import java.util.List;

import com.example.agent.core.request.ChunkType;
import com.example.agent.provider.openai.dto.stream.OpenAiStreamChoice;
import com.example.agent.provider.openai.dto.stream.OpenAiStreamDelta;
import com.example.agent.provider.openai.dto.stream.OpenAiStreamResponse;
import com.example.agent.provider.openai.dto.stream.OpenAiStreamToolCall;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenAiStreamParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StreamChunk parse(String data) {
        try {

            if (data == null || data.isBlank()) {
                return StreamChunk.empty();
            }

            // 修剪
            String trimmed = data.trim();
            if ("[DONE]".equals(trimmed)) {
                return StreamChunk.done();
            }

            OpenAiStreamResponse response = objectMapper.readValue(trimmed, OpenAiStreamResponse.class);

            List<OpenAiStreamChoice> choices = response.choices();
            if (choices == null || choices.isEmpty()) {
                return StreamChunk.empty();
            }

            OpenAiStreamChoice openAiStreamChoice = choices.get(0);
            if (openAiStreamChoice == null || openAiStreamChoice.delta() == null) {
                return StreamChunk.empty();
            }

            OpenAiStreamDelta delta = openAiStreamChoice.delta();
            if (delta.toolCalls() != null && !delta.toolCalls().isEmpty()) {
                return StreamChunk.toolCallDelta(delta.toolCalls());
            }

            if (delta.reasoningContent() != null && !delta.reasoningContent().isEmpty()) {
                return StreamChunk.reasoningContent(delta.reasoningContent());
            }

            if (delta.content() != null && !delta.content().isEmpty()) {
                return StreamChunk.content(delta.content());
            }

            return StreamChunk.empty();

        } catch (Exception e) {
            throw new RuntimeException("解析 OpenAI Stream 数据失败: " + data, e);
        }
    }

    public record StreamChunk(
            String content,
            ChunkType type,
            List<OpenAiStreamToolCall> toolCalls) {

        public static StreamChunk reasoningContent(String reasoningContent) {
            return new StreamChunk(reasoningContent, ChunkType.REASONING, List.of());
        }

        public static StreamChunk content(String content) {
            return new StreamChunk(content, ChunkType.CONTENT, List.of());
        }

        public static StreamChunk toolCallDelta(List<OpenAiStreamToolCall> toolCalls) {
            return new StreamChunk(
                    "",
                    ChunkType.TOOL_CALL_DELTA,
                    toolCalls);
        }

        public static StreamChunk done() {
            return new StreamChunk("", ChunkType.DONE, List.of());
        }

        public static StreamChunk empty() {
            return new StreamChunk("", ChunkType.EMPTY, List.of());
        }

        public boolean isReasoning() {
            return type == ChunkType.REASONING;
        }

        public boolean isDone() {
            return type == ChunkType.DONE;
        }

        public boolean isContent() {
            return type == ChunkType.CONTENT;
        }

        public boolean isToolCallDelta() {
            return type == ChunkType.TOOL_CALL_DELTA;
        }

        public boolean hasContent() {
            return content != null && !content.isEmpty();
        }

        public boolean hasCalls() {
            return toolCalls != null && !toolCalls.isEmpty();
        }
    }
}
```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\OpenAiStreamToolCallAccumulator.java`n```
package com.example.agent.provider.openai;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.example.agent.core.tool.ToolCall;
import com.example.agent.provider.openai.dto.stream.OpenAiStreamToolCall;
import com.example.agent.provider.openai.dto.stream.OpenAiStreamToolFunction;

public class OpenAiStreamToolCallAccumulator {

    private final Map<Integer, ToolCallBuilder> builders = new HashMap<>();

    public void append(List<OpenAiStreamToolCall> toolCallDeltas) {
        if (toolCallDeltas == null || toolCallDeltas.isEmpty()) {
            return;
        }

        for (OpenAiStreamToolCall delta : toolCallDeltas) {
            if (delta == null) {
                continue;
            }

            Integer index = delta.index();
            if (index == null) {
                index = 0;
            }

            ToolCallBuilder builder = builders.computeIfAbsent(
                    index,
                    ignored -> new ToolCallBuilder());

            if (delta.id() != null && !delta.id().isBlank()) {
                builder.id = delta.id();
            }

            if (delta.type() != null && !delta.type().isBlank()) {
                builder.type = delta.type();
            }

            OpenAiStreamToolFunction function = delta.function();

            if (function == null) {
                continue;
            }

            if (function.name() != null && !function.name().isBlank()) {
                builder.name = function.name();
            }

            if (function.arguments() != null && !function.arguments().isEmpty()) {
                builder.arguments.append(function.arguments());
            }
        }
    }

    public boolean hasToolCalls() {
        return !builders.isEmpty();
    }

    public List<ToolCall> toToolCalls() {
        return builders.entrySet()
                .stream()
                .sorted(Comparator.comparingInt((Entry<Integer, ?> entry) -> entry.getKey()))
                .map(entry -> entry.getValue().build())
                .toList();
    }

    public void clear() {
        builders.clear();
    }

    private static class ToolCallBuilder {
        private String id;
        private String type;
        private String name;
        private final StringBuilder arguments = new StringBuilder();

        private ToolCall build() {
            if (id == null || id.isBlank()) {
                throw new RuntimeException("流式工具调用缺少 id");
            }

            if (name == null || name.isBlank()) {
                throw new RuntimeException("流式工具调用缺少 function.name");
            }

            return new ToolCall(
                    id,
                    name,
                    arguments.toString());
        }
    }
}
```
`n`n---`n## C:\Users\admin\Desktop\LoongTutorAI\dev\service-all\agent\src\main\java\com\example\agent\provider\openai\parser\OpenAiEmbeddingResponseParser.java`n```
package com.example.agent.provider.openai.parser;

import java.util.Comparator;
import java.util.List;

import com.example.agent.core.rag.embedding.Embedding;
import com.example.agent.provider.openai.dto.response.OpenAiEmbeddingData;
import com.example.agent.provider.openai.dto.response.OpenAiEmbeddingResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class OpenAiEmbeddingResponseParser {
    private final ObjectMapper objectMapper;

    public OpenAiEmbeddingResponseParser(ObjectMapper objectMapper) {

        if (objectMapper == null) {
            throw new IllegalArgumentException("objectMapper 不能为空");
        }

        this.objectMapper = objectMapper;
    }

    public List<Embedding> parse(String responseBody) {

        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalArgumentException("responseBody 不能为空");
        }

        try {
            OpenAiEmbeddingResponse response = objectMapper.readValue(responseBody, OpenAiEmbeddingResponse.class);

            if (response.data() == null || response.data().isEmpty()) {
                throw new IllegalStateException("Embedding 响应中没有 data");
            }

            return response.data()
                    .stream()
                    .sorted(Comparator.comparingInt(
                            embeddingData -> embeddingData.index()))
                    .map(this::toEmbedding)
                    .toList();

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("解析 Embedding 响应失败");
        }

    }

    private Embedding toEmbedding(OpenAiEmbeddingData data) {
        if (data.embedding() == null || data.embedding().isEmpty()) {
            throw new IllegalStateException(
                    "Embedding vector 不能为空");
        }

        float[] vector = new float[data.embedding().size()];

        for (int i = 0; i < vector.length; i++) {
            Float value = data.embedding().get(i);
            if (value == null) {
                throw new IllegalStateException(
                        "Embedding vector 不能包含 null");
            }
            vector[i] = value;
        }
        return new Embedding(vector);
    }
}

```
