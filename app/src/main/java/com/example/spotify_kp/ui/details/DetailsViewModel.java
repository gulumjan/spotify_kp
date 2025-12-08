package com.example.spotify_kp.ui.details;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.spotify_kp.data.remote.dto.AlbumDto;
import com.example.spotify_kp.data.repository.AlbumRepository;
import com.example.spotify_kp.utils.Resource;

public class DetailsViewModel extends AndroidViewModel {

    private AlbumRepository albumRepository;
    private MutableLiveData<Resource<AlbumDto>> albumDetails;

    public DetailsViewModel(@NonNull Application application) {
        super(application);
        albumRepository = new AlbumRepository(application);
        albumDetails = new MutableLiveData<>();
    }

    public LiveData<Resource<AlbumDto>> getAlbumDetails() {
        return albumDetails;
    }

    public void loadAlbumDetails(String albumId) {
        albumRepository.getAlbumDetails(albumId).observeForever(resource -> {
            albumDetails.setValue(resource);
        });
    }
}