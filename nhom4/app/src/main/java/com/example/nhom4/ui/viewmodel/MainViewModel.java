package com.example.nhom4.ui.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Activity;
import com.example.nhom4.data.bean.Mood;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.data.repository.AuthRepository;
import com.example.nhom4.data.repository.PostRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ViewModel trung tâm cho MainFragment/CenterFragment:
 * - Lấy feed bài viết, mood, activity tham gia
 * - Tạo bài post mới và xử lý mở khóa phần thưởng.
 */
public class MainViewModel extends ViewModel {

    private final PostRepository postRepository;
    private final AuthRepository authRepository;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final MutableLiveData<Resource<List<Post>>> posts = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<Mood>>> moods = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> uploadStatus = new MutableLiveData<>();

    // [MỚI] LiveData chứa danh sách Activity đã tham gia
    private final MutableLiveData<Resource<List<Activity>>> joinedActivities = new MutableLiveData<>();

    // [MỚI] LiveData báo hiệu vừa mở khóa Mood mới
    private final MutableLiveData<Resource<Mood>> unlockedReward = new MutableLiveData<>();

    public MainViewModel() {
        postRepository = new PostRepository();
        authRepository = new AuthRepository();

        loadPosts();
        loadMoods();
        loadJoinedActivities(); // Tự động load activity khi khởi tạo
    }

    public LiveData<Resource<List<Post>>> getPosts() { return posts; }
    public LiveData<Resource<List<Mood>>> getMoods() { return moods; }
    public LiveData<Resource<Boolean>> getUploadStatus() { return uploadStatus; }

    // Getters mới
    public LiveData<Resource<List<Activity>>> getJoinedActivities() { return joinedActivities; }
    public LiveData<Resource<Mood>> getUnlockedReward() { return unlockedReward; }

    private void loadPosts() {
        postRepository.getPosts(posts); // Snapshot listener trả dữ liệu feed
    }

    private void loadMoods() {
        db.collection("Mood").get()
                .addOnSuccessListener(snapshots -> {
                    List<Mood> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        // Fix lỗi null iconUrl nếu có
                        String iconUrl = doc.getString("iconUrl");
                        if (iconUrl == null) iconUrl = "";

                        list.add(new Mood(doc.getString("name"), iconUrl, Boolean.TRUE.equals(doc.getBoolean("isPremium"))));
                    }
                    moods.postValue(Resource.success(list));
                })
                .addOnFailureListener(e -> moods.postValue(Resource.error(e.getMessage(), null)));
    }

    // [MỚI] Load Activity User đang tham gia
    public void loadJoinedActivities() {
        if (authRepository.getCurrentUser() == null) return;
        String uid = authRepository.getCurrentUser().getUid();

        // Lắng nghe realtime để UI tự cập nhật khi có activity mới
        db.collection("activities")
                .whereArrayContains("participants", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        joinedActivities.postValue(Resource.error(error.getMessage(), null));
                        return;
                    }
                    if (value != null) {
                        List<Activity> list = value.toObjects(Activity.class);
                        joinedActivities.postValue(Resource.success(list));
                    }
                });
    }

    // [CẬP NHẬT] Hàm createPost nhận object Activity thay vì string title
    public void createPost(String caption, String imagePath, Mood mood, Activity activity) {
        if (authRepository.getCurrentUser() == null) return;

        Post post = new Post();
        post.setUserId(authRepository.getCurrentUser().getUid());
        post.setCaption(caption);
        post.setCreatedAt(com.google.firebase.Timestamp.now());

        if (mood != null) {
            post.setType("mood");
            post.setMoodName(mood.getName());
            post.setMoodIconUrl(mood.getIconUrl());
        } else if (activity != null) {
            // Nếu là Activity Post
            post.setType("activity");
            post.setActivityId(activity.getId()); // Lưu ID để query sau này
            post.setActivityTitle(activity.getTitle());
        } else {
            // Fallback nếu không chọn gì (tránh crash)
            post.setType("activity");
            post.setActivityTitle("Hoạt động khác");
        }

        Uri uri = imagePath != null ? Uri.parse("file://" + imagePath) : null;

        // Gọi repository để upload
        // Chúng ta cần wrap uploadStatus để hứng sự kiện Success và cập nhật Progress
        MutableLiveData<Resource<Boolean>> internalStatus = new MutableLiveData<>();
        internalStatus.observeForever(resource -> {
            uploadStatus.setValue(resource); // Chuyển tiếp trạng thái ra UI

            if (resource.status == Resource.Status.SUCCESS && activity != null) {
                // Nếu upload thành công và đây là bài post Activity -> Tăng điểm
                incrementActivityProgress(activity.getId());
            }
        });

        postRepository.createPost(post, uri, internalStatus);
    }

    // [MỚI] Tăng Progress và Kiểm tra quà
    private void incrementActivityProgress(String activityId) {
        if (activityId == null) return;

        db.collection("activities").document(activityId).get().addOnSuccessListener(doc -> {
            Activity act = doc.toObject(Activity.class);

            // Chỉ tăng nếu chưa nhận quà
            if (act != null && !act.isRewardClaimed()) {
                int newProgress = act.getProgress() + 1;

                // Cập nhật progress mới lên Firestore
                db.collection("activities").document(activityId).update("progress", newProgress);

                // Kiểm tra đủ target (ví dụ 10) chưa
                if (newProgress >= act.getTarget()) {
                    unlockRandomPremiumMood(activityId);
                }
            }
        });
    }

    // [MỚI] Mở khóa Mood Premium ngẫu nhiên
    /**
     * Random một mood premium và ghi nhận vào user khi hoàn thành target.
     */
    private void unlockRandomPremiumMood(String activityId) {
        if (authRepository.getCurrentUser() == null) return;
        String uid = authRepository.getCurrentUser().getUid();

        // List Mood Premium mẫu (Thực tế nên fetch từ collection 'PremiumMoods')
        List<Mood> premiums = new ArrayList<>();
        premiums.add(new Mood("Vua Hề", "https://img.icons8.com/emoji/96/clown-face.png", true));
        premiums.add(new Mood("Rich Kid", "https://img.icons8.com/emoji/96/money-mouth-face.png", true));
        premiums.add(new Mood("Siêu Nhân", "https://img.icons8.com/emoji/96/superhero.png", true));

        // Random
        if (premiums.isEmpty()) return;
        Mood reward = premiums.get(new Random().nextInt(premiums.size()));

        // 1. Đánh dấu activity đã nhận quà (để không nhận lại)
        db.collection("activities").document(activityId).update("isRewardClaimed", true);

        // 2. Lưu mood vào collection của user
        db.collection("users").document(uid).collection("unlockedMoods").add(reward)
                .addOnSuccessListener(v -> {
                    // Báo ra UI để hiện Dialog chúc mừng
                    unlockedReward.postValue(Resource.success(reward));
                });
    }
}
