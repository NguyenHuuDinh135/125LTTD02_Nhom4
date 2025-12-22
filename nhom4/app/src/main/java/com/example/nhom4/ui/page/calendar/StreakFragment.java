package com.example.nhom4.ui.page.calendar;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.ui.adapter.StreakAdapter;
import com.example.nhom4.ui.viewmodel.StreakViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class StreakFragment extends Fragment {

    private StreakViewModel viewModel;
    private StreakAdapter calendarAdapter;

    private TextView tvCurrentMonth;
    private MaterialButton btnPrevMonth, btnNextMonth;
    private LinearLayout statsContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_streak, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadData();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StreakViewModel.class);
        initViews(view);
        setupCalendar();
        observeViewModel();
    }

    private void initViews(View view) {
        tvCurrentMonth = view.findViewById(R.id.tv_current_month);
        btnPrevMonth = view.findViewById(R.id.btn_prev_month);
        btnNextMonth = view.findViewById(R.id.btn_next_month);

        btnPrevMonth.setOnClickListener(v -> viewModel.prevMonth());
        btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());

        try {
            LinearLayout rootLayout = (LinearLayout) ((ViewGroup) view).getChildAt(0);
            MaterialCardView statsCard = (MaterialCardView) rootLayout.getChildAt(2);
            statsContainer = (LinearLayout) statsCard.getChildAt(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupCalendar() {
        RecyclerView recyclerView = getView().findViewById(R.id.calendar_recycler_view);
        calendarAdapter = new StreakAdapter();

        // [QUAN TRỌNG] Xử lý click vào item trên lịch
        calendarAdapter.setOnPostClickListener(post -> {
            if (post != null) {
                Intent intent = new Intent(getActivity(), StoryAllActivity.class);
                // Truyền ID bài viết cần xem để bên kia scroll tới
                intent.putExtra("TARGET_POST_ID", post.getPostId());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(calendarAdapter);
    }

    private void observeViewModel() {
        viewModel.getRawPosts().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                viewModel.processPosts(resource.data);
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getCalendarDays().observe(getViewLifecycleOwner(), days -> {
            calendarAdapter.setDays(days);
        });

        viewModel.getCurrentMonth().observe(getViewLifecycleOwner(), yearMonth -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tvCurrentMonth.setText("Tháng " + yearMonth.getMonthValue() + " " + yearMonth.getYear());
            }
        });

        viewModel.getStats().observe(getViewLifecycleOwner(), stats -> {
            updateStatsUI(stats.currentStreak, stats.totalMoods, stats.totalActivities, stats.totalPhotos);
        });
    }

    private void updateStatsUI(int streak, int moods, int activities, int photos) {
        if (statsContainer == null) return;
        int childCount = statsContainer.getChildCount();
        if (childCount > 1) {
            statsContainer.removeViews(1, childCount - 1);
        }
        addStatRow(statsContainer, "🔥 Chuỗi Streak hiện tại", streak + " ngày");
        addStatRow(statsContainer, "🙂 Cảm xúc đã chia sẻ", moods + "");
        addStatRow(statsContainer, "🏃 Hoạt động đã ghi lại", activities + "");
        addStatRow(statsContainer, "📷 Ảnh đã đăng", photos + "");
    }

    private void addStatRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 0);
        row.setLayoutParams(params);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvLabel = new TextView(getContext());
        tvLabel.setText(label);
        tvLabel.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView tvValue = new TextView(getContext());
        tvValue.setText(value);
        tvValue.setTextColor(getResources().getColor(android.R.color.black));
        tvValue.setTypeface(null, android.graphics.Typeface.BOLD);
        tvValue.setTextSize(16);

        row.addView(tvLabel);
        row.addView(tvValue);
        parent.addView(row);
    }
}