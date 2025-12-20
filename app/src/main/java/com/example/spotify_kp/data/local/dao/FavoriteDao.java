package com.example.spotify_kp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.spotify_kp.data.local.entity.FavoriteEntity;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FavoriteEntity favorite);

    @Update
    void update(FavoriteEntity favorite);

    @Query("DELETE FROM favorites WHERE album_id = :albumId AND user_id = :userId")
    void removeFavorite(String albumId, String userId);

    @Query("SELECT * FROM favorites WHERE user_id = :userId ORDER BY added_date DESC")
    LiveData<List<FavoriteEntity>> getFavoritesByUser(String userId);

    @Query("SELECT * FROM favorites WHERE user_id = :userId ORDER BY added_date DESC")
    List<FavoriteEntity> getFavoritesByUserSync(String userId);

    @Query("SELECT * FROM favorites WHERE album_id = :albumId AND user_id = :userId LIMIT 1")
    FavoriteEntity getFavoriteByAlbumSync(String albumId, String userId);

    @Query("SELECT * FROM favorites WHERE album_id = :albumId AND user_id = :userId LIMIT 1")
    LiveData<FavoriteEntity> getFavoriteByAlbum(String albumId, String userId);

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE album_id = :albumId AND user_id = :userId)")
    LiveData<Boolean> isAlbumFavorite(String albumId, String userId);

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE album_id = :albumId AND user_id = :userId)")
    boolean isAlbumFavoriteSync(String albumId, String userId);

    @Query("SELECT COUNT(*) FROM favorites WHERE user_id = :userId")
    int getFavoritesCountSync(String userId);

    @Query("DELETE FROM favorites WHERE user_id = :userId")
    void deleteAllByUser(String userId);
}