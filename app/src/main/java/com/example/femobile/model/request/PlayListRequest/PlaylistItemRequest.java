package com.example.femobile.model.request.PlayListRequest;

public class PlaylistItemRequest {
    private Long playlistId;
    private String songId;

    public PlaylistItemRequest(Long playlistId, String songId) {
        this.playlistId = playlistId;
        this.songId = songId;
    }

    public Long getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(Long playlistId) {
        this.playlistId = playlistId;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }
}
