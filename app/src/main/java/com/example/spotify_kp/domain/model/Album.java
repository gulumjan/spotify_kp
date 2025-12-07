package com.example.spotify_kp.domain.model;

public class Album {
    private String id;
    private String title;
    private String artist;
    private String year;
    private String genre;
    private String coverUrl;
    private int totalTracks;
    private boolean isFavorite;
    private float userRating;

    public Album() {}

    public Album(String id, String title, String artist, String year,
                 String coverUrl, int totalTracks) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.coverUrl = coverUrl;
        this.totalTracks = totalTracks;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public float getUserRating() { return userRating; }
    public void setUserRating(float userRating) { this.userRating = userRating; }
}