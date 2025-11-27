package com.example.smartcampus.client.data.remote.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request для входу через Google SSO
 */
public class GoogleTokenRequest {
    @JsonProperty("idToken")
    public String idToken;  // Google ID Token отриманий з GoogleSignInAccount

    public GoogleTokenRequest(String idToken) {
        this.idToken = idToken;
    }
}