package com.example.femobile.model.response;

import com.example.femobile.model.request.AlbumRequest.Album;

import java.util.List;

public class AlbumResponse {
    private List<Album> content;

    public List<Album> getContent() {
        return content;
    }

    public void setContent(List<Album> content) {
        this.content = content;
    }
}
