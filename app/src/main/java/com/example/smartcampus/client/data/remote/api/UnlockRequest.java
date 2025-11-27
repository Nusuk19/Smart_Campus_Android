package com.example.smartcampus.client.data.remote.api;

/**
 * Request для відкриття дверей
 */
public class UnlockRequest {
    public String tagUid;    // ID тегу користувача (фізичний або віртуальний)
    public String readerId;  // ID рідера на дверях (опціонально)

    public UnlockRequest(String tagUid, String readerId) {
        this.tagUid = tagUid;
        this.readerId = readerId;
    }
}