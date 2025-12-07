package com.example.spotify_kp.ui.catalog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.spotify_kp.R;
import com.example.spotify_kp.data.local.entity.AlbumEntity;

import java.util.ArrayList;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private List<AlbumEntity> albums = new ArrayList<>();
    private OnAlbumClickListener listener;

    public interface OnAlbumClickListener {
        void onAlbumClick(AlbumEntity album);
    }

    public AlbumAdapter(OnAlbumClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        AlbumEntity album = albums.get(position);
        holder.bind(album, listener);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public void setAlbums(List<AlbumEntity> albums) {
        this.albums = albums != null ? albums : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        private ImageView albumCover;
        private TextView albumTitle;
        private TextView artistName;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumCover = itemView.findViewById(R.id.albumCover);
            albumTitle = itemView.findViewById(R.id.albumTitle);
            artistName = itemView.findViewById(R.id.artistName);
        }

        public void bind(AlbumEntity album, OnAlbumClickListener listener) {
            albumTitle.setText(album.getTitle());
            artistName.setText(album.getArtist());

            // Загрузка обложки с закругленными углами
            Glide.with(itemView.getContext())
                    .load(album.getCoverUrl())
                    .apply(new RequestOptions()
                            .transform(new RoundedCorners(16))
                            .placeholder(R.drawable.ic_music)
                            .error(R.drawable.ic_music))
                    .into(albumCover);

            // Клик на всю карточку
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAlbumClick(album);
                }
            });
        }
    }
}