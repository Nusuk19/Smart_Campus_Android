package com.example.smartcampus.client.data.remote.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface AuthApi {

    /**
     * Реєстрація нового користувача
     */
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    /**
     * Вхід (Email + Password)
     */
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    /**
     * Вхід через Google SSO
     */
    @POST("auth/google")
    Call<AuthResponse> loginWithGoogle(@Body GoogleTokenRequest request);

    /**
     * Оновити JWT токен
     */
    @POST("auth/refresh")
    Call<AuthResponse> refreshToken(@Body RefreshTokenRequest request);

    /**
     * Вийти
     */
    @POST("auth/logout")
    Call<ResponseBody> logout();
}

