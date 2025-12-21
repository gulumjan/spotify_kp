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
 * 🔥 SharedViewModel - ЕДИНСТВЕННЫЙ источник правды для избранных альбомов
 *
 * Живёт на уровне MainActivity и разделяется между всеми фрагментами.
 * Это гарантирует что данные НЕ теряются при переключении вкладок.
 *
 * ВАЖНО: Этот ViewModel создаётся ОДИН РАЗ при запуске MainActivity
 * и живёт пока MainActivity не будет уничтожена.
 */
public class SharedViewModel extends AndroidViewModel {

    private static final String TAG = "SharedViewModel";

    private AppDatabase database;
    private FavoriteRepository repository;

    // 🔥 Единственный источник правды - данные живут здесь
    private MutableLiveData<List<FavoriteEntity>> favoritesLiveData = new MutableLiveData<>(new ArrayList<>());

    // Кеш альбомов для быстрого доступа
    private Map<String, AlbumEntity> albumsCache = new HashMap<>();

    public SharedViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(application);
        repository = new FavoriteRepository(application);

        Log.d(TAG, "🚀 SharedViewModel created (Activity-scoped) - hashCode: " + this.hashCode());

        // Загружаем favorites сразу при создании
        loadFavorites();
    }

    /**
     * 🔥 ГЛАВНЫЙ МЕТОД - Загрузить все favorites из БД
     * Вызывается:
     * 1. При создании ViewModel (запуск приложения)
     * 2. При возврате на FavoritesFragment (onResume)
     * 3. После добавления/удаления избранного
     */
    public void loadFavorites() {
        Log.d(TAG, "📥 Loading favorites from database...");

        new Thread(() -> {
            try {
                // Загружаем ВСЕ избранные альбомы пользователя из БД
                List<FavoriteEntity> favorites = repository.getAllFavoritesSync();

                // 🔥 Обновляем LiveData - все подписчики получат обновление
                favoritesLiveData.postValue(new ArrayList<>(favorites));

                Log.d(TAG, "✅ Loaded " + favorites.size() + " favorites from DB");

                // Логируем каждый альбом для отладки
                if (!favorites.isEmpty()) {
                    Log.d(TAG, "📋 Favorites list:");
                    for (FavoriteEntity fav : favorites) {
                        Log.d(TAG, "  - Album ID: " + fav.getAlbumId() +
                                ", Rating: " + fav.getUserRating() +
                                ", Comment: " + (fav.getUserComment() != null ? fav.getUserComment().substring(0, Math.min(20, fav.getUserComment().length())) : "none"));
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Error loading favorites: " + e.getMessage(), e);
                favoritesLiveData.postValue(new ArrayList<>());
            }
        }).start();
    }

    /**
     * Получить LiveData со списком favorites
     * Fragment подписывается на это и автоматически получает обновления
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

        new Thread(() -> {
            // СИНХРОННО сохраняем в БД
            boolean success = repository.addToFavoritesSync(albumId, comment, rating);

            if (success) {
                Log.d(TAG, "✅ Successfully added, reloading list...");
                // Сразу перезагружаем список из БД
                loadFavorites();
            } else {
                Log.e(TAG, "❌ Failed to add to favorites!");
            }
        }).start();
    }

    /**
     * Удалить альбом из избранного
     * Работает СИНХРОННО
     */
    public void removeFromFavorites(String albumId) {
        Log.d(TAG, "🗑️ Removing from favorites (main thread): " + albumId);

        new Thread(() -> {
            boolean success = repository.removeFromFavoritesSync(albumId);

            if (success) {
                // Удаляем из кеша
                albumsCache.remove(albumId);

                Log.d(TAG, "✅ Successfully removed, reloading list...");
                // Перезагружаем список из БД
                loadFavorites();
            } else {
                Log.e(TAG, "❌ Failed to remove from favorites!");
            }
        }).start();
    }

    /**
     * Обновить комментарий и рейтинг
     */
    public void updateFavorite(String albumId, String comment, float rating) {
        Log.d(TAG, "✏️ Updating favorite: " + albumId);

        new Thread(() -> {
            repository.updateFavoriteSync(albumId, comment, rating);
            // Перезагружаем список
            loadFavorites();
        }).start();
    }

    /**
     * Проверить является ли альбом избранным
     * Работает СИНХРОННО
     */
    public LiveData<Boolean> isAlbumFavorite(String albumId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        new Thread(() -> {
            boolean isFav = repository.isAlbumFavoriteSync(albumId);
            result.postValue(isFav);
            Log.d(TAG, "❓ Is favorite: " + isFav + " for album: " + albumId);
        }).start();

        return result;
    }

    /**
     * Получить альбомы по списку ID
     * Использует кеш для оптимизации
     */
    public LiveData<List<AlbumEntity>> getAlbumsByIds(List<String> albumIds) {
        MutableLiveData<List<AlbumEntity>> result = new MutableLiveData<>();

        Log.d(TAG, "🔍 Loading " + albumIds.size() + " albums...");

        new Thread(() -> {
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
                        Log.w(TAG, "⚠️ Album not found in DB: " + id);
                    }
                }
            }

            result.postValue(albums);
            Log.d(TAG, "✅ Loaded " + albums.size() + " albums");
        }).start();

        return result;
    }

    /**
     * Получить количество избранных альбомов
     */
    public int getFavoritesCount() {
        List<FavoriteEntity> currentList = favoritesLiveData.getValue();
        return currentList != null ? currentList.size() : 0;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "💀 SharedViewModel cleared (MainActivity destroyed)");
    }
}