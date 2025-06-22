package com.example.femobile.ui.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.femobile.model.LibraryItem;
import com.example.femobile.R;

import java.util.ArrayList;
import java.util.List;

public class LibraryViewModel extends ViewModel {
    private final MutableLiveData<List<LibraryItem>> playlists = new MutableLiveData<>(new ArrayList<>());

    public LibraryViewModel() {
        // Thêm playlist mẫu mặc định để kiểm tra hiển thị
        List<LibraryItem> demo = new ArrayList<>();
        demo.add(new LibraryItem(R.drawable.ic_music_placeholder, "Demo Playlist", "0 songs"));
        playlists.setValue(demo);
    }

    public LiveData<List<LibraryItem>> getPlaylists() {
        return playlists;
    }

    public void addPlaylist(LibraryItem playlist) {
        List<LibraryItem> current = new ArrayList<>(playlists.getValue());
        current.add(0, playlist);
        playlists.setValue(current);
    }

    public void setPlaylists(List<LibraryItem> list) {
        playlists.setValue(list);
    }
} 