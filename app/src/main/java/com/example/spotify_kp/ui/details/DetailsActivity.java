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

        Log.d(TAG, "🎵 Opening album: " + albumId);

        initViews();
        setupViewModel();
        setupFavoriteRepository();
        setupObservers();

        viewModel.loadAlbumDetails(albumId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "▶️ onResume - checking favorite status");

        // Перепроверяем статус при возврате на экран
        checkFavoriteStatus();
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

        backButton.setOnClickListener(v -> {
            Log.d(TAG, "⬅️ Back button pressed");
            finish();
        });
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

        // Проверяем статус избранного
        checkFavoriteStatus();
    }

    private void checkFavoriteStatus() {
        Log.d(TAG, "🔍 Checking favorite status");

        // СИНХРОННО проверяем статус
        isFavorite = favoriteRepository.isAlbumFavoriteSync(albumId);
        updateFabIcon();

        Log.d(TAG, "❤️ Is favorite: " + isFavorite);
    }

    private void displayAlbumDetails(AlbumEntity album) {
        currentAlbumTitle = album.getTitle();
        currentArtistName = album.getArtist();

        albumTitle.setText(currentAlbumTitle);
        artistName.setText(currentArtistName);

        String year = album.getYear() != null ? album.getYear() : "Unknown";
        albumInfo.setText(year + " • " + album.getTotalTracks() + " tracks • " + album.getGenre());

        // Загружаем обложку
        Glide.with(this)
                .load(album.getCoverUrl())
                .placeholder(R.drawable.ic_music)
                .error(R.drawable.ic_music)
                .into(albumCover);

        // Показываем секцию треков
        tracksHeader.setVisibility(View.VISIBLE);
        tracksContainer.setVisibility(View.VISIBLE);

        // Генерируем треки
        generateSampleTracks(album.getTotalTracks(), album.getArtist());

        // Настраиваем FAB
        setupFabListener();
    }

    private void generateSampleTracks(int trackCount, String artistName) {
        tracksContainer.removeAllViews();

        String[] trackNames = {
                "Lost Heart", "Wild Dreams", "Golden Lights", "Silent Soul", "Electric Fire",
                "Broken Time", "Midnight Rain", "Endless Stars", "Sacred Night", "Fallen Love"
        };

        for (int i = 1; i <= Math.min(trackCount, 10); i++) {
            View trackView = getLayoutInflater().inflate(R.layout.item_track_detailed, tracksContainer, false);

            TextView trackNumber = trackView.findViewById(R.id.trackNumber);
            TextView trackName = trackView.findViewById(R.id.trackName);
            TextView trackArtist = trackView.findViewById(R.id.trackArtist);
            TextView trackDuration = trackView.findViewById(R.id.trackDuration);

            trackNumber.setText(String.valueOf(i));
            trackName.setText(trackNames[i % trackNames.length]);
            trackArtist.setText(artistName);

            // Случайная длительность
            int minutes = 2 + (int)(Math.random() * 3);
            int seconds = (int)(Math.random() * 60);
            trackDuration.setText(String.format("%d:%02d", minutes, seconds));

            tracksContainer.addView(trackView);
        }
    }

    private void setupFabListener() {
        fabFavorite.setOnClickListener(v -> {
            if (isFavorite) {
                // Удалить из избранного
                Log.d(TAG, "🗑️ Removing from favorites");

                boolean success = favoriteRepository.removeFromFavoritesSync(albumId);

                if (success) {
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    checkFavoriteStatus();
                } else {
                    Toast.makeText(this, "Failed to remove", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Добавить в избранное
                Log.d(TAG, "➕ Adding to favorites");
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
                    Log.d(TAG, "💾 Saving favorite with rating: " + rating);

                    // СИНХРОННО сохраняем
                    boolean success = favoriteRepository.addToFavoritesSync(albumId, comment, rating);

                    if (success) {
                        Toast.makeText(this, "Added to favorites!", Toast.LENGTH_SHORT).show();
                        checkFavoriteStatus();
                    } else {
                        Toast.makeText(this, "Failed to add", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        dialog.show();
    }

    private void updateFabIcon() {
        Log.d(TAG, "🎨 Updating FAB icon. isFavorite: " + isFavorite);

        if (isFavorite) {
            fabFavorite.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            fabFavorite.setImageResource(R.drawable.ic_favorite);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "⏸️ onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "💀 onDestroy");
    }
}