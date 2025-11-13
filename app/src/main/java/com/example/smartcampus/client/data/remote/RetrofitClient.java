package com.example.smartcampus.client.data.remote;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
public class RetrofitClient {
    private static final String BASE = "http://10.0.2.2:8080/api/";
    private static Retrofit retrofit;
    public static Retrofit get() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(BASE).addConverterFactory(JacksonConverterFactory.create()).build();
        }
        return retrofit;
    }
}
