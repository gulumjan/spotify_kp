package com.example.spotify_kp.ui.favorites.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.spotify_kp.R;
import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.data.local.entity.FavoriteEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    public interface OnFavoriteClickListener {
        void onFavoriteClick(FavoriteEntity favorite, AlbumEntity album);
        void onRemoveFavorite(FavoriteEntity favorite);
        void onEditFavorite(FavoriteEntity favorite, AlbumEntity album);
    }

    private List<FavoriteEntity> favorites = new ArrayList<>();
    private Map<String, AlbumEntity> albumsMap = new HashMap<>();
    private OnFavoriteClickListener listener;

    public FavoriteAdapter(OnFavoriteClickListener listener) {
        this.listener = listener;
    }

    public void setFavorites(List<FavoriteEntity> favorites, List<AlbumEntity> albums) {
        this.favorites = favorites != null ? favorites : new ArrayList<>();

        albumsMap.clear();
        if (albums != null) {
            for (AlbumEntity album : albums) {
                albumsMap.put(album.getId(), album);
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteEntity favorite = favorites.get(position);
        AlbumEntity album = albumsMap.get(favorite.getAlbumId());

        if (album != null) {
            holder.bind(favorite, album, listener);
        }
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private ImageView albumCover;
        private TextView albumTitle;
        private TextView artistName;
        private TextView yearGenre;
        private RatingBar ratingBar;
        private TextView ratingText;
        private TextView commentText;
        private TextView addedDate;
        private ImageView removeButton;
        private ImageView editButton;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            albumCover = itemView.findViewById(R.id.albumCover);
            albumTitle = itemView.findViewById(R.id.albumTitle);
            artistName = itemView.findViewById(R.id.artistName);
            yearGenre = itemView.findViewById(R.id.yearGenre);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            ratingText = itemView.findViewById(R.id.ratingText);
            commentText = itemView.findViewById(R.id.commentText);
            addedDate = itemView.findViewById(R.id.addedDate);
            removeButton = itemView.findViewById(R.id.removeButton);
            editButton = itemView.findViewById(R.id.editButton);
        }

        public void bind(FavoriteEntity favorite, AlbumEntity album, OnFavoriteClickListener listener) {
            albumTitle.setText(album.getTitle());
            artistName.setText(album.getArtist());

            // Year and Genre
            String yearGenreText = album.getYear() + " • " + album.getGenre();
            yearGenre.setText(yearGenreText);

            // Rating
            ratingBar.setRating(favorite.getUserRating());
            ratingText.setText(String.format(Locale.getDefault(), "%.1f", favorite.getUserRating()));

            // Comment
            if (favorite.getUserComment() != null && !favorite.getUserComment().isEmpty()) {
                commentText.setVisibility(View.VISIBLE);
                commentText.setText(favorite.getUserComment());
            } else {
                commentText.setVisibility(View.GONE);
            }

            // Added Date
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateText = "Added: " + sdf.format(new Date(favorite.getAddedDate()));
            addedDate.setText(dateText);

            // Load cover
            Glide.with(itemView.getContext())
                    .load(album.getCoverUrl())
                    .apply(new RequestOptions()
                            .transform(new RoundedCorners(16))
                            .placeholder(R.drawable.ic_music)
                            .error(R.drawable.ic_music))
                    .into(albumCover);

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(favorite, album);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditFavorite(favorite, album);
                }
            });

            removeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveFavorite(favorite);
                }
            });
        }
    }
}