package com.example.smartcampus.client.data.local.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ✅ ВИПРАВЛЕНО: Додано @JsonIgnoreProperties + підтримка нових полів
 */
@Entity(
        tableName = "rooms",
        indices = {
                @Index(value = "available"),
                @Index(value = "nfc_tag_id", unique = true)
        }
)
@JsonIgnoreProperties(ignoreUnknown = true)  // ✅ КРИТИЧНО: Ігноруємо lockId, occupiedBy, etc.
public class RoomEntity {

    @PrimaryKey
    @JsonProperty("id")
    public long id;

    @ColumnInfo(name = "name")
    @JsonProperty("name")
    public String name;

    @ColumnInfo(name = "available")
    @JsonProperty("available")
    public boolean available;

    @ColumnInfo(name = "floor")
    @JsonProperty("floor")
    public int floor;

    @ColumnInfo(name = "building")
    @JsonProperty("building")
    public String building;

    @ColumnInfo(name = "capacity")
    @JsonProperty("capacity")
    public int capacity;

    @ColumnInfo(name = "nfc_tag_id")
    @JsonProperty("nfcTagId")
    public String nfcTagId;

    @ColumnInfo(name = "room_type")
    @JsonProperty("roomType")
    public String roomType;

    @ColumnInfo(name = "last_updated")
    public long lastUpdated;

    // ✅ НОВИЙ: Додано поля з API (але НЕ зберігаємо в БД)
    @Ignore
    @JsonProperty("nfcReaderId")
    public String nfcReaderId;

    @Ignore
    @JsonProperty("lockId")
    public String lockId;

    @Ignore
    @JsonProperty("occupiedBy")
    public Long occupiedBy;

    /**
     * ✅ Порожній конструктор (для Room та Jackson)
     */
    public RoomEntity() {
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * ✅ Повний конструктор з @Ignore
     */
    @Ignore
    public RoomEntity(long id, String name, boolean available, int floor,
                      String building, int capacity, String nfcTagId, String roomType) {
        this.id = id;
        this.name = name;
        this.available = available;
        this.floor = floor;
        this.building = building;
        this.capacity = capacity;
        this.nfcTagId = nfcTagId;
        this.roomType = roomType;
        this.lastUpdated = System.currentTimeMillis();
    }

    // ========== UTILITY МЕТОДИ ==========

    /**
     * Чи дані застарілі (>5 хвилин)
     */
    public boolean isStale() {
        long fiveMinutes = 5 * 60 * 1000;
        return System.currentTimeMillis() - lastUpdated > fiveMinutes;
    }

    /**
     * Оновити timestamp
     */
    public void markAsUpdated() {
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Повна назва: "Корпус 5, ауд. 305"
     */
    public String getFullName() {
        if (building != null && !building.isEmpty()) {
            return building + ", ауд. " + name;
        }
        return "ауд. " + name;
    }

    /**
     * Текст статусу для UI
     */
    public String getStatusText() {
        return available ? "Вільна" : "Зайнята";
    }

    /**
     * Опис для UI: "3 поверх • 30 місць"
     */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        if (floor > 0) {
            sb.append(floor).append(" поверх");
        }
        if (capacity > 0) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(capacity).append(" місць");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "RoomEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", available=" + available +
                ", building='" + building + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomEntity that = (RoomEntity) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
