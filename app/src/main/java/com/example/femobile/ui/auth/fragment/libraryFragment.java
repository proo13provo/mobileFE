package com.example.femobile.ui.auth.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.femobile.R;
import com.example.femobile.adapter.LibraryItemAdapter;
import com.example.femobile.ui.auth.viewmodel.LibraryViewModel;

public class libraryFragment extends Fragment {
    private LibraryItemAdapter adapter;
    private LibraryViewModel libraryViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library,container,false);
        RecyclerView rvLibrary = view.findViewById(R.id.rvLibrary);
        adapter = new LibraryItemAdapter();
        rvLibrary.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLibrary.setAdapter(adapter);

        libraryViewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);
        libraryViewModel.getPlaylists().observe(getViewLifecycleOwner(), playlists -> {
            adapter.submitList(new java.util.ArrayList<>(playlists));
        });
        return view;
    }
}
