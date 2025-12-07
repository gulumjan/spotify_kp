package com.example.spotify_kp.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.spotify_kp.R;
import com.example.spotify_kp.data.remote.RetrofitClient;
import com.example.spotify_kp.model.User;
import com.example.spotify_kp.ui.catalog.CatalogFragment;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView userName;
    private TextView greetingText;
    private CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View headerView = findViewById(R.id.headerProfile);

        userName = headerView.findViewById(R.id.userName);
        greetingText = headerView.findViewById(R.id.greetingText);
        profileImage = headerView.findViewById(R.id.profileImage);

        setGreeting();
        loadUserProfile();

        // Загружаем CatalogFragment
        if (savedInstanceState == null) {
            loadFragment(new CatalogFragment());
        }
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
                        } else {
                            Log.d(TAG, "No image URL available");
                        }

                        Log.d(TAG, "UI updated");
                    });
                } else {
                    Log.e(TAG, "Error loading profile: " + response.code());
                    userName.setText("Error");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Network failure: " + t.getMessage());
                userName.setText("Connection error");
                t.printStackTrace();
            }
        });
    }
}