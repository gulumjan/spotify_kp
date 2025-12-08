package com.example.spotify_kp.data.remote;

import com.example.spotify_kp.data.remote.dto.AlbumDto;
import com.example.spotify_kp.data.remote.dto.AlbumResponse;
import com.example.spotify_kp.data.remote.dto.LoginRequest;
import com.example.spotify_kp.data.remote.dto.RegisterRequest;
import com.example.spotify_kp.model.TrackResponse;
import com.example.spotify_kp.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SpotifyApi {

    // AUTH
    @POST("login")
    Call<User> login(@Body LoginRequest request);

    @POST("register")
    Call<User> register(@Body RegisterRequest request);

    @GET("me")
    Call<User> getUserProfile();

    // ALBUMS
    @GET("albums")
    Call<AlbumResponse> getAlbums(@Query("ids") String ids);

    @GET("albums/{id}")
    Call<AlbumDto> getAlbumById(@Path("id") String albumId);

    @GET("search")
    Call<AlbumResponse> searchAlbums(@Query("q") String query,
                                     @Query("type") String type,
                                     @Query("limit") int limit);

    // TRACKS
    @GET("tracks")
    Call<TrackResponse> getTracks();
}