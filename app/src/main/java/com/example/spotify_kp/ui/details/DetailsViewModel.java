package com.example.spotify_kp.ui.details;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.data.repository.AlbumRepository;
import com.example.spotify_kp.utils.Resource;

public class DetailsViewModel extends AndroidViewModel {

    private AlbumRepository albumRepository;
    private MediatorLiveData<Resource<AlbumEntity>> albumDetails;
    private LiveData<Resource<AlbumEntity>> currentSource;

    public DetailsViewModel(@NonNull Application application) {
        super(application);
        albumRepository = new AlbumRepository(application);
        albumDetails = new MediatorLiveData<>();
    }

    public LiveData<Resource<AlbumEntity>> getAlbumDetails() {
        return albumDetails;
    }

    public void loadAlbumDetails(String albumId) {
        if (currentSource != null) {
            albumDetails.removeSource(currentSource);
        }

        currentSource = albumRepository.getAlbumDetailsFromDb(albumId);
        albumDetails.addSource(currentSource, albumDetails::setValue);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentSource != null) {
            albumDetails.removeSource(currentSource);
        }
    }
}