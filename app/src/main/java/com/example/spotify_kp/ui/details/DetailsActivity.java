package com.example.spotify_kp.ui.details;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.spotify_kp.R;
import com.example.spotify_kp.data.remote.dto.AlbumDto;
import com.example.spotify_kp.data.repository.FavoriteRepository;
import com.example.spotify_kp.ui.details.adapter.TrackAdapter;
import com.example.spotify_kp.ui.favorites.dialog.AddToFavoriteDialog;
import com.example.spotify_kp.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = "DetailsActivity";

    private ImageView albumCover;
    private TextView albumTitle;
    private TextView artistName;
    private TextView albumInfo;
    private RecyclerView tracksRecyclerView;
    private ProgressBar progressBar;
    private FloatingActionButton fabFavorite;

    private DetailsViewModel viewModel;
    private FavoriteRepository favoriteRepository;
    private TrackAdapter trackAdapter;

    private String albumId;
    private String currentAlbumTitle;
    private String currentArtistName;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        albumId = getIntent().getStringExtra(Constants.KEY_ALBUM_ID);

        if (albumId == null) {
            Toast.makeText(this, "Album ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Opening album: " + albumId);

        initViews();
        setupViewModel();
        setupFavoriteRepository();
        setupObservers();

        viewModel.loadAlbumDetails(albumId);
    }

    private void initViews() {
        albumCover = findViewById(R.id.albumCover);
        albumTitle = findViewById(R.id.albumTitle);
        artistName = findViewById(R.id.artistName);
        albumInfo = findViewById(R.id.albumInfo);
        tracksRecyclerView = findViewById(R.id.tracksRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        fabFavorite = findViewById(R.id.fabFavorite);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        trackAdapter = new TrackAdapter();
        tracksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tracksRecyclerView.setAdapter(trackAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DetailsViewModel.class);
    }

    private void setupFavoriteRepository() {
        favoriteRepository = new FavoriteRepository(this);
    }

    private void setupObservers() {
        // Observe album details
        viewModel.getAlbumDetails().observe(this, resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        progressBar.setVisibility(View.VISIBLE);
                        break;

                    case SUCCESS:
                        progressBar.setVisibility(View.GONE);
                        if (resource.getData() != null) {
                            displayAlbumDetails(resource.getData());
                        }
                        break;

                    case ERROR:
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });

        // Observe favorite status
        favoriteRepository.isAlbumFavorite(albumId).observe(this, isFav -> {
            if (isFav != null) {
                isFavorite = isFav;
                updateFabIcon();
            }
        });
    }

    private void displayAlbumDetails(AlbumDto album) {
        currentAlbumTitle = album.getName();
        albumTitle.setText(currentAlbumTitle);

        // Artist
        if (album.getArtists() != null && !album.getArtists().isEmpty()) {
            currentArtistName = album.getArtists().get(0).getName();
            artistName.setText(currentArtistName);
        }

        // Year and track count
        String year = "Unknown";
        if (album.getReleaseDate() != null && album.getReleaseDate().length() >= 4) {
            year = album.getReleaseDate().substring(0, 4);
        }
        albumInfo.setText(year + " • " + album.getTotalTracks() + " tracks");

        // Cover
        if (album.getImages() != null && !album.getImages().isEmpty()) {
            Glide.with(this)
                    .load(album.getImages().get(0).getUrl())
                    .placeholder(R.drawable.ic_music)
                    .error(R.drawable.ic_music)
                    .into(albumCover);
        }

        // Tracks
        if (album.getTracks() != null && album.getTracks().getItems() != null) {
            trackAdapter.setTracks(album.getTracks().getItems());
        }

        setupFabListener();
    }

    private void setupFabListener() {
        fabFavorite.setOnClickListener(v -> {
            if (isFavorite) {
                // Remove from favorites
                favoriteRepository.removeFromFavorites(albumId);
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
            } else {
                // Show dialog to add to favorites
                showAddToFavoriteDialog();
            }
        });
    }

    private void showAddToFavoriteDialog() {
        AddToFavoriteDialog dialog = new AddToFavoriteDialog(
                this,
                currentAlbumTitle,
                currentArtistName,
                (comment, rating) -> {
                    favoriteRepository.addToFavorites(albumId, comment, rating);
                    Toast.makeText(this, "Added to favorites!", Toast.LENGTH_SHORT).show();
                }
        );
        dialog.show();
    }

    private void updateFabIcon() {
        if (isFavorite) {
            fabFavorite.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            fabFavorite.setImageResource(R.drawable.ic_favorite);
        }
    }
}