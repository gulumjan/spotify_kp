package com.example.spotify_kp.ui.newreleases;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.data.repository.AlbumRepository;
import com.example.spotify_kp.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class NewReleasesViewModel extends AndroidViewModel {

    private AlbumRepository repository;
    private MediatorLiveData<Resource<List<AlbumEntity>>> newReleases;
    private LiveData<Resource<List<AlbumEntity>>> currentSource;

    private int currentOffset = 0;
    private static final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private List<AlbumEntity> allLoadedReleases = new ArrayList<>();

    public NewReleasesViewModel(@NonNull Application application) {
        super(application);
        repository = new AlbumRepository(application);
        newReleases = new MediatorLiveData<>();
    }

    public LiveData<Resource<List<AlbumEntity>>> getNewReleases() {
        return newReleases;
    }

    public void loadNewReleases() {
        currentOffset = 0;
        allLoadedReleases.clear();
        isLoading = true;

        if (currentSource != null) {
            newReleases.removeSource(currentSource);
        }

        currentSource = repository.loadNewReleases(PAGE_SIZE, currentOffset);
        newReleases.addSource(currentSource, resource -> {
            if (resource != null && resource.getStatus() == Resource.Status.SUCCESS) {
                if (resource.getData() != null) {
                    allLoadedReleases = new ArrayList<>(resource.getData());
                }
            }
            newReleases.setValue(resource);
            isLoading = false;
        });
    }

    public void loadNextPage() {
        if (isLoading) return;

        isLoading = true;
        currentOffset += PAGE_SIZE;

        if (currentSource != null) {
            newReleases.removeSource(currentSource);
        }

        currentSource = repository.loadNewReleases(PAGE_SIZE, currentOffset);
        newReleases.addSource(currentSource, resource -> {
            if (resource != null && resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                List<AlbumEntity> combined = new ArrayList<>(allLoadedReleases);
                combined.addAll(resource.getData());
                allLoadedReleases = combined;
                newReleases.setValue(Resource.success(combined));
            } else {
                newReleases.setValue(resource);
            }
            isLoading = false;
        });
    }

    public boolean isLoading() {
        return isLoading;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentSource != null) {
            newReleases.removeSource(currentSource);
        }
    }
}