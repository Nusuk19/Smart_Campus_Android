package com.example.smartcampus.client.data.local.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.smartcampus.client.utils.VirtualTagGenerator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * NFC тег прив'язаний до користувача
 *
 * КОНЦЕПЦІЯ:
 * - Один користувач → Багато тегів
 * - Тег може бути PHYSICAL (фізична картка) або VIRTUAL (в телефоні)
 */
@Entity(
        tableName = "tags",
        indices = {@Index(value = "tag_uid", unique = true)}
)
public class TagEntity {

    @PrimaryKey(autoGenerate = true)
    @JsonProperty("id")
    public long id;

    @ColumnInfo(name = "tag_uid")
    @JsonProperty("tagUid")
    public String tagUid; // Унікальний ID тегу (hex)

    @ColumnInfo(name = "user_id")
    @JsonProperty("userId")
    public long userId; // Власник тегу

    @JsonProperty("tagType")
    public String tagType; // PHYSICAL, VIRTUAL

    @JsonProperty("name")
    public String name; // "Моя картка", "Телефон Samsung"

    @ColumnInfo(name = "is_active")
    @JsonProperty("isActive")
    public boolean isActive;

    @ColumnInfo(name = "created_at")
    @JsonProperty("createdAt")
    public long createdAt;

    @ColumnInfo(name = "last_used_at")
    @JsonProperty("lastUsedAt")
    public long lastUsedAt;

    public TagEntity() {}

    /**
     * Стандартний конструктор
     */
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
     * Конструктор для віртуального тегу (створюється в додатку)
     */
    public static TagEntity createVirtualTag(long userId, String name) {
        return new TagEntity(
                VirtualTagGenerator.generate(), // гарантовано не null
                userId,
                "VIRTUAL",
                name != null ? name : "Віртуальний тег"
        );
    }

    public boolean isVirtual() {
        return "VIRTUAL".equals(tagType);
    }

    public boolean isPhysical() {
        return "PHYSICAL".equals(tagType);
    }

    // Безпечні геттери на випадок null
    public String getSafeTagType() {
        return tagType != null ? tagType : "VIRTUAL";
    }

    public String getSafeTagUid() {
        return tagUid != null ? tagUid : "";
    }

    public String getSafeName() {
        return name != null ? name : "Тег #" + id;
    }
}
