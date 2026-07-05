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
