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
