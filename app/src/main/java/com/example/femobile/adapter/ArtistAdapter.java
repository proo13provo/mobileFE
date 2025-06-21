package com.example.femobile.adapter;

import android.annotation.SuppressLint;
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
import com.example.femobile.model.request.AlbumRequest.Album;
import com.example.femobile.model.request.ArtistRequest.Artist;

import java.util.ArrayList;
import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {
    private List<Artist> artists = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(Artist artist);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist, parent, false);
        return new ArtistViewHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        Artist artist = artists.get(position);
        holder.tv_artist_name.setText(artist.getUsername());

        String imageUrl = artist.getUrlAvatar();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_music_note) // Ảnh mặc định khi đang load
                    .error(R.drawable.ic_music_note) // Ảnh mặc định khi load lỗi
                    .centerCrop() // Cắt ảnh để vừa với kích thước
                    .into(holder.img_album_cover);
        }else {
            holder.img_album_cover.setImageResource(R.drawable.ic_music_note);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null){
                listener.onItemClick(artist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<Artist> newArtist) {
        this.artists = newArtist != null ? newArtist : new ArrayList<>();
        notifyDataSetChanged();
    }
    public class ArtistViewHolder extends RecyclerView.ViewHolder{
        TextView tv_artist_name,tv_artist;
        ImageView img_album_cover;
        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_artist_name = itemView.findViewById(R.id.tv_artist_name);
            img_album_cover = itemView.findViewById(R.id.img_album_cover);
        }
    }
}
