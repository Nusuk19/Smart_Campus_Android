package com.example.smartcampus.client.ui.main.model;

/**
 * ✅ БЕЗ ЗМІН - UI модель залишається як є
 *
 * Це НЕ Room Database!
 * Це просто клас для відображення аудиторії в RecyclerView
 */
public class Room {

    private final long id;
    private final String name;
    private final boolean available;
    private final int floor;
    private final String building;
    private final int capacity;
    private final String fullName;
    private final String statusText;

    public Room(String name) {
        this(0, name, false, 0, null, 0, name, "");
    }

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

    public long getId() { return id; }
    public String getName() { return name; }
    public boolean isAvailable() { return available; }
    public int getFloor() { return floor; }
    public String getBuilding() { return building; }
    public int getCapacity() { return capacity; }
    public String getFullName() { return fullName; }
    public String getStatusText() { return statusText; }

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

    public String getStatusColor() {
        return available ? "#4CAF50" : "#F44336";
    }

    public String getStatusIcon() {
        return available ? "🟢" : "🔴";
    }

    @Override
    public String toString() {
        return fullName + " (" + statusText + ")";
    }
}
