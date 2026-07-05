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
