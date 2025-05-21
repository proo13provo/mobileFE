package com.example.femobile.ui.auth.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.femobile.R;
import com.example.femobile.ui.auth.searchItemActivity;

public class searchFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_searching, container, false);
        
        // Find searchCardView and set click listener
        LinearLayout searchCardView = view.findViewById(R.id.searchCardView);
        searchCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), searchItemActivity.class);
                startActivity(intent);
            }
        });
        
        return view;
    }
}

