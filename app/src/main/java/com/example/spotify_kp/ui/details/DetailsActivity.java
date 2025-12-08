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
import com.example.spotify_kp.ui.details.adapter.TrackAdapter;
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
    private TrackAdapter trackAdapter;

    private String albumId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Получаем ID альбома из Intent
        albumId = getIntent().getStringExtra(Constants.KEY_ALBUM_ID);

        if (albumId == null) {
            Toast.makeText(this, "Album ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Opening album: " + albumId);

        // Инициализация Views
        albumCover = findViewById(R.id.albumCover);
        albumTitle = findViewById(R.id.albumTitle);
        artistName = findViewById(R.id.artistName);
        albumInfo = findViewById(R.id.albumInfo);
        tracksRecyclerView = findViewById(R.id.tracksRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        fabFavorite = findViewById(R.id.fabFavorite);

        // Back button
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // ViewModel
        viewModel = new ViewModelProvider(this).get(DetailsViewModel.class);

        // Adapter для треков
        trackAdapter = new TrackAdapter();
        tracksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tracksRecyclerView.setAdapter(trackAdapter);

        // Observer
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

        // Загрузка деталей альбома
        viewModel.loadAlbumDetails(albumId);

        // FAB для добавления в избранное
        fabFavorite.setOnClickListener(v -> {
            Toast.makeText(this, "Add to favorites", Toast.LENGTH_SHORT).show();
            // TODO: Открыть диалог добавления в избранное
        });
    }

    private void displayAlbumDetails(AlbumDto album) {
        albumTitle.setText(album.getName());

        // Артист
        if (album.getArtists() != null && !album.getArtists().isEmpty()) {
            artistName.setText(album.getArtists().get(0).getName());
        }

        // Год и количество треков
        String year = "Unknown";
        if (album.getReleaseDate() != null && album.getReleaseDate().length() >= 4) {
            year = album.getReleaseDate().substring(0, 4);
        }
        albumInfo.setText(year + " • " + album.getTotalTracks() + " tracks");

        // Обложка
        if (album.getImages() != null && !album.getImages().isEmpty()) {
            Glide.with(this)
                    .load(album.getImages().get(0).getUrl())
                    .placeholder(R.drawable.ic_music)
                    .error(R.drawable.ic_music)
                    .into(albumCover);
        }

        // Треки
        if (album.getTracks() != null && album.getTracks().getItems() != null) {
            trackAdapter.setTracks(album.getTracks().getItems());
        }
    }
}