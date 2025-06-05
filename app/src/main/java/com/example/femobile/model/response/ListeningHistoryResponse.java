package com.example.femobile.model.response;

import com.example.femobile.model.request.SongRequest.Song;
import java.util.List;

public class ListeningHistoryResponse {
    private List<ListeningHistoryItem> listeningHistoryItemResponse;

    public List<ListeningHistoryItem> getListeningHistoryItemResponse() {
        return listeningHistoryItemResponse;
    }

    public static class ListeningHistoryItem {
        private Song songResponse;
        private String lastListenedAt;

        public Song getSongResponse() {
            return songResponse;
        }

        public String getLastListenedAt() {
            return lastListenedAt;
        }
    }
} 