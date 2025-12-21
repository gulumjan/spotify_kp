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
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.spotify_kp.R;
import com.example.spotify_kp.data.remote.RetrofitClient;
import com.example.spotify_kp.data.repository.AlbumRepository;
import com.example.spotify_kp.model.User;
import com.example.spotify_kp.ui.auth.LoginActivity;
import com.example.spotify_kp.ui.catalog.CatalogFragment;
import com.example.spotify_kp.ui.favorites.FavoritesFragment;
import com.example.spotify_kp.ui.newreleases.NewReleasesFragment;
import com.example.spotify_kp.ui.profile.ProfileFragment;
import com.example.spotify_kp.utils.NetworkUtils;
import com.example.spotify_kp.utils.SharedPrefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 🔥 ФИНАЛЬНАЯ ВЕРСИЯ MainActivity
 *
 * Ключевые исправления:
 * 1. SharedViewModel создаётся ОДИН РАЗ
 * 2. Фрагменты КЕШИРУЮТСЯ и переиспользуются
 * 3. При навигации фрагменты не уничтожаются, а скрываются/показываются
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView userName;
    private TextView greetingText;
    private CircleImageView profileImage;
    private ImageView settingsIcon;
    private BottomNavigationView bottomNavigation;

    private LinearLayout offlineIndicator;
    private TextView offlineText;

    private SharedPrefsManager prefsManager;
    private AlbumRepository albumRepository;

    // 🔥 КРИТИЧНО: Один SharedViewModel для всего приложения
    private SharedViewModel sharedViewModel;

    // 🔥 КРИТИЧНО: Кеш фрагментов - создаём ОДИН РАЗ и переиспользуем
    private Map<Integer, Fragment> fragmentCache = new HashMap<>();
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "🚀 MainActivity onCreate");

        prefsManager = SharedPrefsManager.getInstance(this);
        albumRepository = new AlbumRepository(this);

        // 🔥 КРИТИЧНО: Создаём SharedViewModel ОДИН РАЗ при создании Activity
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        Log.d(TAG, "✅ SharedViewModel created - hashCode: " + sharedViewModel.hashCode());

        initViews();
        setupHeader();
        setupBottomNavigation();
        setupOfflineIndicator();

        // Загружаем CatalogFragment по умолчанию
        if (savedInstanceState == null) {
            showFragment(R.id.nav_catalog);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateOfflineIndicator();

        Log.d(TAG, "▶️ MainActivity onResume");
        Log.d(TAG, "📊 SharedViewModel hashCode: " + sharedViewModel.hashCode());
        Log.d(TAG, "📊 Favorites count: " + sharedViewModel.getFavoritesCount());
    }

    private void initViews() {
        View headerView = findViewById(R.id.headerProfile);
        userName = headerView.findViewById(R.id.userName);
        greetingText = headerView.findViewById(R.id.greetingText);
        profileImage = headerView.findViewById(R.id.profileImage);
        settingsIcon = headerView.findViewById(R.id.settingsIcon);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        offlineIndicator = findViewById(R.id.offlineIndicator);
        offlineText = findViewById(R.id.offlineText);
    }

    private void setupHeader() {
        setGreeting();
        loadUserProfile();

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
            int itemId = item.getItemId();
            Log.d(TAG, "📱 Bottom navigation clicked: " + itemId);

            showFragment(itemId);
            return true;
        });
    }

    /**
     * 🔥 КЛЮЧЕВОЙ МЕТОД: Показывает фрагмент из кеша или создаёт новый
     * Фрагменты НЕ уничтожаются, а скрываются/показываются
     */
    private void showFragment(int menuItemId) {
        // Получаем фрагмент из кеша или создаём новый
        Fragment fragment = fragmentCache.get(menuItemId);

        if (fragment == null) {
            // Создаём фрагмент ОДИН РАЗ
            if (menuItemId == R.id.nav_catalog) {
                fragment = new CatalogFragment();
                Log.d(TAG, "➕ Created NEW CatalogFragment");
            } else if (menuItemId == R.id.nav_new_releases) {
                fragment = new NewReleasesFragment();
                Log.d(TAG, "➕ Created NEW NewReleasesFragment");
            } else if (menuItemId == R.id.nav_favorites) {
                fragment = new FavoritesFragment();
                Log.d(TAG, "➕ Created NEW FavoritesFragment");
            } else if (menuItemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
                Log.d(TAG, "➕ Created NEW ProfileFragment");
            }

            // Сохраняем в кеш
            if (fragment != null) {
                fragmentCache.put(menuItemId, fragment);
            }
        } else {
            Log.d(TAG, "♻️ Reusing cached fragment: " + fragment.getClass().getSimpleName());
        }

        if (fragment == null) {
            return;
        }

        // 🔥 КЛЮЧЕВОЕ ИЗМЕНЕНИЕ: Используем show/hide вместо replace
        // Это сохраняет фрагменты в памяти и НЕ уничтожает их ViewModels
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Скрываем текущий фрагмент
        if (currentFragment != null && currentFragment != fragment) {
            transaction.hide(currentFragment);
            Log.d(TAG, "👁️ Hidden: " + currentFragment.getClass().getSimpleName());
        }

        // Показываем нужный фрагмент
        if (fragment.isAdded()) {
            // Фрагмент уже добавлен - просто показываем
            transaction.show(fragment);
            Log.d(TAG, "👁️ Shown: " + fragment.getClass().getSimpleName());
        } else {
            // Фрагмент ещё не добавлен - добавляем
            transaction.add(R.id.fragmentContainer, fragment);
            Log.d(TAG, "➕ Added: " + fragment.getClass().getSimpleName());
        }

        transaction.commit();
        currentFragment = fragment;

        Log.d(TAG, "✅ Current fragment: " + currentFragment.getClass().getSimpleName());
    }

    /**
     * Получить SharedViewModel
     */
    public SharedViewModel getSharedViewModel() {
        return sharedViewModel;
    }

    /**
     * Navigate to Favorites tab
     */
    public void navigateToFavorites() {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_favorites);
        }
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

        if (!NetworkUtils.isNetworkAvailable(this)) {
            return;
        }

        RetrofitClient.api().getUserProfile().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    runOnUiThread(() -> {
                        userName.setText(user.getDisplayName());
                        String imageUrl = user.getImageUrl();
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(MainActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .error(R.drawable.ic_profile_placeholder)
                                    .into(profileImage);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Network failure: " + t.getMessage());
            }
        });
    }

    private void setupOfflineIndicator() {
        if (offlineIndicator == null || offlineText == null) {
            return;
        }

        updateOfflineIndicator();

        offlineIndicator.setOnClickListener(v -> {
            if (NetworkUtils.isNetworkAvailable(this)) {
                Snackbar.make(v, "🔄 Refreshing data...", Snackbar.LENGTH_SHORT).show();
                updateOfflineIndicator();
            } else {
                Snackbar.make(v, "📶 No internet connection", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOfflineIndicator() {
        if (offlineIndicator == null || offlineText == null) {
            return;
        }

        boolean isOnline = NetworkUtils.isNetworkAvailable(this);

        if (!isOnline) {
            offlineIndicator.setVisibility(View.VISIBLE);
            offlineIndicator.setBackgroundColor(Color.parseColor("#FF6B6B"));
            offlineText.setText("📶 Offline Mode");
        } else {
            long lastSync = albumRepository.getLastSyncTime();
            if (lastSync == 0) {
                offlineIndicator.setVisibility(View.VISIBLE);
                offlineIndicator.setBackgroundColor(Color.parseColor("#FFA726"));
                offlineText.setText("⚠️ Tap to sync data");
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                String lastSyncStr = sdf.format(new Date(lastSync));
                long hours = (System.currentTimeMillis() - lastSync) / (1000 * 60 * 60);

                if (hours > 24) {
                    offlineIndicator.setVisibility(View.VISIBLE);
                    offlineIndicator.setBackgroundColor(Color.parseColor("#66BB6A"));
                    offlineText.setText("🔄 Last sync: " + lastSyncStr);
                } else {
                    offlineIndicator.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "💀 MainActivity destroyed");
        Log.d(TAG, "💀 Fragment cache cleared: " + fragmentCache.size() + " fragments");
    }
}