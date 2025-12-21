package com.example.spotify_kp.data.repository;

import android.content.Context;
import android.util.Log;

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

        Log.d(TAG, "✅ FavoriteRepository created for user: " + prefsManager.getUserId());
    }

    // Добавить в избранное - СИНХРОННО через DAO
    public boolean addToFavoritesSync(String albumId, String comment, float rating) {
        try {
            String userId = prefsManager.getUserId();
            Log.d(TAG, "🔵 START: Adding favorite - Album: " + albumId);

            // Проверяем существует ли уже
            boolean exists = database.favoriteDao().isAlbumFavoriteSync(albumId, userId);

            if (exists) {
                // Обновляем существующий через DAO
                FavoriteEntity existing = database.favoriteDao().getFavoriteByAlbumSync(albumId, userId);
                if (existing != null) {
                    existing.setUserComment(comment);
                    existing.setUserRating(rating);
                    existing.setFavorite(true);
                    existing.setAddedDate(System.currentTimeMillis()); // Обновляем timestamp

                    database.favoriteDao().update(existing);

                    // ✅ NEW: Force database checkpoint to ensure data is written to disk
                    try {
                        database.getOpenHelper().getWritableDatabase()
                                .execSQL("PRAGMA wal_checkpoint(TRUNCATE)");
                        Log.d(TAG, "✅ Database checkpoint executed");
                    } catch (Exception e) {
                        Log.w(TAG, "⚠️ Checkpoint warning: " + e.getMessage());
                    }

                    Log.d(TAG, "📝 Updated existing favorite");
                }
            } else {
                // Вставляем новый через DAO
                FavoriteEntity favorite = new FavoriteEntity();
                favorite.setAlbumId(albumId);
                favorite.setUserId(userId);
                favorite.setUserComment(comment);
                favorite.setUserRating(rating);
                favorite.setAddedDate(System.currentTimeMillis());
                favorite.setFavorite(true);

                long insertId = database.favoriteDao().insert(favorite);

                // ✅ NEW: Force database checkpoint
                try {
                    database.getOpenHelper().getWritableDatabase()
                            .execSQL("PRAGMA wal_checkpoint(TRUNCATE)");
                    Log.d(TAG, "✅ Database checkpoint executed");
                } catch (Exception e) {
                    Log.w(TAG, "⚠️ Checkpoint warning: " + e.getMessage());
                }

                Log.d(TAG, "➕ Inserted new favorite with ID: " + insertId);
            }

            // ✅ NEW: Add small delay to ensure write completion
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Проверка что реально сохранилось
            boolean check = database.favoriteDao().isAlbumFavoriteSync(albumId, userId);

            if (check) {
                Log.d(TAG, "✅ VERIFIED: Album successfully saved!");
            } else {
                Log.e(TAG, "❌ ERROR: Album NOT saved after operation!");
                return false;
            }

            // Выводим текущее количество
            int count = database.favoriteDao().getFavoritesCountSync(userId);
            Log.d(TAG, "📊 Total favorites in database: " + count);

            Log.d(TAG, "🔵 END: Operation complete");

            return true;

        } catch (Exception e) {
            Log.e(TAG, "❌ CRITICAL ERROR adding to favorites: " + e.getMessage(), e);
            e.printStackTrace();
            return false;
        }
    }

    // Удалить из избранного - СИНХРОННО через DAO
    public boolean removeFromFavoritesSync(String albumId) {
        try {
            String userId = prefsManager.getUserId();
            Log.d(TAG, "🗑️ Removing favorite: " + albumId);

            // Удаляем через DAO
            database.favoriteDao().removeFavorite(albumId, userId);

            // ✅ NEW: Force database checkpoint
            try {
                database.getOpenHelper().getWritableDatabase()
                        .execSQL("PRAGMA wal_checkpoint(TRUNCATE)");
                Log.d(TAG, "✅ Database checkpoint executed after delete");
            } catch (Exception e) {
                Log.w(TAG, "⚠️ Checkpoint warning: " + e.getMessage());
            }

            // ✅ NEW: Add small delay to ensure delete completion
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Проверка
            boolean stillExists = database.favoriteDao().isAlbumFavoriteSync(albumId, userId);

            if (stillExists) {
                Log.e(TAG, "❌ ERROR: Album still exists after delete!");
                return false;
            } else {
                Log.d(TAG, "✅ Successfully removed from favorites!");

                // Log current count
                int count = database.favoriteDao().getFavoritesCountSync(userId);
                Log.d(TAG, "📊 Remaining favorites: " + count);

                return true;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR removing from favorites: " + e.getMessage(), e);
            return false;
        }
    }

    // Обновить комментарий и рейтинг - СИНХРОННО через DAO
    public void updateFavoriteSync(String albumId, String comment, float rating) {
        try {
            String userId = prefsManager.getUserId();
            Log.d(TAG, "✏️ Updating: " + albumId);

            FavoriteEntity favorite = database.favoriteDao().getFavoriteByAlbumSync(albumId, userId);

            if (favorite != null) {
                favorite.setUserComment(comment);
                favorite.setUserRating(rating);
                database.favoriteDao().update(favorite);

                // ✅ NEW: Force database checkpoint
                try {
                    database.getOpenHelper().getWritableDatabase()
                            .execSQL("PRAGMA wal_checkpoint(TRUNCATE)");
                    Log.d(TAG, "✅ Database checkpoint executed after update");
                } catch (Exception e) {
                    Log.w(TAG, "⚠️ Checkpoint warning: " + e.getMessage());
                }

                Log.d(TAG, "✅ Updated successfully");
            } else {
                Log.w(TAG, "⚠️ Favorite not found for update");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR updating favorite: " + e.getMessage(), e);
        }
    }

    // Получить все избранные - СИНХРОННО через DAO
    public List<FavoriteEntity> getAllFavoritesSync() {
        try {
            String userId = prefsManager.getUserId();

            // Используем синхронный метод DAO
            List<FavoriteEntity> favorites = database.favoriteDao().getFavoritesByUserSync(userId);

            Log.d(TAG, "📋 Loaded " + (favorites != null ? favorites.size() : 0) + " favorites");

            return favorites;

        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR loading favorites: " + e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    // Проверить находится ли альбом в избранном - СИНХРОННО
    public boolean isAlbumFavoriteSync(String albumId) {
        String userId = prefsManager.getUserId();
        boolean isFavorite = database.favoriteDao().isAlbumFavoriteSync(albumId, userId);
        Log.d(TAG, "❓ Is " + albumId + " favorite: " + isFavorite);
        return isFavorite;
    }

    // Получить количество избранных альбомов - СИНХРОННО
    public int getFavoritesCountSync() {
        String userId = prefsManager.getUserId();
        int count = database.favoriteDao().getFavoritesCountSync(userId);
        Log.d(TAG, "🔢 Favorites count: " + count);
        return count;
    }

    // Удалить все избранное пользователя
    public void clearAllFavorites() {
        String userId = prefsManager.getUserId();
        database.favoriteDao().deleteAllByUser(userId);

        // ✅ NEW: Force database checkpoint
        try {
            database.getOpenHelper().getWritableDatabase()
                    .execSQL("PRAGMA wal_checkpoint(TRUNCATE)");
            Log.d(TAG, "✅ Database checkpoint executed after clear all");
        } catch (Exception e) {
            Log.w(TAG, "⚠️ Checkpoint warning: " + e.getMessage());
        }

        Log.d(TAG, "🗑️ Cleared all favorites for user: " + userId);
    }
}