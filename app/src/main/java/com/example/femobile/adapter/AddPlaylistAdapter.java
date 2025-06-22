package com.example.femobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.femobile.R;
import com.example.femobile.model.LibraryItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddPlaylistAdapter extends RecyclerView.Adapter<AddPlaylistAdapter.PlaylistViewHolder> {
    private List<LibraryItem> items = new ArrayList<>();
    private Set<Integer> selectedPositions = new HashSet<>();
    private OnSelectionChangedListener listener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(Set<Integer> selectedPositions);
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.listener = listener;
    }

    public void submitList(List<LibraryItem> newItems) {
        items = newItems;
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedPositions.clear();
        notifyDataSetChanged();
        if (listener != null) listener.onSelectionChanged(selectedPositions);
    }

    public Set<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    public List<LibraryItem> getSelectedItems() {
        List<LibraryItem> selected = new ArrayList<>();
        for (Integer pos : selectedPositions) {
            selected.add(items.get(pos));
        }
        return selected;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover, imgCheck;
        TextView tvTitle, tvSubtitle;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            imgCheck = itemView.findViewById(R.id.imgCheck);
        }

        public void bind(LibraryItem item, int position) {
            imgCover.setImageResource(item.getImageResId());
            tvTitle.setText(item.getTitle());
            tvSubtitle.setText(item.getSubtitle());
            imgCheck.setVisibility(selectedPositions.contains(position) ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> {
                if (selectedPositions.contains(position)) {
                    selectedPositions.remove(position);
                } else {
                    selectedPositions.add(position);
                }
                notifyItemChanged(position);
                if (listener != null) listener.onSelectionChanged(selectedPositions);
            });
        }
    }
} 