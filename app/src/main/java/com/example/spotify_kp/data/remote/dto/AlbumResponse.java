package com.example.spotify_kp.data.remote.dto;

import java.util.List;

public class AlbumResponse {
    private List<AlbumDto> albums;

    public AlbumResponse() {}

    public List<AlbumDto> getAlbums() {
        return albums;
    }

    public void setAlbums(List<AlbumDto> albums) {
        this.albums = albums;
    }
}