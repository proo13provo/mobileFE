package com.example.femobile.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.femobile.R;
import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.ui.auth.SongDetailActivity;

import java.io.IOException;

public class MusicService extends Service {
    private static final String TAG = "MusicService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "MusicServiceChannel";
    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private Song currentSong;
    private boolean isPlaying = false;
    private boolean isPrepared = false;

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Log.d(TAG, "MusicService created");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "MusicService bound");
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "MusicService started");
        if (currentSong != null) {
            startForeground(NOTIFICATION_ID, createNotification());
        }
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Music Service Channel",
                NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, SongDetailActivity.class);
        if (currentSong != null) {
            notificationIntent.putExtra("songId", currentSong.getId());
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentSong != null ? currentSong.getTitle() : "Music Player")
            .setContentText(currentSong != null ? currentSong.getSinger() : "No song playing")
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }

    public void playSong(Song song) {
        Log.d(TAG, "Attempting to play song: " + song.getTitle());
        
        if (currentSong != null && currentSong.getId().equals(song.getId())) {
            Log.d(TAG, "Same song, toggling play/pause");
            if (isPlaying) {
                pausePlayback();
            } else {
                resumePlayback();
            }
            return;
        }

        stopPlayback();
        currentSong = song;
        
        try {
            Log.d(TAG, "Creating new MediaPlayer");
            mediaPlayer = new MediaPlayer();
            isPrepared = false;

            String mediaUrl = song.getMediaUrl();
            if (mediaUrl == null || mediaUrl.isEmpty()) {
                Log.e(TAG, "Media URL is empty");
                return;
            }

            Log.d(TAG, "Setting data source: " + mediaUrl);
            
            // Set audio stream type
            mediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
            
            // Set data source
            mediaPlayer.setDataSource(mediaUrl);
            
            mediaPlayer.setOnPreparedListener(mp -> {
                Log.d(TAG, "MediaPlayer prepared");
                isPrepared = true;
                try {
                    mp.start();
                    isPlaying = true;
                    startForeground(NOTIFICATION_ID, createNotification());
                    Log.d(TAG, "Playback started successfully");
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Error starting playback after preparation", e);
                }
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "Playback completed");
                isPlaying = false;
                isPrepared = false;
                stopForeground(true);
                stopSelf();
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                isPlaying = false;
                isPrepared = false;
                stopForeground(true);
                return true;
            });

            Log.d(TAG, "Starting async preparation");
            mediaPlayer.prepareAsync();
            
        } catch (IOException e) {
            Log.e(TAG, "Error preparing MediaPlayer", e);
            stopPlayback();
        }
    }

    public void pausePlayback() {
        Log.d(TAG, "Attempting to pause playback");
        if (mediaPlayer != null && isPrepared && isPlaying) {
            try {
                mediaPlayer.pause();
                isPlaying = false;
                stopForeground(true);
                Log.d(TAG, "Playback paused successfully");
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error pausing MediaPlayer", e);
            }
        } else {
            Log.d(TAG, "Cannot pause: mediaPlayer=" + (mediaPlayer != null) + 
                      ", isPrepared=" + isPrepared + ", isPlaying=" + isPlaying);
        }
    }

    public void resumePlayback() {
        Log.d(TAG, "Attempting to resume playback");
        if (mediaPlayer != null && isPrepared && !isPlaying) {
            try {
                mediaPlayer.start();
                isPlaying = true;
                startForeground(NOTIFICATION_ID, createNotification());
                Log.d(TAG, "Playback resumed successfully");
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error resuming MediaPlayer", e);
            }
        } else {
            Log.d(TAG, "Cannot resume: mediaPlayer=" + (mediaPlayer != null) + 
                      ", isPrepared=" + isPrepared + ", isPlaying=" + isPlaying);
        }
    }

    public void stopPlayback() {
        Log.d(TAG, "Stopping playback");
        if (mediaPlayer != null) {
            try {
                if (isPlaying) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                Log.d(TAG, "MediaPlayer released successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error stopping MediaPlayer", e);
            } finally {
                mediaPlayer = null;
                isPlaying = false;
                isPrepared = false;
                stopForeground(true);
                stopSelf();
            }
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null && isPrepared) {
            try {
                return mediaPlayer.getCurrentPosition();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error getting current position", e);
            }
        }
        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null && isPrepared) {
            try {
                return mediaPlayer.getDuration();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error getting duration", e);
            }
        }
        return 0;
    }

    public void seekTo(int position) {
        if (mediaPlayer != null && isPrepared) {
            try {
                mediaPlayer.seekTo(position);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error seeking MediaPlayer", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "MusicService being destroyed");
        super.onDestroy();
        stopPlayback();
    }
} 