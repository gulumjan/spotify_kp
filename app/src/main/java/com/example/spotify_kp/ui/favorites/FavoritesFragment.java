package com.example.spotify_kp.ui.favorites;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotify_kp.R;
import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.data.local.entity.FavoriteEntity;
import com.example.spotify_kp.ui.details.DetailsActivity;
import com.example.spotify_kp.ui.favorites.adapter.FavoriteAdapter;
import com.example.spotify_kp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment implements FavoriteAdapter.OnFavoriteClickListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private TextView emptyText;

    private FavoritesViewModel viewModel;
    private FavoriteAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupRecyclerView();
        observeFavorites();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.emptyState);
        emptyText = view.findViewById(R.id.emptyText);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new FavoriteAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void observeFavorites() {
        showLoading();

        viewModel.getFavorites().observe(getViewLifecycleOwner(), favorites -> {
            hideLoading();

            if (favorites == null || favorites.isEmpty()) {
                showEmpty();
            } else {
                showContent();
                loadAlbumsForFavorites(favorites);
            }
        });
    }

    private void loadAlbumsForFavorites(List<FavoriteEntity> favorites) {
        List<String> albumIds = new ArrayList<>();
        for (FavoriteEntity fav : favorites) {
            albumIds.add(fav.getAlbumId());
        }

        viewModel.getAlbumsByIds(albumIds).observe(getViewLifecycleOwner(), albums -> {
            if (albums != null) {
                adapter.setFavorites(favorites, albums);
            }
        });
    }

    @Override
    public void onFavoriteClick(FavoriteEntity favorite, AlbumEntity album) {
        Intent intent = new Intent(getContext(), DetailsActivity.class);
        intent.putExtra(Constants.KEY_ALBUM_ID, album.getId());
        startActivity(intent);
    }

    @Override
    public void onRemoveFavorite(FavoriteEntity favorite) {
        viewModel.removeFavorite(favorite.getAlbumId());
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showContent() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
    }

    private void showEmpty() {
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        emptyText.setText("No favorites yet\n\nStart adding albums to your favorites!");
    }
}