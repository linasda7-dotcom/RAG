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
