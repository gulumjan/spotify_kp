package com.example.spotify_kp.data.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.spotify_kp.data.local.AppDatabase;
import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.data.mapper.AlbumMapper;
import com.example.spotify_kp.data.remote.RetrofitClient;
import com.example.spotify_kp.data.remote.dto.AlbumDto;
import com.example.spotify_kp.data.remote.dto.AlbumResponse;
import com.example.spotify_kp.utils.NetworkUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Менеджер синхронизации данных с сервером
 * Загружает данные только при наличии интернета и с учетом кеша
 */
public class SyncManager {
    private static final String TAG = "SyncManager";
    private static final String PREF_NAME = "SyncPrefs";
    private static final String KEY_LAST_SYNC = "last_sync_time";
    private static final long SYNC_INTERVAL = TimeUnit.HOURS.toMillis(24); // 24 часа

    private Context context;
    private AppDatabase database;
    private SharedPreferences prefs;

    // Список популярных альбомов для начальной загрузки
    private static final String DEFAULT_ALBUM_IDS =
            "382ObEPsp2rxGrnsizN5TX,1A2GTWGtFfWp7KSQTwWOyo,2noRn2Aes5aoNVsU6iWThc";

    public SyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Проверяет нужна ли синхронизация
     */
    public boolean needsSync() {
        long lastSync = prefs.getLong(KEY_LAST_SYNC, 0);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastSync) > SYNC_INTERVAL;
    }

    /**
     * Получает время последней синхронизации
     */
    public long getLastSyncTime() {
        return prefs.getLong(KEY_LAST_SYNC, 0);
    }

    /**
     * Синхронизирует данные с сервером (если есть интернет)
     */
    public void syncIfNeeded(SyncCallback callback) {
        // Проверяем есть ли интернет
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.d(TAG, "❌ No internet - working in offline mode");
            if (callback != null) {
                callback.onSyncCompleted(false, "No internet connection");
            }
            return;
        }

        // Проверяем нужна ли синхронизация
        if (!needsSync()) {
            Log.d(TAG, "✅ Data is fresh - no sync needed");
            if (callback != null) {
                callback.onSyncCompleted(true, "Data is up to date");
            }
            return;
        }

        // Выполняем синхронизацию
        Log.d(TAG, "🔄 Starting sync...");
        syncAlbums(callback);
    }

    /**
     * Принудительная синхронизация (например, по Pull-to-Refresh)
     */
    public void forceSync(SyncCallback callback) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.d(TAG, "❌ Cannot force sync - no internet");
            if (callback != null) {
                callback.onSyncCompleted(false, "No internet connection");
            }
            return;
        }

        Log.d(TAG, "🔄 Force sync started...");
        syncAlbums(callback);
    }

    /**
     * Синхронизация альбомов с API
     */
    private void syncAlbums(SyncCallback callback) {
        RetrofitClient.api().getAlbums(DEFAULT_ALBUM_IDS)
                .enqueue(new Callback<AlbumResponse>() {
                    @Override
                    public void onResponse(Call<AlbumResponse> call, Response<AlbumResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<AlbumDto> albumDtos = response.body().getAlbums();

                            if (albumDtos != null && !albumDtos.isEmpty()) {
                                List<AlbumEntity> albums = AlbumMapper.toEntityList(albumDtos);

                                // Сохраняем в БД в фоновом потоке
                                new Thread(() -> {
                                    database.albumDao().insertAll(albums);

                                    // Обновляем время последней синхронизации
                                    prefs.edit()
                                            .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
                                            .apply();

                                    Log.d(TAG, "✅ Sync completed: " + albums.size() + " albums");
                                }).start();

                                if (callback != null) {
                                    callback.onSyncCompleted(true, "Synced " + albums.size() + " albums");
                                }
                            } else {
                                Log.w(TAG, "⚠️ API returned empty data");
                                if (callback != null) {
                                    callback.onSyncCompleted(false, "No data from server");
                                }
                            }
                        } else {
                            Log.e(TAG, "❌ Sync failed: " + response.code());
                            if (callback != null) {
                                callback.onSyncCompleted(false, "Server error: " + response.code());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<AlbumResponse> call, Throwable t) {
                        Log.e(TAG, "❌ Sync failed: " + t.getMessage());
                        if (callback != null) {
                            callback.onSyncCompleted(false, "Network error: " + t.getMessage());
                        }
                    }
                });
    }

    /**
     * Очищает кеш синхронизации (для тестирования)
     */
    public void clearSyncCache() {
        prefs.edit().remove(KEY_LAST_SYNC).apply();
        Log.d(TAG, "🗑️ Sync cache cleared");
    }

    /**
     * Callback для результатов синхронизации
     */
    public interface SyncCallback {
        void onSyncCompleted(boolean success, String message);
    }
}