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
import com.example.spotify_kp.data.remote.dto.NewReleasesResponse;
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
            List<AlbumEntity> cachedAlbums = database.albumDao().getAllAlbumsSync();
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
                        List<AlbumEntity> albums = AlbumMapper.toEntityList(albumDtos);

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

    // Получение деталей альбома из локальной БД
    public LiveData<Resource<AlbumEntity>> getAlbumDetailsFromDb(String albumId) {
        MutableLiveData<Resource<AlbumEntity>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        new Thread(() -> {
            AlbumEntity album = database.albumDao().getAlbumByIdSync(albumId);
            if (album != null) {
                result.postValue(Resource.success(album));
                Log.d(TAG, "Album loaded from DB: " + album.getTitle());
            } else {
                result.postValue(Resource.error("Album not found", null));
                Log.e(TAG, "Album not found in DB: " + albumId);
            }
        }).start();

        return result;
    }

    // Загрузка новых релизов
    public LiveData<Resource<List<AlbumEntity>>> loadNewReleases(int limit, int offset) {
        MutableLiveData<Resource<List<AlbumEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

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
                                    Log.d(TAG, "Saved new releases to database: " + albums.size());
                                }).start();

                                result.setValue(Resource.success(albums));
                                Log.d(TAG, "Loaded new releases: " + albums.size());
                            } else {
                                result.setValue(Resource.error("No new releases found", null));
                            }
                        } else {
                            result.setValue(Resource.error("Failed to load new releases", null));
                            Log.e(TAG, "API error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<NewReleasesResponse> call, Throwable t) {
                        result.setValue(Resource.error("Network error: " + t.getMessage(), null));
                        Log.e(TAG, "Network error: " + t.getMessage());
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

                            // Сохраняем результаты поиска в БД
                            new Thread(() -> {
                                database.albumDao().insertAll(albums);
                            }).start();

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