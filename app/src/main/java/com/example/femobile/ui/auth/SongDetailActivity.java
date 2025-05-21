package com.example.femobile.ui.auth;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.femobile.R;
import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.MusicService;
import com.example.femobile.service.api.SongApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SongDetailActivity extends AppCompatActivity {
    private static final String TAG = "SongDetailActivity";
    private static final int PERMISSION_REQUEST_CODE = 123;
    TextView songTitle, artistName, currentTime, totalTime;
    ImageView albumArt;
    ImageButton backButton;
    FloatingActionButton playPauseButton;
    SeekBar seekBar;
    private String songId;
    private Song currentSong;
    private Handler handler;
    private Runnable updateTimeRunnable;
    private boolean isUserSeeking = false;
    private MusicService musicService;
    private boolean bound = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startMusicService();
            } else {
                Toast.makeText(this, "Permission required for music playback", Toast.LENGTH_SHORT).show();
            }
        });

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service connected");
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            bound = true;
            
            // Check if we have a current song from intent
            Song songFromIntent = getIntent().getParcelableExtra("currentSong");
            boolean wasPlaying = getIntent().getBooleanExtra("isPlaying", false);
            
            if (songFromIntent != null) {
                Log.d(TAG, "Found song from intent: " + songFromIntent.getTitle());
                currentSong = songFromIntent;
                updateUI(currentSong);
                // Don't call resumePlayback here as it might interrupt current playback
                if (wasPlaying && !musicService.isPlaying()) {
                    Log.d(TAG, "Resuming playback from mini player");
                    musicService.resumePlayback();
                }
            } else if (currentSong != null) {
                Log.d(TAG, "Playing song after service connection: " + currentSong.getTitle());
                musicService.playSong(currentSong);
            }
            updatePlayPauseButton();
            // Start time updates
            handler.post(updateTimeRunnable);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Service disconnected");
            bound = false;
            // Stop time updates
            handler.removeCallbacks(updateTimeRunnable);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songdetail);

        // Initialize views
        songTitle = findViewById(R.id.songTitle);
        artistName = findViewById(R.id.artistName);
        albumArt = findViewById(R.id.albumArt);
        backButton = findViewById(R.id.backButton);
        playPauseButton = findViewById(R.id.playPauseButton);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        seekBar = findViewById(R.id.seekBar);

        // Initialize handler for updating time
        handler = new Handler(Looper.getMainLooper());
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimeDisplay();
                handler.postDelayed(this, 1000);
            }
        };

        // Set up SeekBar
        setupSeekBar();

        // Get songId from intent
        songId = getIntent().getStringExtra("songId");
        Song songFromIntent = getIntent().getParcelableExtra("currentSong");
        
        if (songFromIntent != null) {
            Log.d(TAG, "Using song from intent: " + songFromIntent.getTitle());
            currentSong = songFromIntent;
            updateUI(currentSong);
            startMusicService();
        } else if (songId != null) {
            Log.d(TAG, "Loading song details for ID: " + songId);
            startMusicService();
            loadSongDetails();
        } else {
            Log.e(TAG, "No song ID or song object provided");
            Toast.makeText(this, "Error: No song selected", Toast.LENGTH_SHORT).show();
            finish();
        }

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SongDetailActivity.this, SecondActivity.class);
            intent.putExtra("showMiniPlayer", true);
            intent.putExtra("currentSong", currentSong);
            intent.putExtra("isPlaying", musicService != null && musicService.isPlaying());
            startActivity(intent);
            overridePendingTransition(R.anim.stay, R.anim.slide_out_down);
            finish();
        });

        playPauseButton.setOnClickListener(v -> {
            if (checkAndRequestPermissions()) {
                togglePlayPause();
            }
        });
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return false;
            }
        }
        return true;
    }

    private void startMusicService() {
        Log.d(TAG, "Starting music service");
        Intent intent = new Intent(this, MusicService.class);
        // Start the service first
        startService(intent);
        // Then bind to it
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicService != null) {
                    currentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
                // Pause the time update while seeking
                handler.removeCallbacks(updateTimeRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (musicService != null) {
                    int progress = seekBar.getProgress();
                    musicService.seekTo(progress);
                    currentTime.setText(formatTime(progress));
                    isUserSeeking = false;
                    // Resume time updates
                    handler.post(updateTimeRunnable);
                }
            }
        });
    }

    private void loadSongDetails() {
        // If we already have a song from intent, don't load again
        if (getIntent().getParcelableExtra("currentSong") != null) {
            Log.d(TAG, "Using song from intent, skipping API call");
            return;
        }

        SongApi songApi = RetrofitClient.getApiService(this);
        songApi.getSong(songId).enqueue(new Callback<Song>() {
            @Override
            public void onResponse(Call<Song> call, Response<Song> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentSong = response.body();
                    Log.d(TAG, "Song details loaded: " + currentSong.getTitle());
                    Log.d(TAG, "Media URL: " + currentSong.getMediaUrl());
                    updateUI(currentSong);
                    if (bound) {
                        Log.d(TAG, "Service bound, playing song");
                        musicService.playSong(currentSong);
                        updatePlayPauseButton();
                    } else {
                        Log.d(TAG, "Service not bound yet, will play when connected");
                        startMusicService();
                    }
                } else {
                    Log.e(TAG, "Failed to load song details: " + response.code());
                    Toast.makeText(SongDetailActivity.this, "Không thể tải thông tin bài hát", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Song> call, Throwable t) {
                Log.e(TAG, "Error loading song details", t);
                Toast.makeText(SongDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Song song) {
        // Update text views
        songTitle.setText(song.getTitle());
        artistName.setText(song.getSinger());

        // Load album art using Glide
        String imageUrl = song.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.ic_music_note)
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e(TAG, "Failed to load album art", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (resource instanceof BitmapDrawable) {
                            Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                            Palette.from(bitmap).generate(palette -> {
                                if (palette != null) {
                                    int dominantColor = palette.getDominantColor(getResources().getColor(R.color.dark_red));
                                    int vibrantColor = palette.getVibrantColor(dominantColor);
                                    findViewById(R.id.rootLayout).setBackgroundColor(vibrantColor);
                                    int textColor = isColorDark(vibrantColor) ? 
                                        getResources().getColor(R.color.white) : 
                                        getResources().getColor(R.color.black);
                                    songTitle.setTextColor(textColor);
                                    artistName.setTextColor(textColor);
                                }
                            });
                        }
                        return false;
                    }
                })
                .into(albumArt);
        } else {
            albumArt.setImageResource(R.drawable.ic_music_note);
        }
    }

    private void togglePlayPause() {
        if (currentSong == null) {
            Log.e(TAG, "No song to play");
            Toast.makeText(this, "Không có bài hát để phát", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bound) {
            Log.e(TAG, "Service not bound, attempting to bind again");
            startMusicService();
            Toast.makeText(this, "Đang kết nối service...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (musicService != null) {
            if (musicService.isPlaying()) {
                Log.d(TAG, "Pausing playback");
                musicService.pausePlayback();
            } else {
                Log.d(TAG, "Resuming playback");
                musicService.resumePlayback();
            }
            updatePlayPauseButton();
        } else {
            Log.e(TAG, "MusicService is null");
            Toast.makeText(this, "Service not ready", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTimeDisplay() {
        if (bound && !isUserSeeking && musicService != null) {
            int currentPosition = musicService.getCurrentPosition();
            int duration = musicService.getDuration();
            if (duration > 0) {
                currentTime.setText(formatTime(currentPosition));
                totalTime.setText(formatTime(duration));
                seekBar.setMax(duration);
                seekBar.setProgress(currentPosition);
            }
        }
    }

    private String formatTime(long milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void updatePlayPauseButton() {
        if (bound) {
            playPauseButton.setImageResource(musicService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        }
    }

    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * android.graphics.Color.red(color) + 
                             0.587 * android.graphics.Color.green(color) + 
                             0.114 * android.graphics.Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }
        handler.removeCallbacks(updateTimeRunnable);
    }
}
