package com.example.spotify_kp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.spotify_kp.data.local.entity.AlbumEntity;

import java.util.List;

@Dao
public interface AlbumDao {

    // ===== INSERT =====
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AlbumEntity album);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AlbumEntity> albums);

    // ===== UPDATE =====
    @Update
    void update(AlbumEntity album);

    // ===== DELETE =====
    @Delete
    void delete(AlbumEntity album);

    @Query("DELETE FROM albums")
    void deleteAll();

    // ===== QUERIES - LIVE DATA (реактивные) =====

    @Query("SELECT * FROM albums ORDER BY created_at DESC")
    LiveData<List<AlbumEntity>> getAllAlbums();

    @Query("SELECT * FROM albums WHERE id = :albumId LIMIT 1")
    LiveData<AlbumEntity> getAlbumById(String albumId);

    @Query("SELECT * FROM albums WHERE genre = :genre ORDER BY year DESC")
    LiveData<List<AlbumEntity>> getAlbumsByGenre(String genre);

    @Query("SELECT * FROM albums WHERE year = :year ORDER BY title ASC")
    LiveData<List<AlbumEntity>> getAlbumsByYear(String year);

    @Query("SELECT * FROM albums WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' ORDER BY created_at DESC")
    LiveData<List<AlbumEntity>> searchAlbums(String query);

    // ===== QUERIES - SYNC (синхронные для офлайн-режима) =====

    @Query("SELECT * FROM albums ORDER BY created_at DESC")
    List<AlbumEntity> getAllAlbumsSync();

    @Query("SELECT * FROM albums WHERE id = :albumId LIMIT 1")
    AlbumEntity getAlbumByIdSync(String albumId);

    @Query("SELECT * FROM albums WHERE genre = :genre ORDER BY year DESC")
    List<AlbumEntity> getAlbumsByGenreSync(String genre);

    @Query("SELECT * FROM albums WHERE year = :year ORDER BY title ASC")
    List<AlbumEntity> getAlbumsByYearSync(String year);

    @Query("SELECT * FROM albums WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' ORDER BY created_at DESC")
    List<AlbumEntity> searchAlbumsSync(String query);

    // ===== UTILITY QUERIES =====

    @Query("SELECT COUNT(*) FROM albums")
    int getAlbumsCount();

    @Query("SELECT COUNT(*) FROM albums")
    LiveData<Integer> getAlbumsCountLive();

    @Query("SELECT DISTINCT genre FROM albums WHERE genre IS NOT NULL AND genre != '' ORDER BY genre ASC")
    List<String> getAllGenres();

    @Query("SELECT DISTINCT year FROM albums WHERE year IS NOT NULL AND year != '' AND year != 'Unknown' ORDER BY year DESC")
    List<String> getAllYears();

    @Query("SELECT * FROM albums ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    List<AlbumEntity> getAlbumsPaginated(int limit, int offset);

    // ===== FAVORITES QUERIES =====

    @Query("SELECT a.* FROM albums a INNER JOIN favorites f ON a.id = f.album_id WHERE f.user_id = :userId ORDER BY f.added_date DESC")
    LiveData<List<AlbumEntity>> getFavoriteAlbums(String userId);

    @Query("SELECT a.* FROM albums a INNER JOIN favorites f ON a.id = f.album_id WHERE f.user_id = :userId ORDER BY f.added_date DESC")
    List<AlbumEntity> getFavoriteAlbumsSync(String userId);
}