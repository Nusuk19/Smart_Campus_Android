package com.example.smartcampus.client.data.remote.api;

import com.example.smartcampus.client.data.local.entities.TagEntity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface TagApi {

    /**
     * Отримати всі теги поточного користувача
     */
    @GET("tags/my")
    Call<List<TagEntity>> getMyTags();

    /**
     * Створити віртуальний тег (генерується в телефоні)
     */
    @POST("tags/virtual")
    Call<TagEntity> createVirtualTag(@Body CreateVirtualTagRequest request);

    /**
     * Деактивувати тег
     */
    @DELETE("tags/{id}")
    Call<ResponseBody> deleteTag(@Path("id") long tagId);

    /**
     * Перейменувати тег
     */
    @PATCH("tags/{id}")
    Call<TagEntity> renameTag(@Path("id") long tagId, @Body RenameTagRequest request);
}