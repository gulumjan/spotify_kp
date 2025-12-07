package com.example.spotify_kp.data.remote;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL =
            "https://fb6a2a07-87dc-4665-bb76-70ab974cf747.mock.pstmn.io/";

    private static Retrofit retrofit;

    public static Retrofit get() {
        if (retrofit == null) {
            // Логирование HTTP запросов/ответов
            HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
            logger.setLevel(HttpLoggingInterceptor.Level.BODY);

            // OkHttp клиент с логированием
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build();

            // Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static com.example.spotify_kp.api.SpotifyApi api() {
        return get().create(com.example.spotify_kp.api.SpotifyApi.class);
    }
}