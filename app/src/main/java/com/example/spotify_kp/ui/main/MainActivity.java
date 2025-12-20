package com.example.spotify_kp.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.spotify_kp.R;
import com.example.spotify_kp.data.remote.RetrofitClient;
import com.example.spotify_kp.data.repository.AlbumRepository;
import com.example.spotify_kp.model.User;
import com.example.spotify_kp.ui.auth.LoginActivity;
import com.example.spotify_kp.ui.catalog.CatalogFragment;
import com.example.spotify_kp.ui.favorites.FavoritesFragment;
import com.example.spotify_kp.ui.newreleases.NewReleasesFragment;
import com.example.spotify_kp.utils.NetworkUtils;
import com.example.spotify_kp.utils.SharedPrefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView userName;
    private TextView greetingText;
    private CircleImageView profileImage;
    private ImageView settingsIcon;
    private BottomNavigationView bottomNavigation;

    // ✅ Офлайн индикатор
    private LinearLayout offlineIndicator;
    private TextView offlineText;

    private SharedPrefsManager prefsManager;
    private AlbumRepository albumRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefsManager = SharedPrefsManager.getInstance(this);
        albumRepository = new AlbumRepository(this);

        initViews();
        setupHeader();
        setupBottomNavigation();
        setupOfflineIndicator();

        // Загружаем CatalogFragment по умолчанию
        if (savedInstanceState == null) {
            loadFragment(new CatalogFragment());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateOfflineIndicator();
    }

    private void initViews() {
        View headerView = findViewById(R.id.headerProfile);
        userName = headerView.findViewById(R.id.userName);
        greetingText = headerView.findViewById(R.id.greetingText);
        profileImage = headerView.findViewById(R.id.profileImage);
        settingsIcon = headerView.findViewById(R.id.settingsIcon);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // ✅ Инициализация офлайн индикатора
        offlineIndicator = findViewById(R.id.offlineIndicator);
        offlineText = findViewById(R.id.offlineText);
    }

    private void setupHeader() {
        setGreeting();
        loadUserProfile();

        // Settings click - logout
        settingsIcon.setOnClickListener(v -> {
            prefsManager.logout();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_catalog) {
                fragment = new CatalogFragment();
            } else if (itemId == R.id.nav_new_releases) {
                fragment = new NewReleasesFragment();
            } else if (itemId == R.id.nav_favorites) {
                fragment = new FavoritesFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void setGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            greetingText.setText("Good morning");
        } else if (hour >= 12 && hour < 17) {
            greetingText.setText("Good afternoon");
        } else {
            greetingText.setText("Good evening");
        }
    }

    private void loadUserProfile() {
        Log.d(TAG, "Starting to load user profile...");

        // Сначала показываем данные из SharedPreferences
        String savedName = prefsManager.getUserName();
        String savedImage = prefsManager.getUserImage();

        if (savedName != null) {
            userName.setText(savedName);
        }

        if (savedImage != null && !savedImage.isEmpty()) {
            Glide.with(this)
                    .load(savedImage)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(profileImage);
        }

        // Затем обновляем с сервера (только если есть интернет)
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Log.d(TAG, "📶 Offline mode - using cached profile");
            return;
        }

        RetrofitClient.api().getUserProfile().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d(TAG, "Response received. Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Log.d(TAG, "User loaded: " + user.getDisplayName());

                    runOnUiThread(() -> {
                        userName.setText(user.getDisplayName());

                        String imageUrl = user.getImageUrl();
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Log.d(TAG, "Loading image: " + imageUrl);
                            Glide.with(MainActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .error(R.drawable.ic_profile_placeholder)
                                    .into(profileImage);
                        }

                        Log.d(TAG, "UI updated");
                    });
                } else {
                    Log.e(TAG, "Error loading profile: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Network failure: " + t.getMessage());
            }
        });
    }

    /**
     * ✅ Настройка индикатора офлайн-режима
     */
    private void setupOfflineIndicator() {
        if (offlineIndicator == null || offlineText == null) {
            Log.w(TAG, "⚠️ Offline indicator views not found");
            return;
        }

        updateOfflineIndicator();

        // Клик по индикатору для обновления
        offlineIndicator.setOnClickListener(v -> {
            if (NetworkUtils.isNetworkAvailable(this)) {
                Snackbar.make(v, "🔄 Refreshing data...", Snackbar.LENGTH_SHORT).show();

                // Перезагружаем текущий фрагмент
                Fragment currentFragment = getSupportFragmentManager()
                        .findFragmentById(R.id.fragmentContainer);

                if (currentFragment instanceof CatalogFragment) {
                    loadFragment(new CatalogFragment());
                }

                updateOfflineIndicator();
            } else {
                Snackbar.make(v, "📶 No internet connection", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ✅ Обновление индикатора офлайн-режима
     */
    private void updateOfflineIndicator() {
        if (offlineIndicator == null || offlineText == null) {
            return;
        }

        boolean isOnline = NetworkUtils.isNetworkAvailable(this);

        if (!isOnline) {
            // Офлайн режим
            offlineIndicator.setVisibility(View.VISIBLE);
            offlineIndicator.setBackgroundColor(Color.parseColor("#FF6B6B"));
            offlineText.setText("📶 Offline Mode - Tap to sync when online");
            Log.d(TAG, "📶 Offline mode indicator shown");
        } else {
            // Онлайн - проверяем когда была последняя синхронизация
            long lastSync = albumRepository.getLastSyncTime();

            if (lastSync == 0) {
                // Никогда не синхронизировали
                offlineIndicator.setVisibility(View.VISIBLE);
                offlineIndicator.setBackgroundColor(Color.parseColor("#FFA726"));
                offlineText.setText("⚠️ Tap to sync data");
            } else {
                // Данные есть - показываем время последней синхронизации
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                String lastSyncStr = sdf.format(new Date(lastSync));

                long timeSinceSync = System.currentTimeMillis() - lastSync;
                long hours = timeSinceSync / (1000 * 60 * 60);

                if (hours > 24) {
                    // Данные устарели
                    offlineIndicator.setVisibility(View.VISIBLE);
                    offlineIndicator.setBackgroundColor(Color.parseColor("#66BB6A"));
                    offlineText.setText("🔄 Last sync: " + lastSyncStr + " - Tap to update");
                } else {
                    // Данные свежие
                    offlineIndicator.setVisibility(View.GONE);
                }
            }
            Log.d(TAG, "✅ Online mode, last sync: " + new Date(lastSync));
        }
    }
}