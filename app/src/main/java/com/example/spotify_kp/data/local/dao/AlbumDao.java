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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AlbumEntity album);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AlbumEntity> albums);

    @Update
    void update(AlbumEntity album);

    @Delete
    void delete(AlbumEntity album);

    @Query("SELECT * FROM albums ORDER BY created_at DESC")
    LiveData<List<AlbumEntity>> getAllAlbums();

    @Query("SELECT * FROM albums WHERE id = :albumId LIMIT 1")
    LiveData<AlbumEntity> getAlbumById(String albumId);

    @Query("SELECT * FROM albums WHERE genre = :genre ORDER BY year DESC")
    LiveData<List<AlbumEntity>> getAlbumsByGenre(String genre);

    @Query("SELECT * FROM albums WHERE year = :year ORDER BY title ASC")
    LiveData<List<AlbumEntity>> getAlbumsByYear(String year);

    @Query("SELECT * FROM albums WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%'")
    LiveData<List<AlbumEntity>> searchAlbums(String query);

    @Query("DELETE FROM albums")
    void deleteAll();
}