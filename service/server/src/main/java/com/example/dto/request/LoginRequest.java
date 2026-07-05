package com.example.dto.request;

public record LoginRequest(
        String username,
        String password) {
    public LoginRequest {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
    }
}
