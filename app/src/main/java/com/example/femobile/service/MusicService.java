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
    private Context attributionContext;

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Tạo attribution context cho Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                attributionContext = createAttributionContext("femobile_music_service");
                Log.d(TAG, "Attribution context created with tag: femobile_music_service");
            } catch (Exception e) {
                Log.w(TAG, "Failed to create attribution context, using default context", e);
                attributionContext = this;
            }
        } else {
            attributionContext = this;
        }

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
            channel.setDescription("Channel for music playback notifications");

            // Sử dụng attribution context khi có thể
            NotificationManager manager = (NotificationManager)
                    attributionContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(attributionContext, SongDetailActivity.class);
        if (currentSong != null) {
            notificationIntent.putExtra("songId", currentSong.getId());
            notificationIntent.putExtra("currentSong", currentSong);
            notificationIntent.putExtra("isPlaying", isPlaying);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                attributionContext,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(attributionContext, CHANNEL_ID)
                .setContentTitle(currentSong != null ? currentSong.getTitle() : "Music Player")
                .setContentText(currentSong != null ? currentSong.getSinger() : "No song playing")
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(isPlaying)
                .setShowWhen(false)
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

            // Set AudioAttributes thay vì setAudioStreamType
            android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
            mediaPlayer.setAudioAttributes(audioAttributes);

            // Set data source trực tiếp từ URL
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
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
                String errorMsg = getMediaPlayerErrorMessage(what, extra);
                Log.e(TAG, "Error details: " + errorMsg);

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
        } catch (SecurityException e) {
            Log.e(TAG, "Security error accessing media URL", e);
            stopPlayback();
        }
    }

    private String getMediaPlayerErrorMessage(int what, int extra) {
        String whatStr = "Unknown";
        String extraStr = "Unknown";

        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                whatStr = "MEDIA_ERROR_UNKNOWN";
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                whatStr = "MEDIA_ERROR_SERVER_DIED";
                break;
        }

        switch (extra) {
            case MediaPlayer.MEDIA_ERROR_IO:
                extraStr = "MEDIA_ERROR_IO";
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                extraStr = "MEDIA_ERROR_MALFORMED";
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                extraStr = "MEDIA_ERROR_UNSUPPORTED";
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                extraStr = "MEDIA_ERROR_TIMED_OUT";
                break;
        }

        return "what: " + whatStr + " (" + what + "), extra: " + extraStr + " (" + extra + ")";
    }

    public void pausePlayback() {
        Log.d(TAG, "Attempting to pause playback");
        if (mediaPlayer != null && isPrepared && isPlaying) {
            try {
                mediaPlayer.pause();
                isPlaying = false;
                // Cập nhật notification thay vì stop foreground
                startForeground(NOTIFICATION_ID, createNotification());
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
            }
        }
    }

    public boolean isPlaying() {
        return isPlaying && mediaPlayer != null && isPrepared;
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
                Log.d(TAG, "Seeked to position: " + position);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error seeking MediaPlayer", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "MusicService being destroyed");
        stopPlayback();
        super.onDestroy();
    }
}