package com.example.nhom4.ui.page.calendar;

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

    // UI Components
    private TextView tvCurrentMonth;
    private MaterialButton btnPrevMonth, btnNextMonth;

    // Layout chứa thống kê (tìm trong CardView thứ 2)
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
            viewModel.loadData(); // Gọi hàm này để tải lại dữ liệu mới nhất từ Firebase
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(StreakViewModel.class);

        // 2. Ánh xạ View
        initViews(view);

        // 3. Cài đặt RecyclerView Lịch
        setupCalendar();

        // 4. Lắng nghe dữ liệu thay đổi
        observeViewModel();
    }

    private void initViews(View view) {
        tvCurrentMonth = view.findViewById(R.id.tv_current_month);
        btnPrevMonth = view.findViewById(R.id.btn_prev_month);
        btnNextMonth = view.findViewById(R.id.btn_next_month);

        // Nút chuyển tháng
        btnPrevMonth.setOnClickListener(v -> viewModel.prevMonth());
        btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());

        // Tìm LinearLayout chứa thống kê
        // Dựa trên XML bạn gửi: CardView thứ 2 -> LinearLayout -> (TextView Tiêu đề + Nội dung thống kê)
        // Ta sẽ tìm CardView thứ 2 trong LinearLayout chính, sau đó lấy LinearLayout con của nó
        try {
            // LinearLayout gốc (child của NestedScrollView)
            LinearLayout rootLayout = (LinearLayout) ((ViewGroup) view).getChildAt(0);

            // CardView thứ 2 (index 2 vì: 0=Tiêu đề, 1=Card Lịch, 2=Card Thống kê)
            MaterialCardView statsCard = (MaterialCardView) rootLayout.getChildAt(2);

            // LinearLayout bên trong CardView
            statsContainer = (LinearLayout) statsCard.getChildAt(0);
        } catch (Exception e) {
            e.printStackTrace();
            // Nếu không tìm thấy theo index, có thể dùng findViewById nếu bạn đã đặt ID cho LinearLayout thống kê trong XML
        }
    }

    private void setupCalendar() {
        RecyclerView recyclerView = getView().findViewById(R.id.calendar_recycler_view);
        calendarAdapter = new StreakAdapter();
        recyclerView.setAdapter(calendarAdapter);
    }

    private void observeViewModel() {
        // 1. Quan sát dữ liệu raw từ Firebase
        viewModel.getRawPosts().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                viewModel.processPosts(resource.data);
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Quan sát danh sách ngày hiển thị trên lịch
        viewModel.getCalendarDays().observe(getViewLifecycleOwner(), days -> {
            calendarAdapter.setDays(days);
        });

        // 3. Quan sát tháng hiện tại để cập nhật Tiêu đề
        viewModel.getCurrentMonth().observe(getViewLifecycleOwner(), yearMonth -> {
            // Format: Tháng 3 2025
            String text = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                text = "Tháng " + yearMonth.getMonthValue() + " " + yearMonth.getYear();
            }
            tvCurrentMonth.setText(text);
        });

        // 4. Quan sát Thống kê (Streak, Mood, Activity, Photo)
        viewModel.getStats().observe(getViewLifecycleOwner(), stats -> {
            updateStatsUI(stats.currentStreak, stats.totalMoods, stats.totalActivities, stats.totalPhotos);
        });
    }

    private void updateStatsUI(int streak, int moods, int activities, int photos) {
        if (statsContainer == null) return;

        // Giữ lại tiêu đề "Thống kê", xóa các dòng dữ liệu cũ (nếu có)
        // Giả sử TextView tiêu đề là child đầu tiên (index 0)
        int childCount = statsContainer.getChildCount();
        if (childCount > 1) {
            statsContainer.removeViews(1, childCount - 1);
        }

        // Thêm các dòng thống kê mới
        addStatRow(statsContainer, "🔥 Chuỗi Streak hiện tại", streak + " ngày");
        addStatRow(statsContainer, "🙂 Cảm xúc đã chia sẻ", moods + "");
        addStatRow(statsContainer, "🏃 Hoạt động đã ghi lại", activities + "");
        addStatRow(statsContainer, "📷 Ảnh đã đăng", photos + "");
    }

    // Helper: Tạo dòng thống kê động bằng code Java thay vì XML cứng
    private void addStatRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 0); // Margin top
        row.setLayoutParams(params);
        row.setOrientation(LinearLayout.HORIZONTAL);

        // Label bên trái
        TextView tvLabel = new TextView(getContext());
        tvLabel.setText(label);
        tvLabel.setTextColor(getResources().getColor(android.R.color.darker_gray)); // Hoặc lấy từ theme
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        // Value bên phải
        TextView tvValue = new TextView(getContext());
        tvValue.setText(value);
        tvValue.setTextColor(getResources().getColor(android.R.color.black)); // Hoặc lấy từ theme
        tvValue.setTypeface(null, android.graphics.Typeface.BOLD);
        tvValue.setTextSize(16);

        row.addView(tvLabel);
        row.addView(tvValue);
        parent.addView(row);
    }
}
