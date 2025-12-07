package com.example.spotify_kp.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class TrackItemDto {
    private String id;
    private String name;

    @SerializedName("track_number")
    private int trackNumber;

    @SerializedName("duration_ms")
    private int durationMs;

    public TrackItemDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTrackNumber() { return trackNumber; }
    public void setTrackNumber(int trackNumber) { this.trackNumber = trackNumber; }

    public int getDurationMs() { return durationMs; }
    public void setDurationMs(int durationMs) { this.durationMs = durationMs; }
}