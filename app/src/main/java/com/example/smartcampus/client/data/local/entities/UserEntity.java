package com.example.smartcampus.client.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ✅ ВИПРАВЛЕНО: Додано @JsonIgnoreProperties для ігнорування невідомих полів
 */
@Entity(
        tableName = "users",
        indices = {@Index(value = "email", unique = true)}
)
@JsonIgnoreProperties(ignoreUnknown = true)  // ✅ КРИТИЧНО: Ігноруємо passwordHash з API
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
