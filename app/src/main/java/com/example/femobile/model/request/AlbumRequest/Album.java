package com.example.femobile.model.request.AlbumRequest;

import com.example.femobile.model.request.SongRequest.Song;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Album {
    private int id;
    private String title;
    private String coverUrl;
    @SerializedName("songs")
    private List<Song> listSong;

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
    public List<Song> getListSong() {
        return listSong;
    }

    public void setListSong(List<Song> listSong) {
        this.listSong = listSong;
    }
}
