package com.example.smartcampus.client.data.remote.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request для зміни пароля
 */
public class ChangePasswordRequest {
    @JsonProperty("oldPassword")
    public String oldPassword;

    @JsonProperty("newPassword")
    public String newPassword;

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}