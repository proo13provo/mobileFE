package com.example.femobile.model.response;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String message;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getMessage() {
        return message;
    }
}
