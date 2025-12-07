package com.example.spotify_kp.data.remote.dto;

import com.example.spotify_kp.model.User;

public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private User user;

    public AuthResponse() {}

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }
}