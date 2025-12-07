package com.example.spotify_kp.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.spotify_kp.data.repository.AuthRepository;
import com.example.spotify_kp.model.User;
import com.example.spotify_kp.utils.Resource;

public class AuthViewModel extends AndroidViewModel {

    private AuthRepository authRepository;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    public LiveData<Resource<User>> login(String email, String password) {
        return authRepository.login(email, password);
    }

    public LiveData<Resource<User>> register(String displayName, String email, String password) {
        return authRepository.register(displayName, email, password);
    }

    public void logout() {
        authRepository.logout();
    }

    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }
}