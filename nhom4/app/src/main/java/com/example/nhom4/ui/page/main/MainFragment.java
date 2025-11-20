package com.example.nhom4.ui.page.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.bean.Activity;
import com.example.nhom4.data.bean.Mood;
import com.example.nhom4.ui.adapter.ActivityAdapter;
import com.example.nhom4.ui.adapter.MoodAdapter;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private SwitchMaterial modeSwitch;
    private View checkInLayout;
    private RecyclerView activitiesListRecycler;
    private RecyclerView moodRecyclerView;

    private MoodAdapter moodAdapter;
    private ActivityAdapter activityAdapter;

    private List<Mood> moodList = new ArrayList<>();
    private List<Activity> fakeActivities = new ArrayList<>();

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        db = FirebaseFirestore.getInstance();

        modeSwitch = view.findViewById(R.id.modeSwitch);
        checkInLayout = view.findViewById(R.id.check_in_layout);
        activitiesListRecycler = view.findViewById(R.id.activities_list_recycler);
        moodRecyclerView = view.findViewById(R.id.mood_recycler_view);

        setupMoodRecycler();
        setupActivityRecycler();

        // Tạo dữ liệu giả cho activity (List dọc)
        if(fakeActivities.isEmpty()) {
            fakeActivities.add(new Activity("Plan content calendar"));
            fakeActivities.add(new Activity("Film a new video"));
        }
        activityAdapter.notifyDataSetChanged();

        // GỌI HÀM LẤY MOOD TỪ FIRESTORE
        fetchMoodsFromFirestore();

        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkInLayout.setVisibility(View.VISIBLE);
                activitiesListRecycler.setVisibility(View.GONE);
            } else {
                checkInLayout.setVisibility(View.GONE);
                activitiesListRecycler.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void setupMoodRecycler() {
        moodAdapter = new MoodAdapter(moodList);
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        moodRecyclerView.setAdapter(moodAdapter);
    }

    private void setupActivityRecycler() {
        activityAdapter = new ActivityAdapter(fakeActivities);
        activitiesListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        activitiesListRecycler.setAdapter(activityAdapter);
    }

    // --- LOGIC LẤY DỮ LIỆU ---
    // Trong MainFragment.java

    private void fetchMoodsFromFirestore() {
        db.collection("Mood")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    moodList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        // --- FIX STARTS HERE ---

                        // 1. Extract the data using the field names from your Firestore database.
                        // (Make sure "name", "iconUrl", "isPremium" match the fields in your Firebase console exactly)
                        String name = document.getString("name");
                        String iconUrl = document.getString("iconUrl");

                        // 2. Declare and retrieve 'isPremium'.
                        // Boolean.TRUE.equals is a safe way to convert a nullable Boolean to a primitive boolean.
                        boolean isPremium = Boolean.TRUE.equals(document.getBoolean("isPremium"));

                        // --- FIX ENDS HERE ---

                        // Log ra để xem có lấy được gì không
                        Log.d("MainFragment", "Lấy được mood: " + name + " - URL: " + iconUrl);

                        // Now the variables are defined, so this line will work
                        Mood mood = new Mood(name, iconUrl, isPremium);
                        moodList.add(mood);
                    }

                    // QUAN TRỌNG: Phải báo cho Adapter cập nhật
                    if (moodAdapter != null) {
                        moodAdapter.notifyDataSetChanged();
                    } else {
                        moodAdapter = new MoodAdapter(moodList);
                        moodRecyclerView.setAdapter(moodAdapter);
                    }

                    Log.d("MainFragment", "Tổng số mood: " + moodList.size());
                })
                .addOnFailureListener(e -> {
                    Log.e("MainFragment", "Lỗi lấy dữ liệu Firebase", e);
                });
    }


    // Hàm helper để chuyển tên icon trên server thành ID ảnh trong app
    // Bạn cần thay thế R.drawable.ic_xxx bằng hình ảnh thực tế bạn có trong res/drawable
    private int mapIconKeyToResourceId(String iconKey) {
        if (iconKey == null) return R.drawable.ic_launcher_background;

        switch (iconKey.toLowerCase()) {
            case "happy":
            case "hanh_phuc":
                return R.drawable.ic_launcher_background; // Thay bằng R.drawable.ic_happy

            case "sad":
            case "buon":
                return R.drawable.ic_launcher_background; // Thay bằng R.drawable.ic_sad

            case "angry":
            case "tuc_gian":
                return R.drawable.ic_launcher_background; // Thay bằng R.drawable.ic_angry

            case "surprised":
                return R.drawable.ic_launcher_background;

            default:
                return R.drawable.ic_launcher_background; // Icon mặc định nếu không tìm thấy
        }
    }
}
