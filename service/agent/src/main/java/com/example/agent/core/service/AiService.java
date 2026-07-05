package com.example.agent.core.service;

public final class AiService {

    private AiService() {
    }

    public static <T> AiServiceBuilder<T> builder(Class<T> serviceClass) {
        return new AiServiceBuilder<>(serviceClass);
    }
}
