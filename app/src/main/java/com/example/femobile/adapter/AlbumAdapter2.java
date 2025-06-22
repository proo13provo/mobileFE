package com.example.femobile.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.femobile.R;
import com.example.femobile.model.request.AlbumRequest.Album;

import java.util.ArrayList;
import java.util.List;

public class AlbumAdapter2 extends RecyclerView.Adapter<AlbumAdapter2.AlbumViewHolder> {
    private List<Album> albums = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public AlbumAdapter2.AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumAdapter2.AlbumViewHolder holder, int position) {
        Album album = albums.get(position);
        holder.tvPlaylistName.setText(album.getTitle());

        String imageUrl = album.getCoverUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_music_note)
                    .error(R.drawable.ic_music_note)
                    .centerCrop()
                    .into(holder.ivPlaylistCover);
        } else {
            holder.ivPlaylistCover.setImageResource(R.drawable.ic_music_note);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(album);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<Album> newAlbums) {
        this.albums = newAlbums != null ? newAlbums : new ArrayList<>();
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Album album);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaylistName;
        ImageView ivPlaylistCover;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaylistName = itemView.findViewById(R.id.tvPlaylistName);
            ivPlaylistCover = itemView.findViewById(R.id.ivPlaylistCover);
        }
    }

}
