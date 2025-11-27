package com.example.smartcampus.client.data.remote.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request для створення віртуального тегу
 */
public class CreateVirtualTagRequest {
    @JsonProperty("tagUid")
    public String tagUid;  // Згенерований UID (наприклад: "VT-04:5E:2A:B2")

    @JsonProperty("name")
    public String name;    // Назва тегу (наприклад: "Мій Samsung S21")

    public CreateVirtualTagRequest(String tagUid, String name) {
        this.tagUid = tagUid;
        this.name = name;
    }
}