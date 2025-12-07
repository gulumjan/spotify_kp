package com.example.spotify_kp.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "favorites",
        foreignKeys = @ForeignKey(
                entity = AlbumEntity.class,
                parentColumns = "id",
                childColumns = "album_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class FavoriteEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "album_id")
    private String albumId;

    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "user_comment")
    private String userComment;

    @ColumnInfo(name = "user_rating")
    private float userRating;

    @ColumnInfo(name = "added_date")
    private long addedDate;

    @ColumnInfo(name = "is_favorite")
    private boolean isFavorite;

    public FavoriteEntity() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAlbumId() { return albumId; }
    public void setAlbumId(String albumId) { this.albumId = albumId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserComment() { return userComment; }
    public void setUserComment(String userComment) { this.userComment = userComment; }

    public float getUserRating() { return userRating; }
    public void setUserRating(float userRating) { this.userRating = userRating; }

    public long getAddedDate() { return addedDate; }
    public void setAddedDate(long addedDate) { this.addedDate = addedDate; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}