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
