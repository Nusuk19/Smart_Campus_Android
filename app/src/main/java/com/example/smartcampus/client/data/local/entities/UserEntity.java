package com.example.smartcampus.client.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Локальне збереження даних користувача
 */
@Entity(
        tableName = "users",
        indices = {@Index(value = "email", unique = true)}
)
public class UserEntity {

    @PrimaryKey
    @JsonProperty("id")
    public long id;

    @JsonProperty("email")
    public String email;

    @JsonProperty("name")
    public String name;

    @JsonProperty("role")
    public String role; // STUDENT, PROFESSOR, ADMIN

    @JsonProperty("photoUrl")
    public String photoUrl; // URL фото (з Google)

    public long lastSyncedAt;

    public UserEntity() {
        this.lastSyncedAt = System.currentTimeMillis();
    }
}