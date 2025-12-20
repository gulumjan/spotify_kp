package com.example.spotify_kp.ui.newreleases;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NewReleasesFragment extends Fragment implements AlbumAdapter.OnAlbumClickListener {

    private NewReleasesViewModel viewModel;
    private AlbumAdapter adapter;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private LinearLayout errorState;
    private TextView errorText;
    private Button retryButton;
    private TextView headerTitle;
    private EditText searchInput;

    private List<AlbumEntity> allReleases = new ArrayList<>();
    private boolean isLoadingMore = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_releases, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupSwipeRefresh();
        setupScrollListener();
        setupSearchInput();
        observeNewReleases();

        viewModel.loadNewReleases();
    }

    private void initViews(View view) {
        headerTitle = view.findViewById(R.id.headerTitle);
        searchInput = view.findViewById(R.id.searchInput);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.emptyState);
        errorState = view.findViewById(R.id.errorState);
        errorText = view.findViewById(R.id.errorText);
        retryButton = view.findViewById(R.id.retryButton);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(NewReleasesViewModel.class);
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
            viewModel.loadNewReleases();
        });
    }

    private void setupSearchInput() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterReleases(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * ✅ ИСПРАВЛЕНИЕ: Фильтрация без дубликатов
     */
    private void filterReleases(String query) {
        if (query.isEmpty()) {
            adapter.setAlbums(allReleases);
            return;
        }

        String lowerQuery = query.toLowerCase();

        // ✅ Используем Set для уникальности
        Set<String> seenIds = new HashSet<>();
        List<AlbumEntity> filtered = new ArrayList<>();

        for (AlbumEntity album : allReleases) {
            // Проверяем что альбом еще не добавлен
            if (!seenIds.contains(album.getId()) &&
                    (album.getTitle().toLowerCase().contains(lowerQuery) ||
                            album.getArtist().toLowerCase().contains(lowerQuery))) {

                filtered.add(album);
                seenIds.add(album.getId());
            }
        }

        adapter.setAlbums(filtered);

        if (filtered.isEmpty()) {
            showEmpty();
        } else {
            showContent();
        }
    }

    private void setupScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoadingMore && !viewModel.isLoading()) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // Загружаем следующую страницу, когда доходим до последних 4 элементов
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 4
                            && firstVisibleItemPosition >= 0) {
                        loadNextPage();
                    }
                }
            }
        });

        retryButton.setOnClickListener(v -> viewModel.loadNewReleases());
    }

    private void loadNextPage() {
        isLoadingMore = true;
        viewModel.loadNextPage();
    }

    /**
     * ✅ ИСПРАВЛЕНИЕ: Обработка данных без дубликатов
     */
    private void observeNewReleases() {
        viewModel.getNewReleases().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                swipeRefresh.setRefreshing(false);
                isLoadingMore = false;

                switch (resource.getStatus()) {
                    case LOADING:
                        if (!swipeRefresh.isRefreshing() && !isLoadingMore) {
                            showLoading();
                        }
                        break;

                    case SUCCESS:
                        if (resource.getData() != null && !resource.getData().isEmpty()) {
                            showContent();

                            // ✅ ИСПРАВЛЕНИЕ: Удаляем дубликаты перед показом
                            allReleases = removeDuplicates(resource.getData());

                            // Применяем фильтр если есть текст в поиске
                            String searchText = searchInput.getText().toString();
                            if (!searchText.isEmpty()) {
                                filterReleases(searchText);
                            } else {
                                adapter.setAlbums(allReleases);
                            }
                        } else {
                            showEmpty();
                        }
                        break;

                    case ERROR:
                        if (adapter.getItemCount() == 0) {
                            showError(resource.getMessage());
                        }
                        break;
                }
            }
        });
    }

    /**
     * ✅ НОВЫЙ МЕТОД: Удаление дубликатов
     */
    private List<AlbumEntity> removeDuplicates(List<AlbumEntity> albums) {
        Set<String> seenIds = new HashSet<>();
        List<AlbumEntity> uniqueAlbums = new ArrayList<>();

        for (AlbumEntity album : albums) {
            if (!seenIds.contains(album.getId())) {
                uniqueAlbums.add(album);
                seenIds.add(album.getId());
            }
        }

        return uniqueAlbums;
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