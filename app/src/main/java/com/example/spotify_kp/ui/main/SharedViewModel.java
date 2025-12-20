package com.example.spotify_kp.ui.main;

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
 * SharedViewModel - живёт на уровне MainActivity
 * Управляет favorites для ВСЕГО приложения
 * Работает СИНХРОННО в main thread для гарантированной записи на диск
 */
public class SharedViewModel extends AndroidViewModel {

    private static final String TAG = "SharedViewModel";

    private AppDatabase database;
    private FavoriteRepository repository;

    // Вручную управляемый список favorites
    private MutableLiveData<List<FavoriteEntity>> favoritesLiveData = new MutableLiveData<>(new ArrayList<>());

    // Кеш альбомов для быстрого доступа
    private Map<String, AlbumEntity> albumsCache = new HashMap<>();

    public SharedViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(application);
        repository = new FavoriteRepository(application);

        Log.d(TAG, "🚀 SharedViewModel created (Activity-scoped)");

        // Загружаем favorites сразу при создании
        loadFavorites();
    }

    /**
     * Загрузить все favorites из БД
     * Работает СИНХРОННО в main thread
     */
    public void loadFavorites() {
        Log.d(TAG, "📥 Loading favorites (main thread)...");

        // Загружаем прямо в main thread т.к. allowMainThreadQueries включен
        List<FavoriteEntity> favorites = repository.getAllFavoritesSync();

        // Обновляем LiveData
        favoritesLiveData.setValue(new ArrayList<>(favorites));

        Log.d(TAG, "✅ Loaded " + favorites.size() + " favorites");
    }

    /**
     * Получить LiveData со списком favorites
     */
    public LiveData<List<FavoriteEntity>> getFavorites() {
        return favoritesLiveData;
    }

    /**
     * Добавить альбом в избранное
     * Работает СИНХРОННО для гарантированной записи
     */
    public void addToFavorites(String albumId, String comment, float rating) {
        Log.d(TAG, "➕ Adding to favorites (main thread): " + albumId);

        // СИНХРОННО сохраняем в БД
        boolean success = repository.addToFavoritesSync(albumId, comment, rating);

        if (success) {
            // Сразу перезагружаем список
            loadFavorites();
            Log.d(TAG, "✅ Successfully added and reloaded list!");
        } else {
            Log.e(TAG, "❌ Failed to add to favorites!");
        }
    }

    /**
     * Удалить альбом из избранного
     * Работает СИНХРОННО
     */
    public void removeFromFavorites(String albumId) {
        Log.d(TAG, "🗑️ Removing from favorites (main thread): " + albumId);

        boolean success = repository.removeFromFavoritesSync(albumId);

        if (success) {
            // Перезагружаем список
            loadFavorites();
            Log.d(TAG, "✅ Successfully removed and reloaded list!");
        } else {
            Log.e(TAG, "❌ Failed to remove from favorites!");
        }
    }

    /**
     * Обновить комментарий и рейтинг
     */
    public void updateFavorite(String albumId, String comment, float rating) {
        Log.d(TAG, "✏️ Updating favorite: " + albumId);

        repository.updateFavoriteSync(albumId, comment, rating);

        // Перезагружаем список
        loadFavorites();
    }

    /**
     * Проверить является ли альбом избранным
     * Работает СИНХРОННО
     */
    public LiveData<Boolean> isAlbumFavorite(String albumId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        // СИНХРОННО проверяем в main thread
        boolean isFav = repository.isAlbumFavoriteSync(albumId);
        result.setValue(isFav);

        Log.d(TAG, "❓ Is favorite: " + isFav + " for album: " + albumId);

        return result;
    }

    /**
     * Получить альбомы по списку ID
     * Использует кеш для оптимизации
     */
    public LiveData<List<AlbumEntity>> getAlbumsByIds(List<String> albumIds) {
        MutableLiveData<List<AlbumEntity>> result = new MutableLiveData<>();
        List<AlbumEntity> albums = new ArrayList<>();

        Log.d(TAG, "🔍 Loading " + albumIds.size() + " albums...");

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
     * Получить количество избранных альбомов
     */
    public int getFavoritesCount() {
        return repository.getFavoritesCountSync();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "💀 SharedViewModel cleared");
    }
}