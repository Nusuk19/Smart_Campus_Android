package com.example.smartcampus.client.data.remote.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response на успішну автентифікацію
 */
public class AuthResponse {
    @JsonProperty("accessToken")
    public String accessToken;   // JWT токен (термін дії: 1 година)

    @JsonProperty("refreshToken")
    public String refreshToken;  // Токен для оновлення (термін дії: 30 днів)

    @JsonProperty("user")
    public UserData user;        // Дані користувача

    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, UserData user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    /**
     * Дані користувача в response
     */
    public static class UserData {
        @JsonProperty("id")
        public long id;

        @JsonProperty("email")
        public String email;

        @JsonProperty("name")
        public String name;

        @JsonProperty("role")
        public String role;        // STUDENT, PROFESSOR, ADMIN

        @JsonProperty("photoUrl")
        public String photoUrl;    // URL фото з Google (nullable)

        public UserData() {}

        public UserData(long id, String email, String name, String role, String photoUrl) {
            this.id = id;
            this.email = email;
            this.name = name;
            this.role = role;
            this.photoUrl = photoUrl;
        }
    }
}
