package com.example.femobile.ui.auth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.femobile.R;
import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.SongApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SongDetailActivity extends AppCompatActivity {

    private static final String TAG = "SongDetailActivity";
    TextView songTitle, artistName, currentTime, totalTime;
    ImageView albumArt;
    ImageButton backButton;
    FloatingActionButton playPauseButton;
    private String songId;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private Song currentSong;
    private Handler handler;
    private Runnable updateTimeRunnable;

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

        // Initialize handler for updating time
        handler = new Handler(Looper.getMainLooper());
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimeDisplay();
                handler.postDelayed(this, 1000);
            }
        };

        // Get songId from intent
        songId = getIntent().getStringExtra("songId");
        if (songId != null) {
            loadSongDetails();
        }

        backButton.setOnClickListener(v -> {
            stopPlayback();
            Intent intent = new Intent(SongDetailActivity.this, SecondActivity.class);
            startActivity(intent);
            //animation
            overridePendingTransition(R.anim.stay, R.anim.slide_in_down);
        });

        playPauseButton.setOnClickListener(v -> togglePlayPause());
    }

    private void loadSongDetails() {
        SongApi songApi = RetrofitClient.getApiService(this);
        songApi.getSong(songId).enqueue(new Callback<Song>() {
            @Override
            public void onResponse(Call<Song> call, Response<Song> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentSong = response.body();
                    updateUI(currentSong);
                } else {
                    Toast.makeText(SongDetailActivity.this, "Không thể tải thông tin bài hát", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Song> call, Throwable t) {
                Toast.makeText(SongDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading song details", t);
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
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (resource instanceof BitmapDrawable) {
                            Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                            // Extract colors from the bitmap
                            Palette.from(bitmap).generate(palette -> {
                                if (palette != null) {
                                    // Get the dominant color
                                    int dominantColor = palette.getDominantColor(getResources().getColor(R.color.dark_red));
                                    
                                    // Get a vibrant color if available
                                    int vibrantColor = palette.getVibrantColor(dominantColor);
                                    
                                    // Apply the color to the background
                                    findViewById(R.id.rootLayout).setBackgroundColor(vibrantColor);
                                    
                                    // Adjust text colors based on background brightness
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
            Toast.makeText(this, "Không có bài hát để phát", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isPlaying) {
            pausePlayback();
        } else {
            startPlayback();
        }
    }

    private void startPlayback() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            try {
                String mediaUrl = currentSong.getMediaUrl();
                if (mediaUrl == null || mediaUrl.isEmpty()) {
                    Toast.makeText(this, "Không tìm thấy file nhạc", Toast.LENGTH_SHORT).show();
                }

                // Set up MediaPlayer
                mediaPlayer.setDataSource(mediaUrl);
                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    isPlaying = true;
                    updatePlayPauseButton();
                    // Start updating time display
                    handler.post(updateTimeRunnable);
                    // Set total time
                    totalTime.setText(formatTime(mp.getDuration()));
                });
                mediaPlayer.setOnCompletionListener(mp -> {
                    isPlaying = false;
                    updatePlayPauseButton();
                    // Stop updating time
                    handler.removeCallbacks(updateTimeRunnable);
                    // Reset current time
                    currentTime.setText(formatTime(0));
                });
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                    Toast.makeText(SongDetailActivity.this, "Lỗi phát nhạc", Toast.LENGTH_SHORT).show();
                    isPlaying = false;
                    updatePlayPauseButton();
                    // Stop updating time
                    handler.removeCallbacks(updateTimeRunnable);
                    return true;
                });

                // Prepare MediaPlayer asynchronously
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                Log.e(TAG, "Error preparing MediaPlayer", e);
                Toast.makeText(this, "Không thể phát bài hát này", Toast.LENGTH_SHORT).show();
                stopPlayback();
            }
        } else {
            mediaPlayer.start();
            isPlaying = true;
            updatePlayPauseButton();
            // Start updating time display
            handler.post(updateTimeRunnable);
        }
    }

    private void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            updatePlayPauseButton();
            // Stop updating time
            handler.removeCallbacks(updateTimeRunnable);
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping MediaPlayer", e);
            } finally {
                mediaPlayer = null;
                isPlaying = false;
                updatePlayPauseButton();
                // Stop updating time
                handler.removeCallbacks(updateTimeRunnable);
                // Reset time displays
                currentTime.setText(formatTime(0));
                totalTime.setText(formatTime(0));
            }
        }
    }

    private void updateTimeDisplay() {
        if (mediaPlayer != null && isPlaying) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            currentTime.setText(formatTime(currentPosition));
        }
    }

    private String formatTime(long milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void updatePlayPauseButton() {
        playPauseButton.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
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
        stopPlayback();
        // Remove any pending callbacks
        handler.removeCallbacks(updateTimeRunnable);
    }
}
