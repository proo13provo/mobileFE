package com.example.femobile.model.response;

import com.google.gson.annotations.SerializedName; // Cần import này
import com.example.femobile.model.request.SongRequest.Song;
import java.util.List;

public class SongResponse {
    private boolean success;
    private String message;
    @SerializedName("songs") // <-- Ánh xạ key "songs" từ JSON vào trường data
    private List<Song> data;

    // ... getters and setters giữ nguyên tên data
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Song> getData() {
        return data;
    }

    public void setData(List<Song> data) {
        this.data = data;
    }
}