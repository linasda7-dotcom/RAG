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
