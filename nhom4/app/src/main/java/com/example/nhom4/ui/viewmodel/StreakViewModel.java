package com.example.nhom4.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.CalendarDay;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.data.bean.StreakStats;
import com.example.nhom4.data.repository.StreakRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreakViewModel extends ViewModel {

    private final StreakRepository repository;
    private final MutableLiveData<Resource<List<Post>>> rawPosts = new MutableLiveData<>();
    private final MutableLiveData<List<CalendarDay>> calendarDays = new MutableLiveData<>();
    private final MutableLiveData<StreakStats> stats = new MutableLiveData<>();
    private final MutableLiveData<YearMonth> currentMonth = new MutableLiveData<>();

    // [THAY ĐỔI] Map lưu Post thay vì chỉ String url
    private Map<LocalDate, Post> postedDataMap = new HashMap<>();

    public StreakViewModel() {
        repository = new StreakRepository();
        currentMonth.setValue(YearMonth.now());
        loadData();
    }

    public void loadData() {
        repository.getAllUserPosts(rawPosts);
    }

    public LiveData<Resource<List<Post>>> getRawPosts() { return rawPosts; }
    public LiveData<List<CalendarDay>> getCalendarDays() { return calendarDays; }
    public LiveData<StreakStats> getStats() { return stats; }
    public LiveData<YearMonth> getCurrentMonth() { return currentMonth; }

    public void processPosts(List<Post> posts) {
        postedDataMap.clear();
        StreakStats currentStats = new StreakStats();

        for (Post post : posts) {
            if (post.getCreatedAt() == null) continue;

            LocalDate date = post.getCreatedAt().toDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            // Logic: Lưu Post vào map nếu ngày đó chưa có hoặc post này có ảnh (ưu tiên ảnh)
            boolean hasImage = post.getPhotoUrl() != null && !post.getPhotoUrl().isEmpty();

            if (!postedDataMap.containsKey(date) || hasImage) {
                postedDataMap.put(date, post);
            }

            // Thống kê
            if ("mood".equals(post.getType())) currentStats.totalMoods++;
            if ("activity".equals(post.getType())) currentStats.totalActivities++;
            if (hasImage) currentStats.totalPhotos++;
        }

        currentStats.currentStreak = calculateCurrentStreak();
        stats.setValue(currentStats);
        generateCalendarDays(currentMonth.getValue());
    }

    private int calculateCurrentStreak() {
        int streak = 0;
        LocalDate checkDate = LocalDate.now();
        if (!postedDataMap.containsKey(checkDate)) {
            checkDate = checkDate.minusDays(1);
            if (!postedDataMap.containsKey(checkDate)) return 0;
        }
        while (postedDataMap.containsKey(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }
        return streak;
    }

    public void generateCalendarDays(YearMonth yearMonth) {
        if (yearMonth == null) return;
        List<CalendarDay> days = new ArrayList<>();
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();

        // Helper function để tạo CalendarDay từ Map
        //
        addDaysToGrid(days, firstDayOfMonth.minusDays(dayOfWeek - 1), dayOfWeek - 1, false); // Tháng trước
        addDaysToGrid(days, firstDayOfMonth, lastDayOfMonth.getDayOfMonth(), true);         // Tháng này
        addDaysToGrid(days, lastDayOfMonth.plusDays(1), 42 - days.size(), false);           // Tháng sau

        calendarDays.setValue(days);
    }

    private void addDaysToGrid(List<CalendarDay> list, LocalDate startDate, int count, boolean isCurrentMonth) {
        for (int i = 0; i < count; i++) {
            LocalDate date = startDate.plusDays(i);
            Post post = postedDataMap.get(date);
            boolean hasPost = (post != null);

            // Lấy URL thumbnail (ưu tiên ảnh -> mood icon)
            String thumb = null;
            if (hasPost) {
                thumb = post.getPhotoUrl();
                if (thumb == null || thumb.isEmpty()) thumb = post.getMoodIconUrl();
            }

            list.add(new CalendarDay(date, isCurrentMonth, hasPost, thumb, post));
        }
    }

    public void nextMonth() {
        YearMonth next = currentMonth.getValue().plusMonths(1);
        currentMonth.setValue(next);
        generateCalendarDays(next);
    }

    public void prevMonth() {
        YearMonth prev = currentMonth.getValue().minusMonths(1);
        currentMonth.setValue(prev);
        generateCalendarDays(prev);
    }
}