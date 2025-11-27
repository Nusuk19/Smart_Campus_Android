package com.example.smartcampus.client.data.remote.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request для перейменування тегу
 */
public class RenameTagRequest {
    @JsonProperty("name")
    public String name;

    public RenameTagRequest(String name) {
        this.name = name;
    }
}