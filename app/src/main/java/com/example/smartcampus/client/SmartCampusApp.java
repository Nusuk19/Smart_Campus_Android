package com.example.smartcampus.client;

import android.app.Application;
import com.example.smartcampus.client.data.remote.RetrofitClient;

/**
 * 🔄 ОНОВЛЕНО: Ініціалізація RetrofitClient
 */
public class SmartCampusApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Ініціалізувати Retrofit з Context
        RetrofitClient.init(this);
    }
}