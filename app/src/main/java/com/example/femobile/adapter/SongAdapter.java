package com.example.femobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.femobile.R;
import com.example.femobile.model.request.SongRequest.Song;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> songs = new ArrayList<>();

    private OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(Song song);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public void submitList(List<Song> newSongs) {
        this.songs = newSongs != null ? newSongs : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.tvTitle.setText(song.getTitle());
        holder.tvSinger.setText(song.getSinger());

        // Xử lý load ảnh từ S3
        String imageUrl = song.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_music_note) // Ảnh mặc định khi đang load
                .error(R.drawable.ic_music_note) // Ảnh mặc định khi load lỗi
                .centerCrop() // Cắt ảnh để vừa với kích thước
                .into(holder.ivSongImage);
        } else {
            // Nếu không có URL ảnh, hiển thị ảnh mặc định
            holder.ivSongImage.setImageResource(R.drawable.ic_music_note);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(song); // Gọi callback khi item được click
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSinger;
        ImageView ivSongImage;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSongTitle);
            tvSinger = itemView.findViewById(R.id.tvSongSinger);
            ivSongImage = itemView.findViewById(R.id.ivSongImage);
        }
    }
}