package com.example.femobile.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.femobile.R;
import com.example.femobile.model.response.PlayListResponse;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private List<PlayListResponse> playlists = new ArrayList<>();
    private Long selectedPlaylistId = null;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(PlayListResponse playlist);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setPlaylists(List<PlayListResponse> playlists) {
        this.playlists = playlists != null ? playlists : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedPlaylistId(Long id) {
        this.selectedPlaylistId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        PlayListResponse playlist = playlists.get(position);
        holder.tvPlaylistName.setText(playlist.getName());
        // Có thể load ảnh nếu có: Glide.with(...).load(playlist.getImageUrl())...

        // Highlight nếu được chọn
        if (playlist.getId() != null && playlist.getId().equals(selectedPlaylistId)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#1DB954")); // Màu xanh Spotify
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(playlist);
            setSelectedPlaylistId(playlist.getId());
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaylistName;
        ImageView ivPlaylistCover;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaylistName = itemView.findViewById(R.id.tvPlaylistName);
            ivPlaylistCover = itemView.findViewById(R.id.ivPlaylistCover);
        }
    }
}