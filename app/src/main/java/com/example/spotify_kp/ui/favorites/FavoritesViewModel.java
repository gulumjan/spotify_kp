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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FavoritesViewModel - управляет избранными альбомами
 * Работает СИНХРОННО для гарантированной офлайн работы
 */
public class FavoritesViewModel extends AndroidViewModel {

    private static final String TAG = "FavoritesViewModel";

    private FavoriteRepository favoriteRepository;
    private AppDatabase database;

    // Вручную управляемый список favorites
    private MutableLiveData<List<FavoriteEntity>> favoritesLiveData = new MutableLiveData<>(new ArrayList<>());
    private Map<String, AlbumEntity> albumsCache = new HashMap<>();

    public FavoritesViewModel(@NonNull Application application) {
        super(application);
        favoriteRepository = new FavoriteRepository(application);
        database = AppDatabase.getInstance(application);

        Log.d(TAG, "✅ FavoritesViewModel created (Activity-scoped)");

        // Загружаем favorites при создании
        loadFavorites();
    }

    /**
     * Загрузить все favorites из БД
     */
    public void loadFavorites() {
        Log.d(TAG, "📥 Loading favorites...");

        // Загружаем СИНХРОННО через Repository
        List<FavoriteEntity> favorites = favoriteRepository.getAllFavoritesSync();
        favoritesLiveData.setValue(new ArrayList<>(favorites));

        Log.d(TAG, "✅ Loaded " + favorites.size() + " favorites");
    }

    public LiveData<List<FavoriteEntity>> getFavorites() {
        return favoritesLiveData;
    }

    /**
     * Получить альбомы по списку ID
     */
    public LiveData<List<AlbumEntity>> getAlbumsByIds(List<String> albumIds) {
        MutableLiveData<List<AlbumEntity>> result = new MutableLiveData<>();

        Log.d(TAG, "🔍 Loading " + albumIds.size() + " albums...");

        List<AlbumEntity> albums = new ArrayList<>();

        for (String id : albumIds) {
            // Проверяем кеш
            if (albumsCache.containsKey(id)) {
                albums.add(albumsCache.get(id));
                Log.d(TAG, "💾 Album from cache: " + id);
            } else {
                // Загружаем из БД (синхронно)
                AlbumEntity album = database.albumDao().getAlbumByIdSync(id);
                if (album != null) {
                    albums.add(album);
                    albumsCache.put(id, album);
                    Log.d(TAG, "💿 Album from DB: " + album.getTitle());
                } else {
                    Log.w(TAG, "⚠️ Album not found: " + id);
                }
            }
        }

        result.setValue(albums);
        Log.d(TAG, "✅ Loaded " + albums.size() + " albums");

        return result;
    }

    /**
     * Удалить альбом из избранного
     */
    public void removeFavorite(String albumId) {
        Log.d(TAG, "🗑️ Removing favorite: " + albumId);

        // СИНХРОННО удаляем через Repository
        boolean success = favoriteRepository.removeFromFavoritesSync(albumId);

        if (success) {
            // Удаляем из кеша
            albumsCache.remove(albumId);

            // Перезагружаем список
            loadFavorites();

            Log.d(TAG, "✅ Removed successfully");
        } else {
            Log.e(TAG, "❌ Failed to remove");
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "💀 FavoritesViewModel cleared");
    }
}