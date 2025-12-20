package com.example.spotify_kp.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.spotify_kp.data.remote.RetrofitClient;
import com.example.spotify_kp.data.local.AppDatabase;
import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.data.mapper.AlbumMapper;
import com.example.spotify_kp.data.remote.dto.AlbumDto;
import com.example.spotify_kp.data.remote.dto.AlbumResponse;
import com.example.spotify_kp.data.remote.dto.NewReleasesResponse;
import com.example.spotify_kp.utils.NetworkUtils;
import com.example.spotify_kp.utils.Resource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AlbumRepository с полной поддержкой офлайн-режима
 * Стратегия: Cache-First (сначала кеш, потом обновление с сервера)
 */
public class AlbumRepository {
    private static final String TAG = "AlbumRepository";
    private static final String PREF_NAME = "AlbumSyncPrefs";
    private static final String KEY_LAST_SYNC = "last_sync_albums";
    private static final long SYNC_INTERVAL = TimeUnit.HOURS.toMillis(24); // 24 часа

    private Context context;
    private AppDatabase database;
    private SharedPreferences prefs;

    // Список популярных альбомов для загрузки по умолчанию
    private static final String DEFAULT_ALBUM_IDS =
            "382ObEPsp2rxGrnsizN5TX,1A2GTWGtFfWp7KSQTwWOyo,2noRn2Aes5aoNVsU6iWThc";

    public AlbumRepository(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * ГЛАВНЫЙ МЕТОД - Загрузка альбомов с поддержкой офлайн-режима
     */
    public LiveData<Resource<List<AlbumEntity>>> loadAlbums() {
        MutableLiveData<Resource<List<AlbumEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        // 1. Загружаем из кеша (СИНХРОННО в фоновом потоке)
        new Thread(() -> {
            List<AlbumEntity> cachedAlbums = database.albumDao().getAllAlbumsSync();

            if (cachedAlbums != null && !cachedAlbums.isEmpty()) {
                // Есть кеш - показываем сразу
                result.postValue(Resource.success(cachedAlbums));
                Log.d(TAG, "✅ Loaded from cache: " + cachedAlbums.size() + " albums");
            } else {
                // Нет кеша
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    result.postValue(Resource.error("No data available. Please connect to internet.", null));
                    Log.e(TAG, "❌ No cache and no internet");
                    return;
                }
                Log.d(TAG, "⚠️ Cache is empty, loading from API...");
            }

            // 2. Проверяем нужна ли синхронизация
            if (NetworkUtils.isNetworkAvailable(context) && needsSync()) {
                syncFromServer(result);
            } else {
                Log.d(TAG, "📶 Offline mode or data is fresh");
            }
        }).start();

        return result;
    }

    /**
     * Принудительное обновление (Pull-to-Refresh)
     */
    public LiveData<Resource<List<AlbumEntity>>> forceRefresh() {
        MutableLiveData<Resource<List<AlbumEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        if (!NetworkUtils.isNetworkAvailable(context)) {
            // Нет интернета - показываем кеш
            new Thread(() -> {
                List<AlbumEntity> cachedAlbums = database.albumDao().getAllAlbumsSync();
                result.postValue(Resource.success(cachedAlbums));
                Log.d(TAG, "📶 Offline - showing cached data");
            }).start();
            return result;
        }

        // Есть интернет - принудительная загрузка
        syncFromServer(result);
        return result;
    }

    /**
     * Синхронизация с сервером
     */
    private void syncFromServer(MutableLiveData<Resource<List<AlbumEntity>>> result) {
        Log.d(TAG, "🔄 Syncing from server...");

        RetrofitClient.api().getAlbums(DEFAULT_ALBUM_IDS).enqueue(new Callback<AlbumResponse>() {
            @Override
            public void onResponse(Call<AlbumResponse> call, Response<AlbumResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AlbumDto> albumDtos = response.body().getAlbums();

                    if (albumDtos != null && !albumDtos.isEmpty()) {
                        List<AlbumEntity> albums = AlbumMapper.toEntityList(albumDtos);

                        new Thread(() -> {
                            database.albumDao().insertAll(albums);

                            // Обновляем время синхронизации
                            prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply();

                            result.postValue(Resource.success(albums));
                            Log.d(TAG, "✅ Synced from server: " + albums.size() + " albums");
                        }).start();
                    } else {
                        result.postValue(Resource.error("No albums found", null));
                    }
                } else {
                    result.postValue(Resource.error("Failed to load albums", null));
                    Log.e(TAG, "❌ API error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AlbumResponse> call, Throwable t) {
                // При ошибке показываем кеш
                new Thread(() -> {
                    List<AlbumEntity> cachedAlbums = database.albumDao().getAllAlbumsSync();
                    if (!cachedAlbums.isEmpty()) {
                        result.postValue(Resource.success(cachedAlbums));
                    } else {
                        result.postValue(Resource.error("Network error: " + t.getMessage(), null));
                    }
                }).start();
                Log.e(TAG, "❌ Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Проверяет нужна ли синхронизация
     */
    private boolean needsSync() {
        long lastSync = prefs.getLong(KEY_LAST_SYNC, 0);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastSync) > SYNC_INTERVAL;
    }

    /**
     * Получение деталей альбома из локальной БД
     */
    public LiveData<Resource<AlbumEntity>> getAlbumDetailsFromDb(String albumId) {
        MutableLiveData<Resource<AlbumEntity>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        new Thread(() -> {
            AlbumEntity album = database.albumDao().getAlbumByIdSync(albumId);
            if (album != null) {
                result.postValue(Resource.success(album));
                Log.d(TAG, "✅ Album loaded from DB: " + album.getTitle());
            } else {
                result.postValue(Resource.error("Album not found", null));
                Log.e(TAG, "❌ Album not found in DB: " + albumId);
            }
        }).start();

        return result;
    }

    /**
     * Загрузка новых релизов
     */
    public LiveData<Resource<List<AlbumEntity>>> loadNewReleases(int limit, int offset) {
        MutableLiveData<Resource<List<AlbumEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        // Сначала показываем кеш
        new Thread(() -> {
            List<AlbumEntity> cachedAlbums = database.albumDao().getAllAlbumsSync();
            if (!cachedAlbums.isEmpty()) {
                result.postValue(Resource.success(cachedAlbums));
                Log.d(TAG, "💾 Showing cached albums");
            }
        }).start();

        // Если есть интернет - загружаем новое
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.d(TAG, "📶 Offline - showing cache only");
            return result;
        }

        RetrofitClient.api().getNewReleases(limit, offset)
                .enqueue(new Callback<NewReleasesResponse>() {
                    @Override
                    public void onResponse(Call<NewReleasesResponse> call,
                                           Response<NewReleasesResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            NewReleasesResponse body = response.body();

                            if (body.getAlbums() != null &&
                                    body.getAlbums().getItems() != null) {

                                List<AlbumDto> albumDtos = body.getAlbums().getItems();
                                List<AlbumEntity> albums = AlbumMapper.toEntityList(albumDtos);

                                new Thread(() -> {
                                    database.albumDao().insertAll(albums);
                                    result.postValue(Resource.success(albums));
                                    Log.d(TAG, "✅ New releases loaded: " + albums.size());
                                }).start();
                            }
                        } else {
                            Log.e(TAG, "❌ API error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<NewReleasesResponse> call, Throwable t) {
                        Log.e(TAG, "❌ Network error: " + t.getMessage());
                    }
                });

        return result;
    }

    /**
     * Поиск альбомов (офлайн)
     */
    public LiveData<Resource<List<AlbumEntity>>> searchAlbums(String query) {
        MutableLiveData<Resource<List<AlbumEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        new Thread(() -> {
            List<AlbumEntity> albums = database.albumDao().searchAlbumsSync(query);
            result.postValue(Resource.success(albums));
            Log.d(TAG, "🔍 Search results: " + albums.size() + " albums");
        }).start();

        return result;
    }

    /**
     * Фильтрация по жанру (офлайн)
     */
    public LiveData<Resource<List<AlbumEntity>>> getAlbumsByGenre(String genre) {
        MutableLiveData<Resource<List<AlbumEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        new Thread(() -> {
            List<AlbumEntity> albums = database.albumDao().getAlbumsByGenreSync(genre);
            result.postValue(Resource.success(albums));
            Log.d(TAG, "🎵 Genre filter: " + albums.size() + " albums");
        }).start();

        return result;
    }

    /**
     * Фильтрация по году (офлайн)
     */
    public LiveData<Resource<List<AlbumEntity>>> getAlbumsByYear(String year) {
        MutableLiveData<Resource<List<AlbumEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        new Thread(() -> {
            List<AlbumEntity> albums = database.albumDao().getAlbumsByYearSync(year);
            result.postValue(Resource.success(albums));
            Log.d(TAG, "📅 Year filter: " + albums.size() + " albums");
        }).start();

        return result;
    }

    /**
     * Получение всех альбомов из Room
     */
    public LiveData<List<AlbumEntity>> getAllAlbumsFromDb() {
        return database.albumDao().getAllAlbums();
    }

    /**
     * Получить время последней синхронизации
     */
    public long getLastSyncTime() {
        return prefs.getLong(KEY_LAST_SYNC, 0);
    }
}