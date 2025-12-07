package com.example.spotify_kp.ui.catalog;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.data.repository.AlbumRepository;
import com.example.spotify_kp.utils.Resource;

import java.util.List;

public class CatalogViewModel extends AndroidViewModel {

    private AlbumRepository albumRepository;
    private MutableLiveData<Resource<List<AlbumEntity>>> albums;

    public CatalogViewModel(@NonNull Application application) {
        super(application);
        albumRepository = new AlbumRepository(application);
        albums = new MutableLiveData<>();
    }

    public LiveData<Resource<List<AlbumEntity>>> getAlbums() {
        return albums;
    }

    public void loadAlbums() {
        albumRepository.loadAlbums().observeForever(resource -> {
            albums.setValue(resource);
        });
    }

    public void searchAlbums(String query) {
        albumRepository.searchAlbums(query).observeForever(resource -> {
            albums.setValue(resource);
        });
    }

    public LiveData<List<AlbumEntity>> getAllAlbumsFromDb() {
        return albumRepository.getAllAlbumsFromDb();
    }
}