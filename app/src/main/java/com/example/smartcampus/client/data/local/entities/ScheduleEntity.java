package com.example.smartcampus.client.data.local.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 🆕 Entity для розкладу (тільки для читання з API)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleEntity {

    @JsonProperty("id")
    public long id;

    @JsonProperty("roomId")
    public long roomId;

    @JsonProperty("userId")
    public long userId;

    @JsonProperty("startTime")
    public String startTime;  // ISO 8601 format

    @JsonProperty("endTime")
    public String endTime;

    @JsonProperty("subject")
    public String subject;

    @JsonProperty("groupName")
    public String groupName;

    public ScheduleEntity() {}

    /**
     * Чи пара зараз йде?
     */
    public boolean isCurrentlyActive() {
        // TODO: Порівняти з поточним часом
        return false;
    }

    /**
     * Форматований час "10:00 - 11:30"
     */
    public String getFormattedTime() {
        if (startTime != null && endTime != null) {
            String start = formatTime(startTime);
            String end = formatTime(endTime);
            return start + " - " + end;
        }
        return "";
    }

    private String formatTime(String isoTime) {
        try {
            // "2025-12-03T10:00:00" -> "10:00"
            return isoTime.substring(11, 16);
        } catch (Exception e) {
            return isoTime;
        }
    }
}