package com.example.spotify_kp.ui.newreleases;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.data.repository.NewReleasesRepository;
import com.example.spotify_kp.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class NewReleasesViewModel extends AndroidViewModel {

    private NewReleasesRepository repository;
    private MutableLiveData<Resource<List<AlbumEntity>>> newReleases;

    private int currentOffset = 0;
    private static final int PAGE_SIZE = 10;
    private boolean isLoading = false;

    public NewReleasesViewModel(@NonNull Application application) {
        super(application);
        repository = new NewReleasesRepository(application);
        newReleases = new MutableLiveData<>();
    }

    public LiveData<Resource<List<AlbumEntity>>> getNewReleases() {
        return newReleases;
    }

    public void loadNewReleases() {
        currentOffset = 0;
        isLoading = true;

        repository.loadNewReleases(PAGE_SIZE, currentOffset).observeForever(resource -> {
            newReleases.setValue(resource);
            isLoading = false;
        });
    }

    public void loadNextPage() {
        if (isLoading) return;

        isLoading = true;
        currentOffset += PAGE_SIZE;

        repository.loadNewReleases(PAGE_SIZE, currentOffset).observeForever(resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                // Добавляем новые данные к существующим
                List<AlbumEntity> currentData = newReleases.getValue() != null
                        ? newReleases.getValue().getData()
                        : new ArrayList<>();

                if (currentData == null) {
                    currentData = new ArrayList<>();
                }

                List<AlbumEntity> combined = new ArrayList<>(currentData);
                combined.addAll(resource.getData());

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
}