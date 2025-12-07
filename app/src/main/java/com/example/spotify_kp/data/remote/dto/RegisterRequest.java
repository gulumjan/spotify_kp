package com.example.spotify_kp.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("display_name")
    private String displayName;

    private String email;
    private String password;

    public RegisterRequest(String displayName, String email, String password) {
        this.displayName = displayName;
        this.email = email;
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}