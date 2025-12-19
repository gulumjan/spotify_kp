package com.example.spotify_kp.ui.favorites;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.spotify_kp.data.local.AppDatabase;
import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.data.local.entity.FavoriteEntity;
import com.example.spotify_kp.data.repository.FavoriteRepository;

import java.util.ArrayList;
import java.util.List;

public class FavoritesViewModel extends AndroidViewModel {

    private static final String TAG = "FavoritesViewModel";

    private FavoriteRepository favoriteRepository;
    private AppDatabase database;

    public FavoritesViewModel(@NonNull Application application) {
        super(application);
        favoriteRepository = new FavoriteRepository(application);
        database = AppDatabase.getInstance(application);
    }

    public LiveData<List<FavoriteEntity>> getFavorites() {
        return favoriteRepository.getFavorites();
    }

    public LiveData<List<AlbumEntity>> getAlbumsByIds(List<String> albumIds) {
        MutableLiveData<List<AlbumEntity>> result = new MutableLiveData<>();

        new Thread(() -> {
            List<AlbumEntity> albums = new ArrayList<>();

            for (String id : albumIds) {
                AlbumEntity album = database.albumDao().getAlbumByIdSync(id);
                if (album != null) {
                    albums.add(album);
                    Log.d(TAG, "Found album: " + album.getTitle());
                } else {
                    Log.w(TAG, "Album not found: " + id);
                }
            }

            Log.d(TAG, "Total albums found: " + albums.size());
            result.postValue(albums);
        }).start();

        return result;
    }

    public void removeFavorite(String albumId) {
        favoriteRepository.removeFromFavorites(albumId);
        Log.d(TAG, "Removing favorite: " + albumId);
    }
}