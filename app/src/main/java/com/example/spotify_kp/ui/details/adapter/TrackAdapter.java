package com.example.spotify_kp.ui.details.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotify_kp.R;
import com.example.spotify_kp.data.remote.dto.TrackItemDto;

import java.util.ArrayList;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private List<TrackItemDto> tracks = new ArrayList<>();

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        TrackItemDto track = tracks.get(position);
        holder.bind(track);
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public void setTracks(List<TrackItemDto> tracks) {
        this.tracks = tracks != null ? tracks : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {
        private TextView trackNumber;
        private TextView trackName;
        private TextView trackDuration;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackNumber = itemView.findViewById(R.id.trackNumber);
            trackName = itemView.findViewById(R.id.trackName);
            trackDuration = itemView.findViewById(R.id.trackDuration);
        }

        public void bind(TrackItemDto track) {
            trackNumber.setText(String.valueOf(track.getTrackNumber()));
            trackName.setText(track.getName());
            trackDuration.setText(formatDuration(track.getDurationMs()));
        }

        private String formatDuration(int durationMs) {
            int minutes = (durationMs / 1000) / 60;
            int seconds = (durationMs / 1000) % 60;
            return String.format("%d:%02d", minutes, seconds);
        }
    }
}