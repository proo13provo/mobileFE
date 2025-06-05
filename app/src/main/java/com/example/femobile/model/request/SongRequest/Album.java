package com.example.femobile.model.request.SongRequest;

public class Album {
    private int id;
    private String title;
    private String coverUrl;

    public Album(int id, String title, String coverUrl) {
        this.id = id;
        this.title = title;
        this.coverUrl = coverUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}
