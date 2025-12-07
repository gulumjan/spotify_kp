package com.example.spotify_kp.data.remote.dto;

import java.util.List;

public class TrackDto {
    private List<TrackItemDto> items;
    private int total;

    public void TracksDto() {}

    public List<TrackItemDto> getItems() { return items; }
    public void setItems(List<TrackItemDto> items) { this.items = items; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}