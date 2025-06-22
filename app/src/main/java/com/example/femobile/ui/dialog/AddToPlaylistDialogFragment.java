package com.example.femobile.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.femobile.adapter.PlaylistAdapter;
import com.example.femobile.databinding.DialogAddToPlaylistBinding;
import com.example.femobile.model.response.PlayListResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.api.PlayListApi;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.EditText;
import com.example.femobile.model.request.PlayListRequest.PlayListRequest;
import com.example.femobile.security.TokenManager;
import com.example.femobile.model.request.PlayListRequest.PlaylistItemRequest;

public class AddToPlaylistDialogFragment extends DialogFragment {
    private DialogAddToPlaylistBinding binding;
    private String songId;
    private PlaylistAdapter adapter;
    private Long selectedPlaylistId = null;
    private Long lastCreatedPlaylistId = null;

    public static AddToPlaylistDialogFragment newInstance(String songId) {
        AddToPlaylistDialogFragment fragment = new AddToPlaylistDialogFragment();
        Bundle args = new Bundle();
        args.putString("songId", songId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogAddToPlaylistBinding.inflate(LayoutInflater.from(getContext()));
        songId = getArguments().getString("songId");

        // Setup RecyclerView
        adapter = new PlaylistAdapter();
        binding.rvPlaylists.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPlaylists.setAdapter(adapter);

        // Load playlists
        loadPlaylists();

        // Tạo playlist mới
        binding.btnNewPlaylist.setOnClickListener(v -> showCreatePlaylistDialog());
//
//         Chọn playlist
        adapter.setOnItemClickListener(playlist -> {
            selectedPlaylistId = playlist.getId();
            adapter.setSelectedPlaylistId(selectedPlaylistId);
        });

        // Done
        binding.btnDone.setOnClickListener(v -> {
            if (selectedPlaylistId != null) {
                addSongToPlaylist(selectedPlaylistId, songId);
            } else {
                Toast.makeText(getContext(), "Chọn playlist!", Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel
        binding.btnCancel.setOnClickListener(v -> dismiss());

        return new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .create();
    }

    private void loadPlaylists() {
        PlayListApi api = RetrofitClient.getplayListApi(getContext());
        TokenManager tokenManager = new TokenManager(requireContext());
        String token = tokenManager.getValidAccessToken();
        api.getPlayLists(token).enqueue(new Callback<List<PlayListResponse>>() {
            @Override
            public void onResponse(Call<List<PlayListResponse>> call, Response<List<PlayListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setPlaylists(response.body());
                    // Nếu vừa tạo playlist, tự động chọn nó
                    if (lastCreatedPlaylistId != null) {
                        adapter.setSelectedPlaylistId(lastCreatedPlaylistId);
                        lastCreatedPlaylistId = null;
                    }
                }
            }
            @Override
            public void onFailure(Call<List<PlayListResponse>> call, Throwable t) {}
        });
    }

    private void showCreatePlaylistDialog() {
        Context context = getContext();
        if (context == null) return;

        final EditText input = new EditText(context);
        input.setHint("Nhập tên playlist");

        new AlertDialog.Builder(context)
            .setTitle("Tạo playlist mới")
            .setView(input)
            .setPositiveButton("Done", (dialog, which) -> {
                String playlistName = input.getText().toString().trim();
                if (!playlistName.isEmpty()) {
                    createNewPlaylist(playlistName);
                } else {
                    Toast.makeText(context, "Tên playlist không được để trống", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void createNewPlaylist(String name) {
        PlayListApi api = RetrofitClient.getplayListApi(getContext());
        TokenManager tokenManager = new TokenManager(requireContext());
        String token = tokenManager.getValidAccessToken();
        PlayListRequest request = new PlayListRequest();
        request.setName(name);

        api.addPlayList(token, request).enqueue(new Callback<PlayListResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlayListResponse> call, @NonNull Response<PlayListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Tạo playlist thành công!", Toast.LENGTH_SHORT).show();
                    lastCreatedPlaylistId = response.body().getId(); // Lưu lại id playlist vừa tạo
                    loadPlaylists(); // Load lại danh sách
                } else {
                    Toast.makeText(getContext(), "Tạo playlist thất bại!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<PlayListResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSongToPlaylist(Long playlistId, String songId) {
        PlayListApi api = RetrofitClient.getplayListApi(getContext());
        TokenManager tokenManager = new TokenManager(requireContext());
        String token = tokenManager.getValidAccessToken();
        PlaylistItemRequest request = new PlaylistItemRequest(playlistId, songId);

        api.addSongToPlaylist(token, request).enqueue(new Callback<PlaylistItemRequest>() {
            @Override
            public void onResponse(@NonNull Call<PlaylistItemRequest> call, @NonNull Response<PlaylistItemRequest> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã thêm vào playlist!", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    String errorMsg = "Thêm vào playlist thất bại!";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " " + response.errorBody().string();
                            Log.e("AddToPlaylistDialogFragment", errorMsg);
                        } catch (Exception e) {
                            errorMsg += " (Không đọc được lỗi chi tiết)";
                        }
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<PlaylistItemRequest> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}