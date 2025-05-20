package com.example.femobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.femobile.R;
import com.example.femobile.model.request.SongRequest.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> songList;

    public SongAdapter(List<Song> songList) {
        this.songList = songList;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivSongImage;
        private TextView tvTitle;
        private TextView tvSinger;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSongImage = itemView.findViewById(R.id.rvSongs);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSinger = itemView.findViewById(R.id.bottom_nav);
        }

        public void bind(Song song) {
            tvTitle.setText(song.getTitle());
            tvSinger.setText(song.getSinger());

            // Load song image using Glide
            if (song.getImageUrl() != null && !song.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(song.getImageUrl())
                        .placeholder(R.drawable.music)
                        .error(R.drawable.music)
                        .into(ivSongImage);
            } else {
                ivSongImage.setImageResource(R.drawable.music);
            }
        }
    }
} 