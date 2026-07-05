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
