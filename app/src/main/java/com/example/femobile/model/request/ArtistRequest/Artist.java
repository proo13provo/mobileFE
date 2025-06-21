package com.example.femobile.model.request.ArtistRequest;

public class Artist {
    private int id;
    private String username;
    private String email;
    private String urlAvatar;


    public Artist(int id, String username, String email, String urlAvatar) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.urlAvatar = urlAvatar;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrlAvatar() {
        return urlAvatar;
    }

    public void setUrlAvatar(String urlAvatar) {
        this.urlAvatar = urlAvatar;
    }
}
