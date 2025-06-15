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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.femobile.R;
import com.example.femobile.databinding.SongdetailBinding;
import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.model.request.SongRequest.SongIdsRequest;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.MusicService;
import com.example.femobile.service.api.SongApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SongDetailActivity extends AppCompatActivity {
    private static final String TAG = "SongDetailActivity";
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    
    private SongdetailBinding binding;
    private String songId;
    private Song currentSong;
    private Handler handler;
    private Runnable updateTimeRunnable;
    private boolean isUserSeeking = false;
    private MusicService musicService;
    private boolean bound = false;
    private boolean isDestroyed = false;
    private List<String> playedSongIds = new ArrayList<>();
    private int currentSongIndex = -1;
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::handlePermissionResult);
    private GestureDetector gestureDetector;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (isDestroyed) return;
            handleServiceConnection(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Service disconnected");
            bound = false;
            if (!isDestroyed) {
                handler.removeCallbacks(updateTimeRunnable);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SongdetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeViews();
        setupClickListeners();
        setupSeekBar();
        initializeHandler();
        setupGestureDetector();
        
        handleIntentData();
    }

    private void initializeViews() {
        handler = new Handler(Looper.getMainLooper());
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> handleBackButtonClick());
        binding.playPauseButton.setOnClickListener(v -> {
            if (!isDestroyed) {
                togglePlayPause();
            }
        });
        binding.nextButton.setOnClickListener(v -> {
            if (!isDestroyed) {
                playNextSong();
            }
        });
        binding.prevButton.setOnClickListener(v -> {
            if (!isDestroyed) {
                playPreviousSong();
            }
        });
    }

    private void handleBackButtonClick() {
        if (isDestroyed) return;

        Intent intent = new Intent(SongDetailActivity.this, SecondActivity.class);
        intent.putExtra("showMiniPlayer", true);
        intent.putExtra("currentSong", currentSong);
        intent.putExtra("isPlaying", musicService != null && musicService.isPlaying());
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(R.anim.stay, R.anim.slide_out_down);
        finish();
    }

    private void initializeHandler() {
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isDestroyed) {
                    updateTimeDisplay();
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    private void handleIntentData() {
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
    }

    private void handlePermissionResult(java.util.Map<String, Boolean> permissions) {
        boolean allGranted = permissions.values().stream().allMatch(isGranted -> isGranted);
        if (allGranted && !isDestroyed) {
            startMusicService();
        } else if (!isDestroyed) {
            Toast.makeText(this, "Cần cấp quyền để phát nhạc", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleServiceConnection(IBinder service) {
        Log.d(TAG, "Service connected");
        MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
        musicService = binder.getService();
        bound = true;

        Song songFromIntent = getIntent().getParcelableExtra("currentSong");
        boolean wasPlaying = getIntent().getBooleanExtra("isPlaying", false);

        if (songFromIntent != null) {
            handleSongFromIntent(songFromIntent, wasPlaying);
        } else if (currentSong != null) {
            Log.d(TAG, "Playing song after service connection: " + currentSong.getTitle());
            musicService.playSong(currentSong);
        }

        if (!isDestroyed) {
            handler.post(updateTimeRunnable);
        }
    }

    private void handleSongFromIntent(Song song, boolean wasPlaying) {
        Log.d(TAG, "Found song from intent: " + song.getTitle());
        currentSong = song;
        if (playedSongIds.isEmpty()) {
            playedSongIds.add(currentSong.getId());
            currentSongIndex = 0;
        }
        runOnUiThread(() -> {
            if (!isDestroyed) {
                updateUI(currentSong);
                updatePlayPauseButton();
            }
        });

        if (wasPlaying && !musicService.isPlaying()) {
            Log.d(TAG, "Resuming playback from mini player");
            musicService.resumePlayback();
        }
    }

    private void startMusicService() {
        if (isDestroyed) return;

        Log.d(TAG, "Starting music service");
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setupSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicService != null && !isDestroyed) {
                    binding.currentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
                if (!isDestroyed) {
                    handler.removeCallbacks(updateTimeRunnable);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (musicService != null && !isDestroyed) {
                    int progress = seekBar.getProgress();
                    musicService.seekTo(progress);
                    binding.currentTime.setText(formatTime(progress));
                    isUserSeeking = false;
                    handler.post(updateTimeRunnable);
                }
            }
        });
    }

    private void loadSongDetails() {
        if (isDestroyed) return;

        if (getIntent().getParcelableExtra("currentSong") != null) {
            Log.d(TAG, "Using song from intent, skipping API call");
            return;
        }

        SongApi songApi = RetrofitClient.getApiService(this);
        songApi.getSong(songId).enqueue(new Callback<Song>() {
            @Override
            public void onResponse(@NonNull Call<Song> call, @NonNull Response<Song> response) {
                if (isDestroyed) return;

                if (response.isSuccessful() && response.body() != null) {
                    currentSong = response.body();
                    Log.d(TAG, "Song details loaded: " + currentSong.getTitle());
                    Log.d(TAG, "Media URL: " + currentSong.getMediaUrl());

                    runOnUiThread(() -> {
                        if (!isDestroyed) {
                            updateUI(currentSong);
                        }
                    });

                    if (bound) {
                        Log.d(TAG, "Service bound, playing song");
                        musicService.playSong(currentSong);
                        runOnUiThread(() -> {
                            if (!isDestroyed) {
                                updatePlayPauseButton();
                            }
                        });
                    } else {
                        Log.d(TAG, "Service not bound yet, will play when connected");
                        startMusicService();
                    }
                } else {
                    Log.e(TAG, "Failed to load song details: " + response.code());
                    if (!isDestroyed) {
                        Toast.makeText(SongDetailActivity.this, "Không thể tải thông tin bài hát", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Song> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading song details", t);
                if (!isDestroyed) {
                    Toast.makeText(SongDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(Song song) {
        if (isDestroyed) return;

        binding.songTitle.setText(song.getTitle());
        binding.artistName.setText(song.getSinger());

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
                        if (isDestroyed) return false;

                        if (resource instanceof BitmapDrawable) {
                            Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                            Palette.from(bitmap).generate(palette -> {
                                if (palette != null && !isDestroyed) {
                                    runOnUiThread(() -> {
                                        if (!isDestroyed) {
                                            int dominantColor = palette.getDominantColor(getResources().getColor(R.color.dark_red));
                                            int vibrantColor = palette.getVibrantColor(dominantColor);
                                            binding.rootLayout.setBackgroundColor(vibrantColor);
                                            int textColor = isColorDark(vibrantColor) ?
                                                getResources().getColor(R.color.white) :
                                                getResources().getColor(R.color.black);
                                            binding.songTitle.setTextColor(textColor);
                                            binding.artistName.setTextColor(textColor);
                                        }
                                    });
                                }
                            });
                        }
                        return false;
                    }
                })
                .into(binding.albumArt);
        } else {
            binding.albumArt.setImageResource(R.drawable.ic_music_note);
        }
    }

    private void togglePlayPause() {
        if (isDestroyed) return;

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
        if (bound && !isUserSeeking && musicService != null && !isDestroyed) {
            int currentPosition = musicService.getCurrentPosition();
            int duration = musicService.getDuration();
            if (duration > 0) {
                binding.currentTime.setText(formatTime(currentPosition));
                binding.totalTime.setText(formatTime(duration));
                binding.seekBar.setMax(duration);
                binding.seekBar.setProgress(currentPosition);
            }
        }
    }

    private String formatTime(long milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void updatePlayPauseButton() {
        if (bound && !isDestroyed) {
            binding.playPauseButton.setImageResource(musicService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        }
    }

    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * android.graphics.Color.red(color) +
                             0.587 * android.graphics.Color.green(color) +
                             0.114 * android.graphics.Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    private void playNextSong() {
        if (isDestroyed) return;

        // Nếu danh sách rỗng, thêm id bài hát hiện tại vào
        if (playedSongIds.isEmpty() && currentSong != null) {
            playedSongIds.add(currentSong.getId());
            currentSongIndex = 0;
        }

        // Nếu đang ở giữa danh sách, chỉ phát lại bài tiếp theo trong list
        if (currentSongIndex < playedSongIds.size() - 1) {
            currentSongIndex++;
            String nextSongId = playedSongIds.get(currentSongIndex);

            SongApi songApi = RetrofitClient.getApiService(this);
            songApi.getSong(nextSongId).enqueue(new Callback<Song>() {
                @Override
                public void onResponse(@NonNull Call<Song> call, @NonNull Response<Song> response) {
                    if (isDestroyed) return;
                    if (response.isSuccessful() && response.body() != null) {
                        Song nextSong = response.body();
                        currentSong = nextSong;
                        runOnUiThread(() -> {
                            if (!isDestroyed) {
                                updateUI(nextSong);
                            }
                        });
                        if (bound && musicService != null) {
                            musicService.playSong(nextSong);
                        }
                    } else {
                        Toast.makeText(SongDetailActivity.this, "Không thể tải bài hát tiếp theo!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Song> call, @NonNull Throwable t) {
                    if (!isDestroyed) {
                        Toast.makeText(SongDetailActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return;
        }

        // Nếu đang ở cuối list, gọi API lấy bài mới
        Log.d(TAG, "Requesting next song with playedSongIds: " + playedSongIds);

        SongApi songApi = RetrofitClient.getApiService(this);
        SongIdsRequest request = new SongIdsRequest(new ArrayList<>(playedSongIds));

        songApi.getNextSong(request).enqueue(new Callback<Song>() {
            @Override
            public void onResponse(@NonNull Call<Song> call, @NonNull Response<Song> response) {
                Log.d(TAG, "Next song API response code: " + response.code());
                if (isDestroyed) return;

                if (response.isSuccessful() && response.body() != null) {
                    Song nextSong = response.body();
                    currentSong = nextSong;
                    playedSongIds.add(nextSong.getId());
                    currentSongIndex = playedSongIds.size() - 1;
                    runOnUiThread(() -> {
                        if (!isDestroyed) {
                            updateUI(nextSong);
                        }
                    });
                    if (bound && musicService != null) {
                        musicService.playSong(nextSong);
                    }
                } else {
                    Log.e(TAG, "Failed to get next song: " + response.code());
                    if (!isDestroyed) {
                        Toast.makeText(SongDetailActivity.this, "Không thể tải bài hát tiếp theo", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Song> call, @NonNull Throwable t) {
                Log.e(TAG, "Error getting next song", t);
                if (!isDestroyed) {
                    Toast.makeText(SongDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void playPreviousSong() {
        if (isDestroyed) return;

        // Nếu đang ở bài đầu tiên hoặc chưa có bài nào thì không làm gì
        if (currentSongIndex <= 0 || playedSongIds.isEmpty()) {
            Toast.makeText(this, "Không có bài trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lùi lại 1 vị trí
        currentSongIndex--;
        String prevSongId = playedSongIds.get(currentSongIndex);

        SongApi songApi = RetrofitClient.getApiService(this);
        songApi.getSong(prevSongId).enqueue(new Callback<Song>() {
            @Override
            public void onResponse(@NonNull Call<Song> call, @NonNull Response<Song> response) {
                if (isDestroyed) return;
                if (response.isSuccessful() && response.body() != null) {
                    Song prevSong = response.body();
                    currentSong = prevSong;
                    runOnUiThread(() -> {
                        if (!isDestroyed) {
                            updateUI(prevSong);
                        }
                    });
                    if (bound && musicService != null) {
                        musicService.playSong(prevSong);
                    }
                } else {
                    Toast.makeText(SongDetailActivity.this, "Không thể tải bài hát trước!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Song> call, @NonNull Throwable t) {
                if (!isDestroyed) {
                    Toast.makeText(SongDetailActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;

                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();

                if (Math.abs(diffY) > Math.abs(diffX)) {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            // Swipe down detected
                            handleBackButtonClick();
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector != null) {
            return gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }
        handler.removeCallbacks(updateTimeRunnable);
        binding = null;
        super.onDestroy();
    }
}
