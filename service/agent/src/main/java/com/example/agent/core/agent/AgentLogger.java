package com.example.agent.core.agent;

/**
 * Agent 运行日志
 */
public class AgentLogger {
    private final boolean enabled;

    public AgentLogger(boolean enabled) {
        this.enabled = enabled;
    }

    public void info(String message) {
        if (!enabled) {
            return;
        }
        System.out.println("[Agent] " + message);
    }

    public void step(int step, String message) {
        if (!enabled) {
            return;
        }
        System.out.println("[Agent Step " + step + "] " + message);
    }

    public void tool(String toolName, String message) {
        if (!enabled) {
            return;
        }
        System.out.println("[Tool:" + toolName + "] " + message);
    }

    public void error(String message, Throwable e) {
        if (!enabled) {
            return;
        }
        System.out.println("[Agent Error] " + message);
        if (e != null) {
            System.out.println("[Agent Error] "
                    + e.getClass().getSimpleName()
                    + ":"
                    + e.getMessage());
        }
    }

}
