package com.example.smartcampus.client.data.remote.api;
import com.example.smartcampus.client.data.local.entities.RoomEntity;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
public interface RoomApi { @GET("rooms") Call<List<RoomEntity>> getRooms(); }
