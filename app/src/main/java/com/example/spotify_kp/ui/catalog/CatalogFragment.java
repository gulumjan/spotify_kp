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
            viewModel.loadAlbums();
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
        chipGenre.setOnClickListener(v -> showGenreFilterDialog());
        chipYear.setOnClickListener(v -> showYearFilterDialog());
        chipClearFilters.setOnClickListener(v -> {
            viewModel.clearFilters();
            chipClearFilters.setVisibility(View.GONE);
            chipGenre.setText("Genre");
            chipYear.setText("Year");
        });

        updateFilterChipsVisibility();
    }

    private void showGenreFilterDialog() {
        if (allAlbums.isEmpty()) {
            Toast.makeText(getContext(), "Loading albums...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Собираем уникальные жанры
        Set<String> genres = new HashSet<>();
        for (AlbumEntity album : allAlbums) {
            if (album.getGenre() != null && !album.getGenre().isEmpty()) {
                genres.add(album.getGenre());
            }
        }

        String[] genreArray = genres.toArray(new String[0]);

        new AlertDialog.Builder(requireContext())
                .setTitle("Filter by Genre")
                .setItems(genreArray, (dialog, which) -> {
                    String selectedGenre = genreArray[which];
                    viewModel.filterByGenre(selectedGenre);
                    chipGenre.setText("Genre: " + selectedGenre);
                    chipClearFilters.setVisibility(View.VISIBLE);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showYearFilterDialog() {
        if (allAlbums.isEmpty()) {
            Toast.makeText(getContext(), "Loading albums...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Собираем уникальные годы
        Set<String> years = new HashSet<>();
        for (AlbumEntity album : allAlbums) {
            if (album.getYear() != null && !album.getYear().isEmpty() && !album.getYear().equals("Unknown")) {
                years.add(album.getYear());
            }
        }

        List<String> sortedYears = new ArrayList<>(years);
        sortedYears.sort((a, b) -> b.compareTo(a)); // От новых к старым

        String[] yearArray = sortedYears.toArray(new String[0]);

        new AlertDialog.Builder(requireContext())
                .setTitle("Filter by Year")
                .setItems(yearArray, (dialog, which) -> {
                    String selectedYear = yearArray[which];
                    viewModel.filterByYear(selectedYear);
                    chipYear.setText("Year: " + selectedYear);
                    chipClearFilters.setVisibility(View.VISIBLE);
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