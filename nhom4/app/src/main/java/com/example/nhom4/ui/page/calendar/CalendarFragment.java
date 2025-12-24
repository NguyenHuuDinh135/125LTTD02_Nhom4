package com.example.nhom4.ui.page.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.ui.adapter.MonthAdapter;
import com.example.nhom4.ui.viewmodel.StreakViewModel;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private StreakViewModel viewModel;
    private MonthAdapter monthAdapter;
    private TextView tvTotalLocket, tvCurrentStreak, tvFirstMoment;
    private View cardStats;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StreakViewModel.class);

        initViews(view);
        setupRecyclerView(view);
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.loadData();
    }

    private void initViews(View view) {
        tvTotalLocket = view.findViewById(R.id.tv_total_locket);
        tvCurrentStreak = view.findViewById(R.id.tv_current_streak);
        tvFirstMoment = view.findViewById(R.id.tv_first_moment);
        cardStats = view.findViewById(R.id.card_stats);
    }

    private void setupRecyclerView(View view) {
        RecyclerView rvMonths = view.findViewById(R.id.rv_months);

        // MonthAdapter giữ nguyên như code trước
        monthAdapter = new MonthAdapter(post -> {
            if (post != null) {
                Intent intent = new Intent(getActivity(), StoryAllActivity.class);
                intent.putExtra("TARGET_POST_ID", post.getPostId());
                startActivity(intent);
            }
        });

        rvMonths.setAdapter(monthAdapter);
    }

    private void observeViewModel() {
        // 1. Lấy dữ liệu và xử lý
        viewModel.getRawPosts().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                viewModel.processPosts(resource.data);
            } else if (resource.status == Resource.Status.ERROR) {
                // Xử lý lỗi nhẹ
            }
        });

        // 2. Hiển thị danh sách tháng
        viewModel.getMonthList().observe(getViewLifecycleOwner(), monthDataList -> {
            monthAdapter.setMonthList(monthDataList);
        });

        // 3. Hiển thị Thống kê (Stats)
        viewModel.getStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                // Hiển thị Card Stats nếu có dữ liệu
                cardStats.setVisibility(View.VISIBLE);
                tvTotalLocket.setText(stats.totalPhotos + " Locket");
                tvCurrentStreak.setText(stats.currentStreak + "d chuỗi");
            } else {
                cardStats.setVisibility(View.GONE);
            }
        });

        // 4. Hiển thị ngày bắt đầu (First Moment)
        viewModel.getFirstPostDate().observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd 'thg' MM, yyyy", Locale.getDefault());
                tvFirstMoment.setText("Tham gia từ " + sdf.format(date));
                tvFirstMoment.setVisibility(View.VISIBLE);
            } else {
                tvFirstMoment.setVisibility(View.GONE);
            }
        });
    }
}