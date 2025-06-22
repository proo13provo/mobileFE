package com.example.femobile.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.femobile.R;
import com.example.femobile.adapter.AddPlaylistAdapter;
import com.example.femobile.model.LibraryItem;
import com.example.femobile.ui.auth.viewmodel.LibraryViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddToPlaylistDialogFragment extends BottomSheetDialogFragment {
    private AddPlaylistAdapter adapter;
    private OnAddToPlaylistDoneListener doneListener;
    private LibraryViewModel libraryViewModel;

    public interface OnAddToPlaylistDoneListener {
        void onAddToPlaylistDone(java.util.List<LibraryItem> selectedItems);
    }

    public static AddToPlaylistDialogFragment newInstance() {
        return new AddToPlaylistDialogFragment();
    }

    public void setOnAddToPlaylistDoneListener(OnAddToPlaylistDoneListener listener) {
        this.doneListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_to_playlist, container, false);

        TextView btnCancel = view.findViewById(R.id.btnCancel);
        Button btnNewPlaylist = view.findViewById(R.id.btnNewPlaylist);
        TextView btnClearAll = view.findViewById(R.id.btnClearAll);
        Button btnDone = view.findViewById(R.id.btnDone);
        RecyclerView rvPlaylists = view.findViewById(R.id.rvPlaylists);

        adapter = new AddPlaylistAdapter();
        rvPlaylists.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPlaylists.setAdapter(adapter);

        libraryViewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);
        libraryViewModel.getPlaylists().observe(getViewLifecycleOwner(), playlists -> {
            adapter.submitList(new java.util.ArrayList<>(playlists));
        });

        btnCancel.setOnClickListener(v -> dismiss());
        btnClearAll.setOnClickListener(v -> adapter.clearSelection());
        btnDone.setOnClickListener(v -> {
            if (doneListener != null) {
                doneListener.onAddToPlaylistDone(adapter.getSelectedItems());
            }
            for (LibraryItem item : adapter.getSelectedItems()) {
                libraryViewModel.addPlaylist(item);
            }
            dismiss();
        });
        btnNewPlaylist.setOnClickListener(v -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
            builder.setTitle("New Playlist");

            final android.widget.EditText input = new android.widget.EditText(requireContext());
            input.setHint("Playlist name");
            builder.setView(input);

            builder.setPositiveButton("Create", (dialog1, which) -> {
                String playlistName = input.getText().toString().trim();
                if (!playlistName.isEmpty()) {
                    LibraryItem newPlaylist = new LibraryItem(
                        R.drawable.ic_music_placeholder,
                        playlistName,
                        "0 songs"
                    );
                    libraryViewModel.addPlaylist(newPlaylist);
                }
            });
            builder.setNegativeButton("Cancel", (dialog1, which) -> dialog1.cancel());

            builder.show();
        });

        return view;
    }
} 