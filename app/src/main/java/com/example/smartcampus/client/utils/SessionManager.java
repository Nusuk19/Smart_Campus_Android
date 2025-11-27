package com.example.smartcampus.client.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 🆕 НОВИЙ: Менеджер сесії користувача
 *
 * Функції:
 * - Зберігання JWT токенів
 * - Перевірка авторизації
 * - Очищення сесії при logout
 */
public class SessionManager {

    private static final String PREF_NAME = "SmartCampusSession";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ROLE = "user_role";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Зберегти JWT токени після успішного логіну
     */
    public void saveTokens(String accessToken, String refreshToken) {
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    /**
     * Зберегти дані користувача
     */
    public void saveUser(long userId, String email, String name, String role) {
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }

    /**
     * Отримати Access Token (для Authorization header)
     */
    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    /**
     * Отримати Refresh Token
     */
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    /**
     * ID поточного користувача
     */
    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    /**
     * Email поточного користувача
     */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Ім'я поточного користувача
     */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Користувач");
    }

    /**
     * Роль поточного користувача
     */
    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "STUDENT");
    }

    /**
     * Чи користувач залогінений?
     */
    public boolean isLoggedIn() {
        return getAccessToken() != null;
    }

    /**
     * Очистити сесію (logout)
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    /**
     * Оновити Access Token (після refresh)
     */
    public void updateAccessToken(String newAccessToken) {
        editor.putString(KEY_ACCESS_TOKEN, newAccessToken);
        editor.apply();
    }
}
