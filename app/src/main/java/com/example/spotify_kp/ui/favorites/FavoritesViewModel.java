package com.example.spotify_kp.ui.favorites;

import android.app.Application;

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
                AlbumEntity album = database.albumDao().getAlbumById(id).getValue();
                if (album != null) {
                    albums.add(album);
                }
            }
            result.postValue(albums);
        }).start();

        return result;
    }

    public void removeFavorite(String albumId) {
        favoriteRepository.removeFromFavorites(albumId);
    }
}