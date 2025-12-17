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
import com.example.spotify_kp.utils.Resource;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewReleasesRepository {
    private static final String TAG = "NewReleasesRepository";

    private AppDatabase database;

    public NewReleasesRepository(Context context) {
        this.database = AppDatabase.getInstance(context);
    }

    // Загрузка новинок из API
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

                                // Сохраняем в Room
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

    // Загрузка следующей страницы (pagination)
    public LiveData<Resource<List<AlbumEntity>>> loadMoreReleases(int offset) {
        return loadNewReleases(10, offset);
    }
}