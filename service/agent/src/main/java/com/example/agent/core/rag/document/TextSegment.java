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