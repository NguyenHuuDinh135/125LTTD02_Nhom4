package com.example.nhom4.ui.page.main;

import static java.security.AccessController.getContext;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.bean.Activity;
import com.example.nhom4.data.bean.Mood;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    // Khai báo Views
    private SwitchMaterial modeSwitch;
    private View checkInLayout;
    private RecyclerView activitiesListRecycler;
    private RecyclerView moodRecyclerView;

    // Khai báo Adapters
    private MoodAdapter moodAdapter;
    private ActivityAdapter activityAdapter;

    // Khai báo Dữ liệu giả
    private List<Mood> fakeMoods = new ArrayList<>();
    private List<Activity> fakeActivities = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // 1. Ánh xạ Views (Tìm bằng ID)
        modeSwitch = view.findViewById(R.id.modeSwitch);
        checkInLayout = view.findViewById(R.id.check_in_layout);
        activitiesListRecycler = view.findViewById(R.id.activities_list_recycler);
        moodRecyclerView = view.findViewById(R.id.mood_recycler_view);

        // 2. Tạo dữ liệu giả (Chỉ chạy 1 lần)
        if (fakeMoods.isEmpty()) {
            createFakeData();
        }

        // 3. Cài đặt RecyclerViews
        setupMoodRecycler();
        setupActivityRecycler();

        // 4. Cài đặt logic cho Switch (Công tắc)
        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Chế độ Check-in (Icons)
                checkInLayout.setVisibility(View.VISIBLE);
                activitiesListRecycler.setVisibility(View.GONE);
            } else {
                // Chế độ Hoạt động (List)
                checkInLayout.setVisibility(View.GONE);
                activitiesListRecycler.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void createFakeData() {
        // Dữ liệu cho Mode 2 (Icon)
        // TODO: Bạn cần tạo các file drawable này trong res/drawable/
        // Ví dụ: ic_mood_sad, ic_mood_happy, ...
        fakeMoods.add(new Mood("Buồn", R.drawable.ic_launcher_background));
        fakeMoods.add(new Mood("Hạnh phúc", R.drawable.ic_launcher_background));
        fakeMoods.add(new Mood("Tức giận", R.drawable.ic_launcher_background));
        fakeMoods.add(new Mood("Ngạc nhiên", R.drawable.ic_launcher_background));

        // Dữ liệu cho Mode 1 (List)
        fakeActivities.add(new Activity("Plan content calendar"));
        fakeActivities.add(new Activity("Film a new video"));
        fakeActivities.add(new Activity("Pay credit card bill"));
        fakeActivities.add(new Activity("Collect parcel from locker"));
        fakeActivities.add(new Activity("Pack my luggage"));
        fakeActivities.add(new Activity("Write blog post"));
    }

    private void setupMoodRecycler() {
        moodAdapter = new MoodAdapter(fakeMoods);
        // Cài đặt layout ngang cho mood
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        moodRecyclerView.setAdapter(moodAdapter);
    }

    private void setupActivityRecycler() {
        activityAdapter = new ActivityAdapter(fakeActivities);
        // Cài đặt layout dọc cho activity
        activitiesListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        activitiesListRecycler.setAdapter(activityAdapter);
    }
}