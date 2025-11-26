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

    // [QUAN TRỌNG] Map: Ngày -> Link Ảnh (Để hiển thị lên lịch)
    private Map<LocalDate, String> postedDataMap = new HashMap<>();

    public StreakViewModel() {
        repository = new StreakRepository();
        currentMonth.setValue(YearMonth.now());
        // Load dữ liệu ngay khi khởi tạo
        loadData();
    }

    public void loadData() {
        // Repository đã tự lấy Current User ID
        repository.getAllUserPosts(rawPosts);
    }

    // Getters
    public LiveData<Resource<List<Post>>> getRawPosts() { return rawPosts; }
    public LiveData<List<CalendarDay>> getCalendarDays() { return calendarDays; }
    public LiveData<StreakStats> getStats() { return stats; }
    public LiveData<YearMonth> getCurrentMonth() { return currentMonth; }

    // Xử lý logic chính
    public void processPosts(List<Post> posts) {
        postedDataMap.clear();
        StreakStats currentStats = new StreakStats();

        for (Post post : posts) {
            if (post.getCreatedAt() == null) continue;

            // Chuyển đổi Timestamp Firebase sang LocalDate theo múi giờ của điện thoại
            LocalDate date = post.getCreatedAt().toDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            // Ưu tiên lấy ảnh chụp (PhotoUrl), nếu không có thì lấy icon cảm xúc (MoodIconUrl)
            String imgUrl = post.getPhotoUrl();
            if (imgUrl == null || imgUrl.isEmpty()) {
                imgUrl = post.getMoodIconUrl();
            }

            // Lưu vào Map để hiển thị lên lịch
            // Logic: Nếu ngày đó chưa có trong map HOẶC bài viết này có ảnh (ưu tiên hiện ảnh hơn là icon)
            if (!postedDataMap.containsKey(date) || (imgUrl != null && !imgUrl.isEmpty())) {
                postedDataMap.put(date, imgUrl);
            }

            // Thống kê tổng số lượng
            if ("mood".equals(post.getType())) currentStats.totalMoods++;
            if ("activity".equals(post.getType())) currentStats.totalActivities++;
            if (post.getPhotoUrl() != null && !post.getPhotoUrl().isEmpty()) currentStats.totalPhotos++;
        }

        // Tính chuỗi Streak hiện tại
        currentStats.currentStreak = calculateCurrentStreak();
        stats.setValue(currentStats);

        // Vẽ lại lịch
        generateCalendarDays(currentMonth.getValue());
    }

    private int calculateCurrentStreak() {
        int streak = 0;
        LocalDate checkDate = LocalDate.now(); // Ngày hôm nay trên điện thoại

        // Logic:
        // 1. Nếu hôm nay CÓ đăng bài -> Bắt đầu đếm từ hôm nay lùi về.
        // 2. Nếu hôm nay CHƯA đăng bài -> Kiểm tra hôm qua.
        //    - Nếu hôm qua CÓ đăng -> Bắt đầu đếm từ hôm qua lùi về (Streak chưa bị đứt).
        //    - Nếu hôm qua KHÔNG đăng -> Streak = 0.

        if (!postedDataMap.containsKey(checkDate)) {
            // Hôm nay chưa đăng, check hôm qua
            checkDate = checkDate.minusDays(1);
            if (!postedDataMap.containsKey(checkDate)) {
                return 0; // Mất chuỗi
            }
        }

        // Vòng lặp đếm lùi về quá khứ
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

        // Lấy thứ của ngày đầu tháng (1 = Thứ 2, ..., 7 = Chủ Nhật)
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();

        // 1. Thêm các ngày của tháng trước (để lấp đầy hàng đầu tiên)
        for (int i = 1; i < dayOfWeek; i++) {
            LocalDate prevDate = firstDayOfMonth.minusDays(dayOfWeek - i);
            String img = postedDataMap.get(prevDate);
            days.add(new CalendarDay(prevDate, false, postedDataMap.containsKey(prevDate), img));
        }

        // 2. Thêm các ngày của tháng hiện tại
        for (int i = 1; i <= lastDayOfMonth.getDayOfMonth(); i++) {
            LocalDate date = yearMonth.atDay(i);
            String img = postedDataMap.get(date);
            days.add(new CalendarDay(date, true, postedDataMap.containsKey(date), img));
        }

        // 3. Thêm các ngày của tháng sau (để lấp đầy lưới 42 ô - cho đẹp đội hình)
        int remaining = 42 - days.size();
        for (int i = 1; i <= remaining; i++) {
            LocalDate nextDate = lastDayOfMonth.plusDays(i);
            String img = postedDataMap.get(nextDate);
            days.add(new CalendarDay(nextDate, false, postedDataMap.containsKey(nextDate), img));
        }

        calendarDays.setValue(days);
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
