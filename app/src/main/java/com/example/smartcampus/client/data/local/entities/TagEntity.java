package com.example.smartcampus.client.data.local.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * ✅ ВИПРАВЛЕНО: NFC тег з правильною десеріалізацією дат
 *
 * ЗМІНИ:
 * - createdAt та lastUsedAt тепер long (timestamp в мілісекундах)
 * - Додано @JsonIgnoreProperties для безпеки
 * - Додано null-перевірки у конструкторі
 */
@Entity(
        tableName = "tags",
        indices = {@Index(value = "tag_uid", unique = true)}
)
@JsonIgnoreProperties(ignoreUnknown = true)  // ✅ Ігноруємо невідомі поля з API
public class TagEntity {

    @PrimaryKey(autoGenerate = true)
    @JsonProperty("id")
    public long id;

    @ColumnInfo(name = "tag_uid")
    @JsonProperty("tagUid")
    public String tagUid;

    @ColumnInfo(name = "user_id")
    @JsonProperty("userId")
    public long userId;

    @JsonProperty("tagType")
    public String tagType; // PHYSICAL, VIRTUAL

    @JsonProperty("name")
    public String name;

    @ColumnInfo(name = "is_active")
    @JsonProperty("isActive")
    public boolean isActive;

    /**
     * ✅ ВИПРАВЛЕНО: Використовуємо long замість LocalDateTime
     * Тепер Jackson правильно десеріалізує timestamp з API
     */
    @ColumnInfo(name = "created_at")
    @JsonProperty("createdAt")
    public long createdAt;  // ✅ long (мілісекунди з 1970)

    @ColumnInfo(name = "last_used_at")
    @JsonProperty("lastUsedAt")
    public long lastUsedAt;  // ✅ long (мілісекунди з 1970)

    /**
     * ✅ Порожній конструктор (для Room та Jackson)
     */
    public TagEntity() {
        this.createdAt = System.currentTimeMillis();
        this.lastUsedAt = 0;
    }

    /**
     * ✅ Конструктор для створення тегу з безпечними значеннями
     */
    @Ignore
    public TagEntity(String tagUid, long userId, String tagType, String name) {
        this.tagUid = tagUid != null ? tagUid : "";
        this.userId = userId;
        this.tagType = tagType != null ? tagType : "VIRTUAL";
        this.name = name != null ? name : "Невідомий тег";
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.lastUsedAt = 0;
    }

    /**
     * Статичний метод для створення віртуального тегу
     */
    public static TagEntity createVirtualTag(long userId, String name) {
        return new TagEntity(
                com.example.smartcampus.client.utils.VirtualTagGenerator.generate(),
                userId,
                "VIRTUAL",
                name != null ? name : "Віртуальний тег"
        );
    }

    // Utility методи
    public boolean isVirtual() {
        return "VIRTUAL".equals(tagType);
    }

    public boolean isPhysical() {
        return "PHYSICAL".equals(tagType);
    }

    public String getSafeTagType() {
        return tagType != null ? tagType : "VIRTUAL";
    }

    public String getSafeTagUid() {
        return tagUid != null ? tagUid : "";
    }

    public String getSafeName() {
        return name != null ? name : "Тег #" + id;
    }

    /**
     * ✅ НОВИЙ: Форматувати дату для відображення
     */
    public String getFormattedCreatedAt() {
        if (createdAt == 0) return "Невідомо";
        return android.text.format.DateFormat.format("dd.MM.yyyy HH:mm", createdAt).toString();
    }

    public String getFormattedLastUsedAt() {
        if (lastUsedAt == 0) return "Ніколи";
        return android.text.format.DateFormat.format("dd.MM.yyyy HH:mm", lastUsedAt).toString();
    }
}