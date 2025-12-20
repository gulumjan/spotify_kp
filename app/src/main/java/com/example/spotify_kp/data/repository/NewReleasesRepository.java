package com.example.spotify_kp.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.spotify_kp.data.local.AppDatabase;
import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.data.mapper.AlbumMapper;
import com.example.spotify_kp.data.remote.RetrofitClient;
import com.example.spotify_kp.data.remote.dto.AlbumDto;
import com.example.spotify_kp.data.remote.dto.NewReleasesResponse;
import com.example.spotify_kp.utils.NetworkUtils;
import com.example.spotify_kp.utils.Resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewReleasesRepository {
    private static final String TAG = "NewReleasesRepository";

    private AppDatabase database;
    private Context context;

    public NewReleasesRepository(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
    }

    /**
     * Загрузка новинок из API (БЕЗ дубликатов)
     */
    public LiveData<Resource<List<AlbumEntity>>> loadNewReleases(int limit, int offset) {
        MutableLiveData<Resource<List<AlbumEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        // Сначала показываем что есть в кеше
        new Thread(() -> {
            List<AlbumEntity> cachedAlbums = database.albumDao().getAllAlbumsSync();
            if (!cachedAlbums.isEmpty()) {
                result.postValue(Resource.success(cachedAlbums));
                Log.d(TAG, "💾 Showing cached: " + cachedAlbums.size());
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

                            if (body.getAlbums() != null && body.getAlbums().getItems() != null) {
                                List<AlbumDto> albumDtos = body.getAlbums().getItems();
                                List<AlbumEntity> newAlbums = AlbumMapper.toEntityList(albumDtos);

                                new Thread(() -> {
                                    // ✅ ИСПРАВЛЕНИЕ: Просто вставляем (REPLACE strategy)
                                    // Room сам обработает дубликаты благодаря OnConflictStrategy.REPLACE
                                    database.albumDao().insertAll(newAlbums);

                                    // Загружаем все уникальные альбомы из БД
                                    List<AlbumEntity> allAlbums = database.albumDao().getAllAlbumsSync();

                                    result.postValue(Resource.success(allAlbums));
                                    Log.d(TAG, "✅ New releases loaded: " + newAlbums.size() +
                                            ", Total unique: " + allAlbums.size());
                                }).start();
                            }
                        } else {
                            Log.e(TAG, "❌ API error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<NewReleasesResponse> call, Throwable t) {
                        Log.e(TAG, "❌ Network error: " + t.getMessage());

                        // При ошибке показываем кеш
                        new Thread(() -> {
                            List<AlbumEntity> cachedAlbums = database.albumDao().getAllAlbumsSync();
                            result.postValue(Resource.success(cachedAlbums));
                        }).start();
                    }
                });

        return result;
    }

    /**
     * Загрузка следующей страницы (pagination)
     */
    public LiveData<Resource<List<AlbumEntity>>> loadMoreReleases(int offset) {
        return loadNewReleases(10, offset);
    }
}