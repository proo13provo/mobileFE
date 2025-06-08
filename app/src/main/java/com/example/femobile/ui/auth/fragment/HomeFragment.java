package com.example.femobile.ui.auth.fragment;

import static com.example.femobile.R.*;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.femobile.R;
import com.example.femobile.adapter.AlbumAdapter;
import com.example.femobile.adapter.SongAdapter;
import com.example.femobile.model.request.AlbumRequest.Album;
import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.service.MusicService;
import com.example.femobile.ui.auth.ActivityAlbum;
import com.example.femobile.ui.auth.SongDetailActivity;
import com.example.femobile.ui.auth.viewmodel.HomeViewModel;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private RecyclerView rvSongs, rvAlbums;
    private SongAdapter songAdapter;
    private MusicService musicService;
    private boolean bound = false;
    private HomeViewModel homeViewModel;
    AlbumAdapter albumAdapter;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize ViewModel
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // Bind to MusicService
        Intent intent = new Intent(getActivity(), MusicService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load recent listening history
        homeViewModel.loadRecentListeningHistory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bound) {
            getActivity().unbindService(serviceConnection);
            bound = false;
        }
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Initialize RecyclerView
        rvSongs = view.findViewById(R.id.rvSongs1);
        rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));

        rvAlbums = view.findViewById(R.id.rvAlbums1);
        rvAlbums.setLayoutManager(new GridLayoutManager(getContext(),6, RecyclerView.HORIZONTAL, false));

        albumAdapter = new AlbumAdapter();
        rvAlbums.setAdapter(albumAdapter);
        albumAdapter.setOnItemClickListener(album -> {
            Intent intent = new Intent(getActivity(), ActivityAlbum.class);
            intent.putExtra("albumId", album.getId());
            intent.putExtra("coverUrl", album.getCoverUrl());
            intent.putExtra("albumTitle", album.getTitle());
            startActivity(intent);
        });

        // Initialize adapter
        songAdapter = new SongAdapter();
        rvSongs.setAdapter(songAdapter);
        
        // Set click listener for songs
        songAdapter.setOnItemClickListener(song -> {
            if (getActivity() != null) {
                if (bound && musicService != null) {
                    // Play the song
                    musicService.playSong(song);
                    
                    // Open song detail activity
                    Intent intent = new Intent(getActivity(), SongDetailActivity.class);
                    intent.putExtra("songId", song.getId());
                    intent.putExtra("currentSong", song);
                    intent.putExtra("isPlaying", true);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Đang kết nối service...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Observe recent songs LiveData
        homeViewModel.getRecentSongs().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null && !songs.isEmpty()) {
                TextView recentTextView = view.findViewById(R.id.recent);
                recentTextView.setVisibility(View.VISIBLE);
                songAdapter.submitList(songs);
            } else {
                TextView recentTextView = view.findViewById(R.id.recent);
                recentTextView.setVisibility(View.GONE);
                songAdapter.submitList(new ArrayList<>());
            }
        });
        
        return view;
    }
}
