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
