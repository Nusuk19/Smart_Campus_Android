package com.example.smartcampus.client.data.repository;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import com.example.smartcampus.client.data.local.AppDatabase;
import com.example.smartcampus.client.data.local.dao.UserDao;
import com.example.smartcampus.client.data.local.entities.UserEntity;
import com.example.smartcampus.client.data.remote.RetrofitClient;
import com.example.smartcampus.client.data.remote.api.ChangePasswordRequest;
import com.example.smartcampus.client.data.remote.api.UpdateProfileRequest;
import com.example.smartcampus.client.data.remote.api.UserApi;
import com.example.smartcampus.client.utils.SessionManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private static final String TAG = "UserRepository";
    private final UserDao userDao;
    private final UserApi api;
    private final SessionManager sessionManager;
    private final ExecutorService executor;

    public UserRepository(Application app) {
        AppDatabase db = Room.databaseBuilder(app, AppDatabase.class, "smartcampus-db")
                .fallbackToDestructiveMigration()
                .build();
        this.userDao = db.userDao();
        this.api = RetrofitClient.get().create(UserApi.class);
        this.sessionManager = new SessionManager(app);
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Отримати профіль поточного користувача
     */
    public LiveData<UserEntity> getCurrentUser() {
        long userId = sessionManager.getUserId();
        return userDao.getUserById(userId);
    }

    /**
     * ✅ НОВИЙ: Оновити профіль з API
     */
    public void refreshProfile(UserCallback callback) {
        api.getMyProfile().enqueue(new Callback<UserEntity>() {
            @Override
            public void onResponse(@NonNull Call<UserEntity> call,
                                   @NonNull Response<UserEntity> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserEntity user = response.body();
                    Log.d(TAG, "✅ Loaded user profile: " + user.email);

                    executor.execute(() -> userDao.insert(user));
                    callback.onSuccess(user);
                } else {
                    Log.w(TAG, "⚠️ Failed to load profile: " + response.code());
                    callback.onError("Failed to load profile");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserEntity> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Failed to load profile", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Оновити профіль
     */
    public void updateProfile(String name, UserCallback callback) {
        Log.d(TAG, "✏️ Updating profile: " + name);

        UpdateProfileRequest request = new UpdateProfileRequest(name);

        api.updateProfile(request).enqueue(new Callback<UserEntity>() {
            @Override
            public void onResponse(@NonNull Call<UserEntity> call,
                                   @NonNull Response<UserEntity> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserEntity user = response.body();
                    Log.d(TAG, "✅ Profile updated");

                    executor.execute(() -> userDao.update(user));
                    callback.onSuccess(user);
                } else {
                    Log.w(TAG, "⚠️ Failed to update profile: " + response.code());
                    callback.onError("Помилка оновлення профілю");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserEntity> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Failed to update profile", t);
                callback.onError("Помилка з'єднання: " + t.getMessage());
            }
        });
    }

    /**
     * Змінити пароль
     */
    public void changePassword(String oldPassword, String newPassword, UserCallback callback) {
        Log.d(TAG, "🔐 Changing password");

        ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, newPassword);

        api.changePassword(request).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<okhttp3.ResponseBody> call,
                                   @NonNull Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Password changed");
                    callback.onSuccess(null);
                } else {
                    Log.w(TAG, "⚠️ Failed to change password: " + response.code());
                    callback.onError("Невірний старий пароль");
                }
            }

            @Override
            public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Failed to change password", t);
                callback.onError("Помилка з'єднання: " + t.getMessage());
            }
        });
    }

    public interface UserCallback {
        void onSuccess(UserEntity user);
        void onError(String error);
    }
}