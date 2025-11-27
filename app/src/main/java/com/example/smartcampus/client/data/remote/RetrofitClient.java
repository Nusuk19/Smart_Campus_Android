package com.example.smartcampus.client.data.remote;

import android.content.Context;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import com.example.smartcampus.client.utils.SessionManager;

/**
 * 🔄 ОНОВЛЕНО: Додано автоматичне підставлення JWT токену
 */
public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/api/";
    private static Retrofit retrofit;
    private static Context appContext;

    /**
     * Ініціалізація (викликати в Application.onCreate())
     */
    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    /**
     * Отримати Retrofit інстанс з автоматичним JWT
     */
    public static Retrofit get() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();

                        // Додати JWT токен якщо є
                        if (appContext != null) {
                            SessionManager sessionManager = new SessionManager(appContext);
                            String token = sessionManager.getAccessToken();

                            if (token != null) {
                                Request newRequest = original.newBuilder()
                                        .header("Authorization", "Bearer " + token)
                                        .build();
                                return chain.proceed(newRequest);
                            }
                        }

                        return chain.proceed(original);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Скинути інстанс (при logout)
     */
    public static void reset() {
        retrofit = null;
    }
}