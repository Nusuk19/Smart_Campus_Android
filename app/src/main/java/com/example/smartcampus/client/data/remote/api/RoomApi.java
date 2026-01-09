package com.example.smartcampus.client.data.remote.api;

import com.example.smartcampus.client.data.local.entities.RoomEntity;
import com.example.smartcampus.client.data.local.entities.ScheduleEntity;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * ✅ ОНОВЛЕНО: API з endpoints для розкладу
 */
public interface RoomApi {

    @GET("rooms")
    Call<List<RoomEntity>> getRooms();

    /**
     * 🆕 Отримати розклад аудиторії
     */
    @GET("rooms/{id}/schedule")
    Call<List<ScheduleEntity>> getRoomSchedule(@Path("id") long roomId);

    /**
     * 🆕 Отримати розклад на сьогодні
     */
    @GET("rooms/{id}/schedule/today")
    Call<List<ScheduleEntity>> getRoomScheduleToday(@Path("id") long roomId);

    /**
     * 🆕 Отримати поточну пару
     */
    @GET("rooms/{id}/schedule/current")
    Call<ScheduleEntity> getCurrentClass(@Path("id") long roomId);

    /**
     * 🆕 Отримати наступну пару
     */
    @GET("rooms/{id}/schedule/next")
    Call<ScheduleEntity> getNextClass(@Path("id") long roomId);
}
