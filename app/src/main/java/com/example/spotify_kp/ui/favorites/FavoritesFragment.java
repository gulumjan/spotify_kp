package com.example.spotify_kp.ui.favorites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotify_kp.R;
import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.data.local.entity.FavoriteEntity;
import com.example.spotify_kp.data.repository.FavoriteRepository;
import com.example.spotify_kp.ui.details.DetailsActivity;
import com.example.spotify_kp.ui.favorites.adapter.FavoriteAdapter;
import com.example.spotify_kp.ui.favorites.dialog.EditFavoriteDialog;
import com.example.spotify_kp.ui.main.SharedViewModel;
import com.example.spotify_kp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment implements FavoriteAdapter.OnFavoriteClickListener {

    private static final String TAG = "FavoritesFragment";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private TextView emptyText;

    private SharedViewModel sharedViewModel; // 🔥 Используем SharedViewModel из MainActivity
    private FavoriteAdapter adapter;
    private FavoriteRepository favoriteRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "🎨 onCreateView");
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "🔧 onViewCreated");

        initViews(view);
        setupSharedViewModel(); // 🔥 КРИТИЧНО: Используем SharedViewModel
        setupFavoriteRepository();
        setupRecyclerView();
        observeFavorites();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "▶️ onResume - FORCE RELOAD from DB");

        // 🔥 КРИТИЧНО: При каждом показе фрагмента перезагружаем данные из БД
        if (sharedViewModel != null) {
            sharedViewModel.loadFavorites();
        }
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.emptyState);
        emptyText = view.findViewById(R.id.emptyText);
    }

    private void setupSharedViewModel() {
        // 🔥 КРИТИЧНО: Используем SharedViewModel из MainActivity
        // Этот ViewModel живёт пока жива MainActivity и хранит данные между переключениями вкладок
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        Log.d(TAG, "✅ SharedViewModel connected - hashCode: " + sharedViewModel.hashCode());
    }

    private void setupFavoriteRepository() {
        favoriteRepository = new FavoriteRepository(requireContext());
    }

    private void setupRecyclerView() {
        adapter = new FavoriteAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "✅ RecyclerView setup complete");
    }

    private void observeFavorites() {
        showLoading();

        // 🔥 Подписываемся на данные из SharedViewModel
        sharedViewModel.getFavorites().observe(getViewLifecycleOwner(), favorites -> {
            Log.d(TAG, "📊 Favorites LiveData updated: " + (favorites != null ? favorites.size() : 0));

            hideLoading();

            if (favorites == null || favorites.isEmpty()) {
                Log.d(TAG, "📭 No favorites found - showing empty state");
                showEmpty();
            } else {
                Log.d(TAG, "📚 Showing " + favorites.size() + " favorites");
                showContent();
                loadAlbumsForFavorites(favorites);
            }
        });
    }

    private void loadAlbumsForFavorites(List<FavoriteEntity> favorites) {
        Log.d(TAG, "🔄 Loading albums for " + favorites.size() + " favorites");

        // Собираем ID альбомов
        List<String> albumIds = new ArrayList<>();
        for (FavoriteEntity fav : favorites) {
            albumIds.add(fav.getAlbumId());
            Log.d(TAG, "  - Album ID: " + fav.getAlbumId());
        }

        // Загружаем альбомы через SharedViewModel
        sharedViewModel.getAlbumsByIds(albumIds).observe(getViewLifecycleOwner(), albums -> {
            Log.d(TAG, "💿 Albums loaded: " + (albums != null ? albums.size() : 0));

            if (albums != null && !albums.isEmpty()) {
                adapter.setFavorites(favorites, albums);
                showContent();
                Log.d(TAG, "✅ Adapter updated with data");
            } else {
                Log.w(TAG, "⚠️ No albums loaded for favorites");
            }
        });
    }

    @Override
    public void onFavoriteClick(FavoriteEntity favorite, AlbumEntity album) {
        Log.d(TAG, "👆 Favorite clicked: " + album.getTitle());
        Intent intent = new Intent(getContext(), DetailsActivity.class);
        intent.putExtra(Constants.KEY_ALBUM_ID, album.getId());
        startActivity(intent);
    }

    @Override
    public void onRemoveFavorite(FavoriteEntity favorite) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove from Favorites")
                .setMessage("Are you sure you want to remove this album from favorites?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    Log.d(TAG, "🗑️ User confirmed removal of: " + favorite.getAlbumId());

                    // 🔥 Удаляем через SharedViewModel - он сохранит изменения в БД
                    sharedViewModel.removeFromFavorites(favorite.getAlbumId());

                    Toast.makeText(getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEditFavorite(FavoriteEntity favorite, AlbumEntity album) {
        EditFavoriteDialog dialog = new EditFavoriteDialog(
                requireContext(),
                album.getTitle(),
                album.getArtist(),
                favorite.getUserComment(),
                favorite.getUserRating(),
                (comment, rating) -> {
                    Log.d(TAG, "✏️ User updated favorite: " + favorite.getAlbumId());

                    // Обновляем через Repository
                    new Thread(() -> {
                        favoriteRepository.updateFavoriteSync(favorite.getAlbumId(), comment, rating);

                        // Перезагружаем список через SharedViewModel
                        requireActivity().runOnUiThread(() -> {
                            sharedViewModel.loadFavorites();
                            Toast.makeText(getContext(), "Favorite updated!", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                }
        );
        dialog.show();
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

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "⏸️ onPause");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "💥 onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🔥 onDestroy");
    }
}