package com.example.femobile.model.request.SongRequest;

import java.util.List;

public class SongIdsRequest {
    private List<String> songIds;

    public SongIdsRequest() {
    }

    public SongIdsRequest(List<String> songIds) {
        this.songIds = songIds;
    }

    public List<String> getSongIds() {
        return songIds;
    }

    public void setSongIds(List<String> songIds) {
        this.songIds = songIds;
    }
} 