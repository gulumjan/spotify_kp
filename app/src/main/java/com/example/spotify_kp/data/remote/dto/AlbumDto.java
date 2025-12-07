package com.example.spotify_kp.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AlbumDto {
    private String id;
    private String name;

    @SerializedName("album_type")
    private String albumType;

    @SerializedName("total_tracks")
    private int totalTracks;

    @SerializedName("release_date")
    private String releaseDate;

    private List<ImageDto> images;
    private List<ArtistDto> artists;
    private TrackDto tracks;

    public AlbumDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAlbumType() { return albumType; }
    public void setAlbumType(String albumType) { this.albumType = albumType; }

    public int getTotalTracks() { return totalTracks; }
    public void setTotalTracks(int totalTracks) { this.totalTracks = totalTracks; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public List<ImageDto> getImages() { return images; }
    public void setImages(List<ImageDto> images) { this.images = images; }

    public List<ArtistDto> getArtists() { return artists; }
    public void setArtists(List<ArtistDto> artists) { this.artists = artists; }

    public TrackDto getTracks() { return tracks; }
    public void setTracks(TrackDto tracks) { this.tracks = tracks; }
}