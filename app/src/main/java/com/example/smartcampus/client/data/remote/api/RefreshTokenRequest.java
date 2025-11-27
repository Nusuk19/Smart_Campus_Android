package com.example.smartcampus.client.data.remote.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request для оновлення Access Token
 */
public class RefreshTokenRequest {
    @JsonProperty("refreshToken")
    public String refreshToken;

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}