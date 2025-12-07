package com.example.spotify_kp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "display_name")
    private String displayName;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "image_url")
    private String imageUrl;

    @ColumnInfo(name = "spotify_uri")
    private String spotifyUri;

    @ColumnInfo(name = "followers_count")
    private int followersCount;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public UserEntity() {}

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSpotifyUri() { return spotifyUri; }
    public void setSpotifyUri(String spotifyUri) { this.spotifyUri = spotifyUri; }

    public int getFollowersCount() { return followersCount; }
    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}