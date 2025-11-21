package com.example.nhom4.ui.page.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
// Import các Adapter Chat/Operator nếu có
// import com.example.nhom4.ui.adapter.ChatAdapter;
// import com.example.nhom4.ui.adapter.OperatorAdapter;

public class DiscoveryFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private RecyclerView operatorRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discovery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        operatorRecyclerView = view.findViewById(R.id.OperatorRecyclerView);

        // Setup RecyclerViews
        // chatRecyclerView.setAdapter(new ChatAdapter(...));
        // operatorRecyclerView.setAdapter(new OperatorAdapter(...));

    }
}
