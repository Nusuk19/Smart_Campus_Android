package com.example.smartcampus.client.data.remote.api;

import com.example.smartcampus.client.data.local.entities.UserEntity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface UserApi {

    @GET("users/me")
    Call<UserEntity> getMyProfile();

    @PUT("users/me")
    Call<UserEntity> updateProfile(@Body UpdateProfileRequest request);

    @POST("users/me/change-password")
    Call<ResponseBody> changePassword(@Body ChangePasswordRequest request);
}