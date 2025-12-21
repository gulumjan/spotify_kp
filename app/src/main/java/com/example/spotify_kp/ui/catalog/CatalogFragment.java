package com.example.spotify_kp.ui.catalog;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.spotify_kp.R;
import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.ui.catalog.adapter.AlbumAdapter;
import com.example.spotify_kp.ui.details.DetailsActivity;
import com.example.spotify_kp.utils.Constants;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CatalogFragment extends Fragment implements AlbumAdapter.OnAlbumClickListener {

    private CatalogViewModel viewModel;
    private AlbumAdapter adapter;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private LinearLayout errorState;
    private TextView errorText;
    private Button retryButton;
    private EditText searchInput;
    private ChipGroup filterChipGroup;
    private Chip chipGenre;
    private Chip chipYear;
    private Chip chipClearFilters;

    private List<AlbumEntity> allAlbums = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_catalog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupSwipeRefresh();
        setupSearchInput();
        setupFilters();
        observeAlbums();

        viewModel.loadAlbums();
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.emptyState);
        errorState = view.findViewById(R.id.errorState);
        errorText = view.findViewById(R.id.errorText);
        retryButton = view.findViewById(R.id.retryButton);
        searchInput = view.findViewById(R.id.searchInput);
        filterChipGroup = view.findViewById(R.id.filterChipGroup);
        chipGenre = view.findViewById(R.id.chipGenre);
        chipYear = view.findViewById(R.id.chipYear);
        chipClearFilters = view.findViewById(R.id.chipClearFilters);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CatalogViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new AlbumAdapter(this);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.spotify_green);
        swipeRefresh.setOnRefreshListener(() -> {
            // ✅ ИСПРАВЛЕНО: используем forceRefresh вместо loadAlbums
            viewModel.forceRefresh();
        });
    }

    private void setupSearchInput() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    viewModel.searchAlbums(s.toString());
                } else if (s.length() == 0) {
                    viewModel.loadAlbums();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        retryButton.setOnClickListener(v -> viewModel.loadAlbums());
    }

    private void setupFilters() {
        // Genre filter with "All" option
        chipGenre.setOnClickListener(v -> showGenreFilterBottomSheet());

        // Year filter with "All" option
        chipYear.setOnClickListener(v -> showYearFilterBottomSheet());

        // Clear all filters
        chipClearFilters.setOnClickListener(v -> {
            viewModel.clearFilters();
            chipClearFilters.setVisibility(View.GONE);
            chipGenre.setText("Genre");
            chipYear.setText("Year");
        });

        updateFilterChipsVisibility();
    }

    private void showGenreFilterBottomSheet() {
        if (allAlbums.isEmpty()) {
            Toast.makeText(getContext(), "Loading albums...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Collect unique genres
        Set<String> genres = new HashSet<>();
        for (AlbumEntity album : allAlbums) {
            if (album.getGenre() != null && !album.getGenre().isEmpty()) {
                genres.add(album.getGenre());
            }
        }

        List<String> genreList = new ArrayList<>(genres);
        genreList.add(0, "🎵 All Genres"); // ✅ NEW: Add "All" option with emoji

        String[] genreArray = genreList.toArray(new String[0]);

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Genre")
                .setSingleChoiceItems(genreArray, -1, (dialog, which) -> {
                    if (which == 0) {
                        // "All Genres" selected - clear filter
                        viewModel.clearFilters();
                        chipGenre.setText("Genre");
                        chipClearFilters.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Showing all genres", Toast.LENGTH_SHORT).show();
                    } else {
                        // Specific genre selected
                        String selectedGenre = genreArray[which].replace("🎵 ", "");
                        viewModel.filterByGenre(selectedGenre);
                        chipGenre.setText("🎵 " + selectedGenre);
                        chipClearFilters.setVisibility(View.VISIBLE);

                        // Count how many albums match this genre
                        int count = 0;
                        for (AlbumEntity album : allAlbums) {
                            if (selectedGenre.equals(album.getGenre())) {
                                count++;
                            }
                        }
                        Toast.makeText(getContext(), count + " albums found", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showYearFilterBottomSheet() {
        if (allAlbums.isEmpty()) {
            Toast.makeText(getContext(), "Loading albums...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Collect unique years
        Set<String> years = new HashSet<>();
        for (AlbumEntity album : allAlbums) {
            if (album.getYear() != null && !album.getYear().isEmpty() && !album.getYear().equals("Unknown")) {
                years.add(album.getYear());
            }
        }

        List<String> sortedYears = new ArrayList<>(years);
        sortedYears.sort((a, b) -> b.compareTo(a)); // Newest first
        sortedYears.add(0, "📅 All Years"); // ✅ NEW: Add "All" option with emoji

        String[] yearArray = sortedYears.toArray(new String[0]);

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Year")
                .setSingleChoiceItems(yearArray, -1, (dialog, which) -> {
                    if (which == 0) {
                        // "All Years" selected - clear filter
                        viewModel.clearFilters();
                        chipYear.setText("Year");
                        chipClearFilters.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Showing all years", Toast.LENGTH_SHORT).show();
                    } else {
                        // Specific year selected
                        String selectedYear = yearArray[which].replace("📅 ", "");
                        viewModel.filterByYear(selectedYear);
                        chipYear.setText("📅 " + selectedYear);
                        chipClearFilters.setVisibility(View.VISIBLE);

                        // Count how many albums from this year
                        int count = 0;
                        for (AlbumEntity album : allAlbums) {
                            if (selectedYear.equals(album.getYear())) {
                                count++;
                            }
                        }
                        Toast.makeText(getContext(), count + " albums from " + selectedYear, Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateFilterChipsVisibility() {
        if (viewModel.getCurrentGenre() != null || viewModel.getCurrentYear() != null) {
            chipClearFilters.setVisibility(View.VISIBLE);
        } else {
            chipClearFilters.setVisibility(View.GONE);
        }
    }

    private void observeAlbums() {
        viewModel.getAlbums().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                swipeRefresh.setRefreshing(false);

                switch (resource.getStatus()) {
                    case LOADING:
                        if (!swipeRefresh.isRefreshing()) {
                            showLoading();
                        }
                        break;

                    case SUCCESS:
                        if (resource.getData() != null && !resource.getData().isEmpty()) {
                            showContent();
                            allAlbums = new ArrayList<>(resource.getData());
                            adapter.setAlbums(resource.getData());
                        } else {
                            showEmpty();
                        }
                        break;

                    case ERROR:
                        showError(resource.getMessage());
                        break;
                }
            }
        });
    }

    @Override
    public void onAlbumClick(AlbumEntity album) {
        Intent intent = new Intent(getContext(), DetailsActivity.class);
        intent.putExtra(Constants.KEY_ALBUM_ID, album.getId());
        startActivity(intent);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        errorState.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        errorState.setVisibility(View.GONE);
    }

    private void showEmpty() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        errorState.setVisibility(View.GONE);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        errorState.setVisibility(View.VISIBLE);
        errorText.setText(message != null ? message : "Something went wrong");
    }
}