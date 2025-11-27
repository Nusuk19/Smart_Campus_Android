package com.example.smartcampus.client.data.repository;

import android.app.Application;
import androidx.annotation.NonNull;
import com.example.smartcampus.client.data.local.AppDatabase;
import com.example.smartcampus.client.data.local.dao.UserDao;
import com.example.smartcampus.client.data.local.entities.UserEntity;
import com.example.smartcampus.client.data.remote.RetrofitClient;
import com.example.smartcampus.client.data.remote.api.*;
import com.example.smartcampus.client.utils.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository для автентифікації
 */
public class AuthRepository {

    private final UserDao userDao;
    private final AuthApi api;
    private final SessionManager sessionManager;
    private final ExecutorService executor;

    public AuthRepository(Application app) {
        AppDatabase db = AppDatabase.getInstance(app);
        this.userDao = db.userDao();
        this.api = RetrofitClient.get().create(AuthApi.class);
        this.sessionManager = new SessionManager(app);
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Реєстрація
     */
    public void register(String email, String password, String name,
                         AuthCallback callback) {
        RegisterRequest request = new RegisterRequest(email, password, name);

        api.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call,
                                   @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleAuthSuccess(response.body(), callback);
                } else {
                    callback.onError("Помилка реєстрації: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                callback.onError("Помилка з'єднання: " + t.getMessage());
            }
        });
    }

    /**
     * Вхід (Email + Password)
     */
    public void login(String email, String password, AuthCallback callback) {
        LoginRequest request = new LoginRequest(email, password);

        api.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call,
                                   @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleAuthSuccess(response.body(), callback);
                } else {
                    callback.onError("Невірний email або пароль");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                callback.onError("Помилка з'єднання: " + t.getMessage());
            }
        });
    }

    /**
     * Вхід через Google SSO
     */
    public void loginWithGoogle(String googleIdToken, AuthCallback callback) {
        GoogleTokenRequest request = new GoogleTokenRequest(googleIdToken);

        api.loginWithGoogle(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call,
                                   @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleAuthSuccess(response.body(), callback);
                } else {
                    callback.onError("Помилка Google авторизації");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                callback.onError("Помилка з'єднання: " + t.getMessage());
            }
        });
    }

    /**
     * Вийти
     */
    public void logout() {
        executor.execute(() -> {
            sessionManager.clearSession();
            userDao.deleteAll();
        });
    }

    /**
     * Чи користувач залогінений?
     */
    public boolean isLoggedIn() {
        return sessionManager.getAccessToken() != null;
    }

    /**
     * Обробка успішної автентифікації
     */
    private void handleAuthSuccess(AuthResponse response, AuthCallback callback) {
        // Зберегти JWT токени
        sessionManager.saveTokens(
                response.accessToken,
                response.refreshToken
        );

        // Зберегти користувача локально
        executor.execute(() -> {
            UserEntity user = new UserEntity();
            user.id = response.user.id;
            user.email = response.user.email;
            user.name = response.user.name;
            user.role = response.user.role;
            user.photoUrl = response.user.photoUrl;
            userDao.insert(user);
        });

        callback.onSuccess(response.user);
    }

    // Callback інтерфейс
    public interface AuthCallback {
        void onSuccess(AuthResponse.UserData user);
        void onError(String error);
    }
}