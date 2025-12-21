package com.example.spotify_kp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.spotify_kp.R;
import com.example.spotify_kp.ui.auth.LoginActivity;
import com.example.spotify_kp.utils.SharedPrefsManager;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private CircleImageView profileImage;
    private TextView userName;
    private TextView userEmail;
    private TextView favoritesCount;
    private TextView albumsCount;
    private TextView joinedDate;

    private MaterialCardView statsCard;
    private LinearLayout favoritesSection;
    private LinearLayout albumsSection;

    private Button editProfileButton;
    private Button logoutButton;

    private ProfileViewModel viewModel;
    private SharedPrefsManager prefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        loadUserData();
        setupListeners();
        observeStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh stats when returning to profile
        viewModel.refreshStats();
    }

    private void initViews(View view) {
        profileImage = view.findViewById(R.id.profileImage);
        userName = view.findViewById(R.id.userName);
        userEmail = view.findViewById(R.id.userEmail);
        favoritesCount = view.findViewById(R.id.favoritesCount);
        albumsCount = view.findViewById(R.id.albumsCount);
        joinedDate = view.findViewById(R.id.joinedDate);

        statsCard = view.findViewById(R.id.statsCard);
        favoritesSection = view.findViewById(R.id.favoritesSection);
        albumsSection = view.findViewById(R.id.albumsSection);

        editProfileButton = view.findViewById(R.id.editProfileButton);
        logoutButton = view.findViewById(R.id.logoutButton);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        prefsManager = SharedPrefsManager.getInstance(requireContext());
    }

    private void loadUserData() {
        // Load from SharedPreferences
        String name = prefsManager.getUserName();
        String email = prefsManager.getUserEmail();
        String imageUrl = prefsManager.getUserImage();

        userName.setText(name != null ? name : "Music Lover");
        userEmail.setText(email != null ? email : "user@example.com");

        // Load profile image
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(profileImage);
        }

        // Set joined date
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        joinedDate.setText("Member since " + sdf.format(new Date()));
    }

    private void setupListeners() {
        editProfileButton.setOnClickListener(v -> showComingSoonDialog("Edit Profile"));

        logoutButton.setOnClickListener(v -> showLogoutConfirmation());

        favoritesSection.setOnClickListener(v -> {
            // Navigate to Favorites tab
            if (getActivity() instanceof com.example.spotify_kp.ui.main.MainActivity) {
                ((com.example.spotify_kp.ui.main.MainActivity) getActivity()).navigateToFavorites();
            }
        });
    }

    private void observeStats() {
        viewModel.getFavoritesCount().observe(getViewLifecycleOwner(), count -> {
            favoritesCount.setText(String.valueOf(count));
        });

        viewModel.getAlbumsCount().observe(getViewLifecycleOwner(), count -> {
            albumsCount.setText(String.valueOf(count));
        });
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setIcon(R.drawable.ic_settings)
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        prefsManager.logout();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showComingSoonDialog(String feature) {
        new AlertDialog.Builder(requireContext())
                .setTitle(feature)
                .setMessage("This feature is coming soon! Stay tuned for updates.")
                .setIcon(R.drawable.ic_music)
                .setPositiveButton("OK", null)
                .show();
    }
}