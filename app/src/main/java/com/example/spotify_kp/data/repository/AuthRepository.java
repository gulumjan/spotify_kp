package com.example.spotify_kp.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.spotify_kp.data.remote.RetrofitClient;
import com.example.spotify_kp.data.local.AppDatabase;
import com.example.spotify_kp.data.local.entity.UserEntity;
import com.example.spotify_kp.data.remote.dto.LoginRequest;
import com.example.spotify_kp.data.remote.dto.RegisterRequest;
import com.example.spotify_kp.model.User;
import com.example.spotify_kp.utils.Resource;
import com.example.spotify_kp.utils.SharedPrefsManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private static final String TAG = "AuthRepository";

    private AppDatabase database;
    private SharedPrefsManager prefsManager;

    public AuthRepository(Context context) {
        this.database = AppDatabase.getInstance(context);
        this.prefsManager = SharedPrefsManager.getInstance(context);
    }

    // Login
    public LiveData<Resource<User>> login(String email, String password) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        LoginRequest request = new LoginRequest(email, password);

        RetrofitClient.api().login(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();

                    // Сохраняем в SharedPreferences
                    String imageUrl = user.getImageUrl();
                    prefsManager.saveLoginData(
                            "fake_token_" + System.currentTimeMillis(),
                            user.getId(),
                            user.getDisplayName(),
                            email,
                            imageUrl != null ? imageUrl : ""
                    );

                    // Сохраняем в Room (в фоновом потоке)
                    saveUserToDatabase(user, email);

                    result.setValue(Resource.success(user));
                    Log.d(TAG, "Login successful: " + user.getDisplayName());
                } else {
                    result.setValue(Resource.error("Invalid credentials", null));
                    Log.e(TAG, "Login failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage(), null));
                Log.e(TAG, "Login error: " + t.getMessage());
            }
        });

        return result;
    }

    // Register
    public LiveData<Resource<User>> register(String displayName, String email, String password) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        RegisterRequest request = new RegisterRequest(displayName, email, password);

        RetrofitClient.api().register(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();

                    // Сохраняем в SharedPreferences
                    String imageUrl = user.getImageUrl();
                    prefsManager.saveLoginData(
                            "fake_token_" + System.currentTimeMillis(),
                            user.getId(),
                            user.getDisplayName(),
                            email,
                            imageUrl != null ? imageUrl : ""
                    );

                    // Сохраняем в Room
                    saveUserToDatabase(user, email);

                    result.setValue(Resource.success(user));
                    Log.d(TAG, "Registration successful: " + user.getDisplayName());
                } else {
                    result.setValue(Resource.error("Registration failed", null));
                    Log.e(TAG, "Registration failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage(), null));
                Log.e(TAG, "Registration error: " + t.getMessage());
            }
        });

        return result;
    }

    // Сохранение пользователя в Room
    private void saveUserToDatabase(User user, String email) {
        new Thread(() -> {
            UserEntity entity = new UserEntity();
            entity.setId(user.getId());
            entity.setDisplayName(user.getDisplayName());
            entity.setEmail(email);
            entity.setImageUrl(user.getImageUrl());
            entity.setSpotifyUri(user.getUri());
            entity.setCreatedAt(System.currentTimeMillis());

            database.userDao().insert(entity);
            Log.d(TAG, "User saved to database");
        }).start();
    }

    // Logout
    public void logout() {
        prefsManager.logout();
        new Thread(() -> {
            database.userDao().deleteAll();
            Log.d(TAG, "User logged out");
        }).start();
    }

    // Проверка авторизации
    public boolean isLoggedIn() {
        return prefsManager.isLoggedIn();
    }
}