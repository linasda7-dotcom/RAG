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
