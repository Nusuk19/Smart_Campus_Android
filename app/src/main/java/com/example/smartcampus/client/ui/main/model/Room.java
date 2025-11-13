package com.example.smartcampus.client.ui.main.model;

/**
 * UI Model для відображення аудиторії в RecyclerView
 *
 * ВІДМІННІСТЬ ВІД RoomEntity:
 * - RoomEntity: технічна модель для БД (з timestamps, indexes)
 * - Room: спрощена модель для UI (тільки те що потрібно показати)
 *
 * ОРИГІНАЛЬНА ЛОГІКА:
 * - Тільки поле name
 *
 * ПОКРАЩЕННЯ:
 * ✅ Додано ID (для кліків)
 * ✅ Додано статус (вільна/зайнята)
 * ✅ Додано поверх, корпус, вмістимість
 * ✅ Додано форматовані рядки для UI
 */
public class Room {

    // ========== ОРИГІНАЛЬНІ ПОЛЯ ==========

    private final String name;

    // ========== ДОДАНІ ПОЛЯ ==========

    private final long id;
    private final boolean available;
    private final int floor;
    private final String building;
    private final int capacity;
    private final String fullName;      // "Корпус A, ауд. 305"
    private final String statusText;    // "Вільна" / "Зайнята"

    /**
     * ОРИГІНАЛЬНИЙ КОНСТРУКТОР
     */
    public Room(String name) {
        this(0, name, false, 0, null, 0, name, "");
    }

    /**
     * ПОВНИЙ КОНСТРУКТОР
     */
    public Room(long id, String name, boolean available, int floor,
                String building, int capacity, String fullName, String statusText) {
        this.id = id;
        this.name = name;
        this.available = available;
        this.floor = floor;
        this.building = building;
        this.capacity = capacity;
        this.fullName = fullName;
        this.statusText = statusText;
    }

    // ========== ГЕТТЕРИ ==========

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return available;
    }

    public int getFloor() {
        return floor;
    }

    public String getBuilding() {
        return building;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getFullName() {
        return fullName;
    }

    public String getStatusText() {
        return statusText;
    }

    // ========== UTILITY МЕТОДИ ==========

    /**
     * Отримати опис для UI
     * Приклад: "3 поверх • 30 місць"
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

    /**
     * Колір статусу (для UI)
     * @return "#4CAF50" (зелений) або "#F44336" (червоний)
     */
    public String getStatusColor() {
        return available ? "#4CAF50" : "#F44336";
    }

    /**
     * Іконка статусу
     * @return "🟢" або "🔴"
     */
    public String getStatusIcon() {
        return available ? "🟢" : "🔴";
    }

    @Override
    public String toString() {
        return fullName + " (" + statusText + ")";
    }
}