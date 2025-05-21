package com.example.femobile.model.request.SongRequest;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Song implements Parcelable {
    private String id;
    private String title;
    private String mediaUrl;
    private String imageUrl;
    private String singer;
    private String album;
    private String author;
    private List<String> genres;

    // Default constructor
    public Song() {
    }

    protected Song(Parcel in) {
        id = in.readString();
        title = in.readString();
        mediaUrl = in.readString();
        imageUrl = in.readString();
        singer = in.readString();
        album = in.readString();
        author = in.readString();
        genres = new ArrayList<>();
        in.readStringList(genres);
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(mediaUrl);
        dest.writeString(imageUrl);
        dest.writeString(singer);
        dest.writeString(album);
        dest.writeString(author);
        dest.writeStringList(genres);
    }
}
