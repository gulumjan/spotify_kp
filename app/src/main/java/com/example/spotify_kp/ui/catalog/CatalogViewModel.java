package com.example.spotify_kp.ui.catalog;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.data.repository.AlbumRepository;
import com.example.spotify_kp.utils.Resource;

import java.util.List;

public class CatalogViewModel extends AndroidViewModel {

    private AlbumRepository albumRepository;
    private MediatorLiveData<Resource<List<AlbumEntity>>> albums;
    private LiveData<Resource<List<AlbumEntity>>> currentSource;

    // Для фильтрации
    private String currentGenre = null;
    private String currentYear = null;

    public CatalogViewModel(@NonNull Application application) {
        super(application);
        albumRepository = new AlbumRepository(application);
        albums = new MediatorLiveData<>();
    }

    public LiveData<Resource<List<AlbumEntity>>> getAlbums() {
        return albums;
    }

    public void loadAlbums() {
        if (currentSource != null) {
            albums.removeSource(currentSource);
        }

        currentSource = albumRepository.loadAlbums();
        albums.addSource(currentSource, albums::setValue);
    }

    public void searchAlbums(String query) {
        if (currentSource != null) {
            albums.removeSource(currentSource);
        }

        currentSource = albumRepository.searchAlbums(query);
        albums.addSource(currentSource, albums::setValue);
    }

    public void filterByGenre(String genre) {
        if (currentSource != null) {
            albums.removeSource(currentSource);
        }

        currentGenre = genre;
        currentSource = albumRepository.getAlbumsByGenre(genre);
        albums.addSource(currentSource, albums::setValue);
    }

    public void filterByYear(String year) {
        if (currentSource != null) {
            albums.removeSource(currentSource);
        }

        currentYear = year;
        currentSource = albumRepository.getAlbumsByYear(year);
        albums.addSource(currentSource, albums::setValue);
    }

    public void clearFilters() {
        currentGenre = null;
        currentYear = null;
        loadAlbums();
    }

    public String getCurrentGenre() {
        return currentGenre;
    }

    public String getCurrentYear() {
        return currentYear;
    }

    public LiveData<List<AlbumEntity>> getAllAlbumsFromDb() {
        return albumRepository.getAllAlbumsFromDb();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentSource != null) {
            albums.removeSource(currentSource);
        }
    }
}