package com.example.smartcampus.client.data.repository;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.room.Room;
import com.example.smartcampus.client.data.local.AppDatabase;
import com.example.smartcampus.client.data.local.dao.TagDao;
import com.example.smartcampus.client.data.local.entities.TagEntity;
import com.example.smartcampus.client.data.remote.RetrofitClient;
import com.example.smartcampus.client.data.remote.api.*;
import com.example.smartcampus.client.utils.SessionManager;
import com.example.smartcampus.client.utils.VirtualTagGenerator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 🆕 Repository для управління NFC тегами
 */
public class TagRepository {

    private static final String TAG = "TagRepository";
    private final TagDao tagDao;
    private final TagApi api;
    private final SessionManager sessionManager;
    private final ExecutorService executor;

    public TagRepository(Application app) {
        AppDatabase db = Room.databaseBuilder(app, AppDatabase.class, "smartcampus-db")
                .fallbackToDestructiveMigration()
                .build();
        this.tagDao = db.tagDao();
        this.api = RetrofitClient.get().create(TagApi.class);
        this.sessionManager = new SessionManager(app);
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Отримати теги поточного користувача
     */
    public LiveData<List<TagEntity>> getMyTags() {
        long userId = sessionManager.getUserId();

        MediatorLiveData<List<TagEntity>> result = new MediatorLiveData<>();

        // Спочатку показуємо кеш
        LiveData<List<TagEntity>> cachedData = tagDao.getTagsByUser(userId);
        result.addSource(cachedData, result::setValue);

        // Потім оновлюємо з API
        refreshFromApi();

        return result;
    }

    /**
     * Оновити з API
     */
    private void refreshFromApi() {
        api.getMyTags().enqueue(new Callback<List<TagEntity>>() {
            @Override
            public void onResponse(@NonNull Call<List<TagEntity>> call,
                                   @NonNull Response<List<TagEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TagEntity> tags = response.body();
                    Log.d(TAG, "✅ Loaded " + tags.size() + " tags from API");

                    executor.execute(() -> {
                        long userId = sessionManager.getUserId();
                        // Видалити старі теги цього користувача
                        tagDao.deleteByUser(userId);
                        // Вставити нові
                        for (TagEntity tag : tags) {
                            tagDao.insert(tag);
                        }
                    });
                } else {
                    Log.w(TAG, "⚠️ Failed to load tags: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<TagEntity>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Failed to load tags", t);
            }
        });
    }

    /**
     * Створити віртуальний тег
     */
    public void createVirtualTag(String name, TagCallback callback) {
        // Генерувати унікальний UID
        String virtualUid = VirtualTagGenerator.generateForCurrentUser(sessionManager);

        Log.d(TAG, "🏷️ Creating virtual tag: " + virtualUid);

        CreateVirtualTagRequest request = new CreateVirtualTagRequest(virtualUid, name);

        api.createVirtualTag(request).enqueue(new Callback<TagEntity>() {
            @Override
            public void onResponse(@NonNull Call<TagEntity> call,
                                   @NonNull Response<TagEntity> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TagEntity tag = response.body();
                    Log.d(TAG, "✅ Tag created: " + tag.id);

                    // Зберегти локально
                    executor.execute(() -> tagDao.insert(tag));

                    callback.onSuccess(tag);
                } else {
                    Log.w(TAG, "⚠️ Failed to create tag: " + response.code());
                    callback.onError("Помилка створення тегу: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TagEntity> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Failed to create tag", t);
                callback.onError("Помилка з'єднання: " + t.getMessage());
            }
        });
    }

    /**
     * Видалити тег
     */
    public void deleteTag(long tagId, TagCallback callback) {
        Log.d(TAG, "🗑️ Deleting tag: " + tagId);

        api.deleteTag(tagId).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<okhttp3.ResponseBody> call,
                                   @NonNull Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Tag deleted");

                    executor.execute(() -> {
                        TagEntity tag = new TagEntity();
                        tag.id = tagId;
                        tagDao.delete(tag);
                    });

                    callback.onSuccess(null);
                } else {
                    Log.w(TAG, "⚠️ Failed to delete tag: " + response.code());
                    callback.onError("Помилка видалення: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Failed to delete tag", t);
                callback.onError("Помилка з'єднання: " + t.getMessage());
            }
        });
    }

    /**
     * Перейменувати тег
     */
    public void renameTag(long tagId, String newName, TagCallback callback) {
        Log.d(TAG, "✏️ Renaming tag " + tagId + " to: " + newName);

        RenameTagRequest request = new RenameTagRequest(newName);

        api.renameTag(tagId, request).enqueue(new Callback<TagEntity>() {
            @Override
            public void onResponse(@NonNull Call<TagEntity> call,
                                   @NonNull Response<TagEntity> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TagEntity updatedTag = response.body();
                    Log.d(TAG, "✅ Tag renamed");

                    executor.execute(() -> tagDao.update(updatedTag));

                    callback.onSuccess(updatedTag);
                } else {
                    Log.w(TAG, "⚠️ Failed to rename tag: " + response.code());
                    callback.onError("Помилка перейменування: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TagEntity> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Failed to rename tag", t);
                callback.onError("Помилка з'єднання: " + t.getMessage());
            }
        });
    }

    /**
     * Оновити час останнього використання
     */
    public void updateLastUsed(long tagId) {
        executor.execute(() -> {
            tagDao.updateLastUsed(tagId, System.currentTimeMillis());
            Log.d(TAG, "✅ Updated lastUsedAt for tag: " + tagId);
        });
    }

    // Callback інтерфейс
    public interface TagCallback {
        void onSuccess(TagEntity tag);
        void onError(String error);
    }
}