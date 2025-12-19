package com.example.spotify_kp.ui.details;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.spotify_kp.R;
import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.data.repository.FavoriteRepository;
import com.example.spotify_kp.ui.favorites.dialog.AddToFavoriteDialog;
import com.example.spotify_kp.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = "DetailsActivity";

    private ImageView albumCover;
    private TextView albumTitle;
    private TextView artistName;
    private TextView albumInfo;
    private TextView tracksHeader;
    private LinearLayout tracksContainer;
    private ProgressBar progressBar;
    private FloatingActionButton fabFavorite;
    private ImageView backButton;

    private DetailsViewModel viewModel;
    private FavoriteRepository favoriteRepository;

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
        tracksHeader = findViewById(R.id.tracksHeader);
        tracksContainer = findViewById(R.id.tracksContainer);
        progressBar = findViewById(R.id.progressBar);
        fabFavorite = findViewById(R.id.fabFavorite);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());
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

    private void displayAlbumDetails(AlbumEntity album) {
        currentAlbumTitle = album.getTitle();
        currentArtistName = album.getArtist();

        albumTitle.setText(currentAlbumTitle);
        artistName.setText(currentArtistName);

        // Year and track count
        String year = album.getYear() != null ? album.getYear() : "Unknown";
        albumInfo.setText(year + " • " + album.getTotalTracks() + " tracks • " + album.getGenre());

        // Cover
        Glide.with(this)
                .load(album.getCoverUrl())
                .placeholder(R.drawable.ic_music)
                .error(R.drawable.ic_music)
                .into(albumCover);

        // Показываем секцию треков
        tracksHeader.setVisibility(View.VISIBLE);
        tracksContainer.setVisibility(View.VISIBLE);

        // Генерируем треки с названием альбома и исполнителя
        generateSampleTracks(album.getTotalTracks(), album.getTitle(), album.getArtist());

        setupFabListener();
    }

    private void generateSampleTracks(int trackCount, String albumTitle, String artistName) {
        tracksContainer.removeAllViews();

        // Списки слов для разных стилей названий
        String[] adjectives = {
                "Lost", "Wild", "Broken", "Golden", "Silent", "Electric", "Midnight",
                "Burning", "Endless", "Shining", "Frozen", "Sacred", "Fallen", "Rising"
        };

        String[] nouns = {
                "Heart", "Dreams", "Lights", "Soul", "Fire", "Rain", "Stars",
                "Night", "Love", "Time", "Hope", "Sky", "Ocean", "Memory"
        };

        String[] verbs = {
                "Dancing", "Running", "Falling", "Flying", "Waiting", "Breathing",
                "Chasing", "Dreaming", "Breaking", "Fading", "Shining", "Calling"
        };

        for (int i = 1; i <= trackCount; i++) {
            View trackView = getLayoutInflater().inflate(R.layout.item_track_detailed, tracksContainer, false);

            TextView trackNumber = trackView.findViewById(R.id.trackNumber);
            TextView trackName = trackView.findViewById(R.id.trackName);
            TextView trackArtist = trackView.findViewById(R.id.trackArtist);
            TextView trackDuration = trackView.findViewById(R.id.trackDuration);

            trackNumber.setText(String.valueOf(i));

            // Генерируем реалистичное название трека
            String trackTitle;
            int styleType = (int)(Math.random() * 4);

            switch (styleType) {
                case 0: // Прилагательное + Существительное
                    trackTitle = adjectives[(int)(Math.random() * adjectives.length)] + " " +
                            nouns[(int)(Math.random() * nouns.length)];
                    break;
                case 1: // Глагол + Существительное
                    trackTitle = verbs[(int)(Math.random() * verbs.length)] + " " +
                            nouns[(int)(Math.random() * nouns.length)];
                    break;
                case 2: // Просто существительное
                    trackTitle = nouns[(int)(Math.random() * nouns.length)];
                    break;
                default: // Прилагательное + Глагол
                    trackTitle = adjectives[(int)(Math.random() * adjectives.length)] + " " +
                            verbs[(int)(Math.random() * verbs.length)];
                    break;
            }

            trackName.setText(trackTitle);
            trackArtist.setText(artistName); // Показываем реального исполнителя

            // Генерируем случайную длительность трека (2-5 минут)
            int minutes = 2 + (int)(Math.random() * 4);
            int seconds = (int)(Math.random() * 60);
            trackDuration.setText(String.format("%d:%02d", minutes, seconds));

            tracksContainer.addView(trackView);
        }
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