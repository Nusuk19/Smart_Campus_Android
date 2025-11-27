package com.example.smartcampus.client.data.remote.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * ✅ ЗМІНЕНО: POST /doors/unlock замість /rooms/open/{nfcTagId}
 *
 * НОВА ЛОГІКА:
 * - Відправляємо tagUid (з фізичного тегу або віртуального)
 * - Відправляємо readerId (ID рідера на дверях аудиторії)
 * - Backend сам визначить чи користувач має право
 */
public interface DoorApi {

    /**
     * Відкрити двері через NFC тег
     *
     * @param request {tagUid: "04:5E:2A:B2", readerId: "R305"}
     */
    @POST("doors/unlock")
    Call<UnlockResponse> unlockDoor(@Body UnlockRequest request);
}