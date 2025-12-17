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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    public interface OnFavoriteClickListener {
        void onFavoriteClick(FavoriteEntity favorite, AlbumEntity album);
        void onRemoveFavorite(FavoriteEntity favorite);
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
        private RatingBar ratingBar;
        private TextView commentText;
        private ImageView removeButton;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            albumCover = itemView.findViewById(R.id.albumCover);
            albumTitle = itemView.findViewById(R.id.albumTitle);
            artistName = itemView.findViewById(R.id.artistName);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            commentText = itemView.findViewById(R.id.commentText);
            removeButton = itemView.findViewById(R.id.removeButton);
        }

        public void bind(FavoriteEntity favorite, AlbumEntity album, OnFavoriteClickListener listener) {
            albumTitle.setText(album.getTitle());
            artistName.setText(album.getArtist());
            ratingBar.setRating(favorite.getUserRating());

            // Show or hide comment
            if (favorite.getUserComment() != null && !favorite.getUserComment().isEmpty()) {
                commentText.setVisibility(View.VISIBLE);
                commentText.setText(favorite.getUserComment());
            } else {
                commentText.setVisibility(View.GONE);
            }

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

            removeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveFavorite(favorite);
                }
            });
        }
    }
}