package com.example.smartcampus.client.data.remote.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response на запит відкриття дверей
 */
public class UnlockResponse {
    @JsonProperty("status")
    public String status;      // OK, DENIED, OCCUPIED, ERROR

    @JsonProperty("roomName")
    public String roomName;    // Назва аудиторії

    @JsonProperty("message")
    public String message;     // Додаткове повідомлення

    @JsonProperty("accessLogId")
    public long accessLogId;   // ID запису в журналі доступу

    public UnlockResponse() {}

    public UnlockResponse(String status, String roomName, String message) {
        this.status = status;
        this.roomName = roomName;
        this.message = message;
    }
}