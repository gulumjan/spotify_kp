package com.example.spotify_kp.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.spotify_kp.data.local.AppDatabase;
import com.example.spotify_kp.data.local.entity.FavoriteEntity;
import com.example.spotify_kp.utils.SharedPrefsManager;

import java.util.List;

public class FavoriteRepository {
    private static final String TAG = "FavoriteRepository";

    private AppDatabase database;
    private SharedPrefsManager prefsManager;

    public FavoriteRepository(Context context) {
        this.database = AppDatabase.getInstance(context);
        this.prefsManager = SharedPrefsManager.getInstance(context);
    }

    // Добавить альбом в избранное
    public void addToFavorites(String albumId, String comment, float rating) {
        new Thread(() -> {
            FavoriteEntity favorite = new FavoriteEntity();
            favorite.setAlbumId(albumId);
            favorite.setUserId(prefsManager.getUserId());
            favorite.setUserComment(comment);
            favorite.setUserRating(rating);
            favorite.setAddedDate(System.currentTimeMillis());
            favorite.setFavorite(true);

            database.favoriteDao().insert(favorite);
            Log.d(TAG, "Album added to favorites: " + albumId + " with rating: " + rating);
        }).start();
    }

    // Удалить из избранного
    public void removeFromFavorites(String albumId) {
        new Thread(() -> {
            String userId = prefsManager.getUserId();
            database.favoriteDao().removeFavorite(albumId, userId);
            Log.d(TAG, "Album removed from favorites: " + albumId);
        }).start();
    }

    // Обновить комментарий и рейтинг
    public void updateFavorite(String albumId, String comment, float rating) {
        new Thread(() -> {
            String userId = prefsManager.getUserId();
            FavoriteEntity favorite = database.favoriteDao().getFavoriteByAlbumSync(albumId, userId);

            if (favorite != null) {
                favorite.setUserComment(comment);
                favorite.setUserRating(rating);
                database.favoriteDao().update(favorite);
                Log.d(TAG, "Favorite updated: " + albumId);
            }
        }).start();
    }

    // Получить все избранные альбомы
    public LiveData<List<FavoriteEntity>> getFavorites() {
        String userId = prefsManager.getUserId();
        return database.favoriteDao().getFavoritesByUser(userId);
    }

    // Проверить, находится ли альбом в избранном
    public LiveData<Boolean> isAlbumFavorite(String albumId) {
        String userId = prefsManager.getUserId();
        return database.favoriteDao().isAlbumFavorite(albumId, userId);
    }

    // Получить информацию о избранном альбоме
    public LiveData<FavoriteEntity> getFavoriteByAlbum(String albumId) {
        String userId = prefsManager.getUserId();
        return database.favoriteDao().getFavoriteByAlbum(albumId, userId);
    }

    // Получить количество избранных альбомов
    public LiveData<Integer> getFavoritesCount() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        String userId = prefsManager.getUserId();

        new Thread(() -> {
            int count = database.favoriteDao().getFavoritesCountSync(userId);
            result.postValue(count);
        }).start();

        return result;
    }

    // Удалить все избранное пользователя
    public void clearAllFavorites() {
        new Thread(() -> {
            String userId = prefsManager.getUserId();
            database.favoriteDao().deleteAllByUser(userId);
            Log.d(TAG, "All favorites cleared for user: " + userId);
        }).start();
    }
}