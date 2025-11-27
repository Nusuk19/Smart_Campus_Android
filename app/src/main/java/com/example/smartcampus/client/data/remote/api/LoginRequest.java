package com.example.smartcampus.client.data.remote.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request для входу через Email + Password
 */
public class LoginRequest {
    @JsonProperty("email")
    public String email;

    @JsonProperty("password")
    public String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}