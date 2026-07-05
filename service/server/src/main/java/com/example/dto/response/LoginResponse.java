package com.example.dto.response;

public record LoginResponse(
        String token,
        Long userId,
        String username,
        String nickname,
        String role) {
}
