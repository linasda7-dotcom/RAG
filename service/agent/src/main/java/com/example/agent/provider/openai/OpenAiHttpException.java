package com.example.agent.provider.openai;

public class OpenAiHttpException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public OpenAiHttpException(int statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int statusCode() {
        return statusCode;
    }

    public String responseBody() {
        return responseBody;
    }

}
