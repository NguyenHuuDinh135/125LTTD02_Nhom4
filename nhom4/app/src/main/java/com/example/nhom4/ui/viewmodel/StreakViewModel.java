package com.example.nhom4.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.CalendarDay;
import com.example.nhom4.data.bean.MonthData;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.data.bean.StreakStats;
import com.example.nhom4.data.repository.StreakRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreakViewModel extends ViewModel {

    private final StreakRepository repository;
    private final MutableLiveData<Resource<List<Post>>> rawPosts = new MutableLiveData<>();

    // [THAY ĐỔI] List chứa danh sách các tháng
    private final MutableLiveData<List<MonthData>> monthList = new MutableLiveData<>();
    private final MutableLiveData<StreakStats> stats = new MutableLiveData<>();
    private final MutableLiveData<Date> firstPostDate = new MutableLiveData<>();

    private Map<LocalDate, Post> postedDataMap = new HashMap<>();

    public StreakViewModel() {
        repository = new StreakRepository();
        repository.getAllUserPosts(rawPosts);
    }

    public LiveData<Resource<List<Post>>> getRawPosts() { return rawPosts; }
    public LiveData<List<MonthData>> getMonthList() { return monthList; }
    public LiveData<StreakStats> getStats() { return stats; }
    public LiveData<Date> getFirstPostDate() { return firstPostDate; }

    public void processPosts(List<Post> posts) {
        postedDataMap.clear();
        StreakStats currentStats = new StreakStats();

        if (posts == null) posts = new ArrayList<>();

        // 1. Sort để tìm ngày đầu tiên và tính toán đúng thứ tự
        posts.sort(Comparator.comparing(Post::getCreatedAt).reversed()); // Mới nhất lên đầu

        Date firstDate = null;
        if (!posts.isEmpty()) {
            Post oldest = posts.get(posts.size() - 1);
            if (oldest.getCreatedAt() != null) firstDate = oldest.getCreatedAt().toDate();
        }
        firstPostDate.setValue(firstDate);

        // 2. Map dữ liệu vào HashMap để truy xuất nhanh theo ngày
        for (Post post : posts) {
            if (post.getCreatedAt() == null) continue;
            LocalDate date = post.getCreatedAt().toDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            // Logic hiển thị: Nếu ngày đó có nhiều bài, ưu tiên bài có ảnh/mood
            boolean hasImage = (post.getPhotoUrl() != null && !post.getPhotoUrl().isEmpty()) ||
                    (post.getMoodIconUrl() != null && !post.getMoodIconUrl().isEmpty());

            if (!postedDataMap.containsKey(date) || hasImage) {
                postedDataMap.put(date, post);
            }

            // Tính Stats
            if ("mood".equals(post.getType())) currentStats.totalMoods++;
            if ("activity".equals(post.getType())) currentStats.totalActivities++;
            if (hasImage) currentStats.totalPhotos++; // Đếm tổng Locket
        }

        currentStats.currentStreak = calculateCurrentStreak();
        stats.setValue(currentStats);

        // 3. Tạo danh sách các tháng (Generate Months)
        generateMonths(firstDate);
    }

    private int calculateCurrentStreak() {
        int streak = 0;
        LocalDate checkDate = LocalDate.now();
        // Nếu hôm nay chưa đăng, kiểm tra hôm qua. Nếu hôm qua có -> tính tiếp.
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

    private void generateMonths(Date firstPostDate) {
        List<MonthData> list = new ArrayList<>();

        YearMonth startMonth;
        if (firstPostDate != null) {
            startMonth = YearMonth.from(firstPostDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        } else {
            startMonth = YearMonth.now(); // Nếu chưa có bài nào, chỉ hiện tháng này
        }

        YearMonth current = YearMonth.now();

        // Loop từ tháng hiện tại lùi về quá khứ cho đến tháng bắt đầu
        while (!current.isBefore(startMonth)) {
            List<CalendarDay> daysInMonth = generateDaysForMonth(current);
            list.add(new MonthData(current, daysInMonth));
            current = current.minusMonths(1);
        }

        monthList.setValue(list);
    }

    private List<CalendarDay> generateDaysForMonth(YearMonth yearMonth) {
        List<CalendarDay> days = new ArrayList<>();
        LocalDate firstDay = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        // Tính padding đầu tuần (Giả sử T2 là đầu tuần -> T2=1)
        int dayOfWeek = firstDay.getDayOfWeek().getValue();
        int paddingBefore = dayOfWeek - 1;

        // Thêm các ô trống đầu tháng
        for (int i = 0; i < paddingBefore; i++) {
            days.add(null);
        }

        // Thêm các ngày thực tế
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = yearMonth.atDay(i);
            Post post = postedDataMap.get(date);
            boolean hasPost = (post != null);

            String thumb = null;
            if (hasPost) {
                thumb = post.getPhotoUrl();
                if (thumb == null || thumb.isEmpty()) thumb = post.getMoodIconUrl();
            }

            // isCurrentMonth luôn là true trong ngữ cảnh logic này vì ta đang build từng tháng riêng biệt
            days.add(new CalendarDay(date, true, hasPost, thumb, post));
        }

        return days;
    }

    public void loadData() {
        repository.getAllUserPosts(rawPosts);
    }
}