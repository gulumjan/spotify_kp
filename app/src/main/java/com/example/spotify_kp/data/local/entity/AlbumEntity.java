package com.example.spotify_kp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "albums")
public class AlbumEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "artist")
    private String artist;

    @ColumnInfo(name = "year")
    private String year;

    @ColumnInfo(name = "genre")
    private String genre;

    @ColumnInfo(name = "cover_url")
    private String coverUrl;

    @ColumnInfo(name = "total_tracks")
    private int totalTracks;

    @ColumnInfo(name = "release_date")
    private String releaseDate;

    @ColumnInfo(name = "spotify_id")
    private String spotifyId;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public AlbumEntity() {}

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public int getTotalTracks() { return totalTracks; }
    public void setTotalTracks(int totalTracks) { this.totalTracks = totalTracks; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getSpotifyId() { return spotifyId; }
    public void setSpotifyId(String spotifyId) { this.spotifyId = spotifyId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}