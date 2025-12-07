package com.example.spotify_kp.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.spotify_kp.data.remote.RetrofitClient;
import com.example.spotify_kp.data.local.AppDatabase;
import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.data.mapper.AlbumMapper;
import com.example.spotify_kp.data.remote.dto.AlbumDto;
import com.example.spotify_kp.data.remote.dto.AlbumResponse;
import com.example.spotify_kp.utils.Resource;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumRepository {
    private static final String TAG = "AlbumRepository";

    private AppDatabase database;

    // Список популярных альбомов для загрузки по умолчанию
    private static final String DEFAULT_ALBUM_IDS =
            "382ObEPsp2rxGrnsizN5TX,1A2GTWGtFfWp7KSQTwWOyo,2noRn2Aes5aoNVsU6iWThc";

    public AlbumRepository(Context context) {
        this.database = AppDatabase.getInstance(context);
    }

    // Загрузка альбомов из API
    public LiveData<Resource<List<AlbumEntity>>> loadAlbums() {
        MutableLiveData<Resource<List<AlbumEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        // Сначала проверяем локальную БД
        new Thread(() -> {
            List<AlbumEntity> cachedAlbums = database.albumDao().getAllAlbums().getValue();
            if (cachedAlbums != null && !cachedAlbums.isEmpty()) {
                result.postValue(Resource.success(cachedAlbums));
                Log.d(TAG, "Loaded albums from cache: " + cachedAlbums.size());
            }
        }).start();

        // Загружаем с API
        RetrofitClient.api().getAlbums(DEFAULT_ALBUM_IDS).enqueue(new Callback<AlbumResponse>() {
            @Override
            public void onResponse(Call<AlbumResponse> call, Response<AlbumResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AlbumDto> albumDtos = response.body().getAlbums();

                    if (albumDtos != null && !albumDtos.isEmpty()) {
                        // Конвертируем DTO → Entity
                        List<AlbumEntity> albums = AlbumMapper.toEntityList(albumDtos);

                        // Сохраняем в Room
                        new Thread(() -> {
                            database.albumDao().insertAll(albums);
                            Log.d(TAG, "Saved albums to database: " + albums.size());
                        }).start();

                        result.setValue(Resource.success(albums));
                        Log.d(TAG, "Loaded albums from API: " + albums.size());
                    } else {
                        result.setValue(Resource.error("No albums found", null));
                    }
                } else {
                    result.setValue(Resource.error("Failed to load albums", null));
                    Log.e(TAG, "API error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AlbumResponse> call, Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage(), null));
                Log.e(TAG, "Network error: " + t.getMessage());
            }
        });

        return result;
    }

    // Получение деталей альбома по ID
    public LiveData<Resource<AlbumDto>> getAlbumDetails(String albumId) {
        MutableLiveData<Resource<AlbumDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        RetrofitClient.api().getAlbumById(albumId).enqueue(new Callback<AlbumDto>() {
            @Override
            public void onResponse(Call<AlbumDto> call, Response<AlbumDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                    Log.d(TAG, "Album details loaded: " + response.body().getName());
                } else {
                    result.setValue(Resource.error("Failed to load album details", null));
                }
            }

            @Override
            public void onFailure(Call<AlbumDto> call, Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage(), null));
                Log.e(TAG, "Error loading album details: " + t.getMessage());
            }
        });

        return result;
    }

    // Поиск альбомов
    public LiveData<Resource<List<AlbumEntity>>> searchAlbums(String query) {
        MutableLiveData<Resource<List<AlbumEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        RetrofitClient.api().searchAlbums(query, "album", 20)
                .enqueue(new Callback<AlbumResponse>() {
                    @Override
                    public void onResponse(Call<AlbumResponse> call, Response<AlbumResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<AlbumDto> albumDtos = response.body().getAlbums();
                            List<AlbumEntity> albums = AlbumMapper.toEntityList(albumDtos);
                            result.setValue(Resource.success(albums));
                            Log.d(TAG, "Search results: " + albums.size());
                        } else {
                            result.setValue(Resource.error("Search failed", null));
                        }
                    }

                    @Override
                    public void onFailure(Call<AlbumResponse> call, Throwable t) {
                        result.setValue(Resource.error("Network error: " + t.getMessage(), null));
                    }
                });

        return result;
    }

    // Получение всех альбомов из Room
    public LiveData<List<AlbumEntity>> getAllAlbumsFromDb() {
        return database.albumDao().getAllAlbums();
    }
}