package com.example.agent.core.agent;

/**
 * 保存 Agent 运行时配置。
 */
public class AgentOptions {

    private final int maxAgentSteps;
    private final double temperature;
    private final boolean logEnabled;
    private final boolean failFastOnToolError;
    private final Integer maxTokens;
    private final Boolean enableThinking;

    private AgentOptions(Builder builder) {
        this.maxAgentSteps = builder.maxAgentSteps;
        this.temperature = builder.temperature;
        this.logEnabled = builder.logEnabled;
        this.failFastOnToolError = builder.failFastOnToolError;
        this.maxTokens = builder.maxTokens;
        this.enableThinking = builder.enableThinking;
    }

    public int maxAgentSteps() {
        return maxAgentSteps;
    }

    public double temperature() {
        return temperature;
    }

    public boolean logEnabled() {
        return logEnabled;
    }

    public boolean failFastOnToolError() {
        return failFastOnToolError;
    }

    public Integer maxTokens() {
        return maxTokens;
    }

    public Boolean enableThinking() {
        return enableThinking;
    }

    public static AgentOptions defaultOptions() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return builder()
                .temperature(temperature)
                .logEnabled(logEnabled)
                .enableThinking(enableThinking)
                .maxTokens(maxTokens)
                .failFastOnToolError(failFastOnToolError)
                .maxAgentSteps(maxAgentSteps);
    }

    public static class Builder {
        private int maxAgentSteps = 5;
        private double temperature = 0.7;
        private boolean logEnabled = true;
        private boolean failFastOnToolError = true;
        private Integer maxTokens = 1024;
        private Boolean enableThinking = true;

        public Builder maxAgentSteps(int maxAgentSteps) {
            if (maxAgentSteps <= 0) {
                throw new IllegalArgumentException("maxAgentSteps 必须大于 0");
            }
            this.maxAgentSteps = maxAgentSteps;
            return this;
        }

        public Builder temperature(double temperature) {
            if (temperature < 0 || temperature > 2) {
                throw new IllegalArgumentException("temperature 必须在 0 到 2 之间");
            }
            this.temperature = temperature;
            return this;
        }

        public Builder logEnabled(boolean logEnabled) {
            this.logEnabled = logEnabled;
            return this;
        }

        public Builder failFastOnToolError(boolean failFastOnToolError) {
            this.failFastOnToolError = failFastOnToolError;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            if (maxTokens != null && maxTokens <= 0) {
                throw new IllegalArgumentException("maxTokens 必须大于0");
            }
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder enableThinking(Boolean enableThinking) {
            this.enableThinking = enableThinking;
            return this;
        }

        public AgentOptions build() {
            return new AgentOptions(this);
        }
    }
}
