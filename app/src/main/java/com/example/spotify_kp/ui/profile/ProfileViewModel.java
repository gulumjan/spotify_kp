package com.example.spotify_kp.ui.profile;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.spotify_kp.data.local.AppDatabase;
import com.example.spotify_kp.data.repository.FavoriteRepository;
import com.example.spotify_kp.utils.SharedPrefsManager;

public class ProfileViewModel extends AndroidViewModel {

    private static final String TAG = "ProfileViewModel";

    private AppDatabase database;
    private FavoriteRepository favoriteRepository;
    private SharedPrefsManager prefsManager;

    private MutableLiveData<Integer> favoritesCount = new MutableLiveData<>(0);
    private MutableLiveData<Integer> albumsCount = new MutableLiveData<>(0);

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(application);
        favoriteRepository = new FavoriteRepository(application);
        prefsManager = SharedPrefsManager.getInstance(application);

        loadStats();
    }

    public LiveData<Integer> getFavoritesCount() {
        return favoritesCount;
    }

    public LiveData<Integer> getAlbumsCount() {
        return albumsCount;
    }

    private void loadStats() {
        new Thread(() -> {
            try {
                // Get favorites count
                int favCount = favoriteRepository.getFavoritesCountSync();
                favoritesCount.postValue(favCount);
                Log.d(TAG, "Favorites count: " + favCount);

                // Get total albums count
                int albumCount = database.albumDao().getAlbumsCount();
                albumsCount.postValue(albumCount);
                Log.d(TAG, "Albums count: " + albumCount);

            } catch (Exception e) {
                Log.e(TAG, "Error loading stats: " + e.getMessage(), e);
                favoritesCount.postValue(0);
                albumsCount.postValue(0);
            }
        }).start();
    }

    public void refreshStats() {
        Log.d(TAG, "Refreshing stats...");
        loadStats();
    }
}