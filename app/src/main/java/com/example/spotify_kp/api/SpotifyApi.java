package com.example.spotify_kp.api;

import com.example.spotify_kp.model.TrackResponse;
import com.example.spotify_kp.model.User;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SpotifyApi {

    @GET("tracks")
    Call<TrackResponse> getTracks();


    @GET("me")
    Call<User> getUserProfile();
}