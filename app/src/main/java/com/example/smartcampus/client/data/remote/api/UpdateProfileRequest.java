package com.example.smartcampus.client.data.remote.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request для оновлення профілю користувача
 */
public class UpdateProfileRequest {
    @JsonProperty("name")
    public String name;     // Нове ім'я

    @JsonProperty("photoUrl")
    public String photoUrl; // URL фото (опціонально)

    public UpdateProfileRequest(String name) {
        this.name = name;
    }

    public UpdateProfileRequest(String name, String photoUrl) {
        this.name = name;
        this.photoUrl = photoUrl;
    }
}