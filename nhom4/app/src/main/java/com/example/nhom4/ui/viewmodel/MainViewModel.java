package com.example.nhom4.ui.viewmodel;

import android.net.Uri;
import com.example.nhom4.data.bean.PostFilterType;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Activity;
import com.example.nhom4.data.bean.ActivityLog;
import com.example.nhom4.data.bean.Mood;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.data.bean.User;
import com.example.nhom4.data.repository.AuthRepository;
import com.example.nhom4.data.repository.PostRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainViewModel extends ViewModel {

    private final PostRepository postRepository = new PostRepository();
    private final AuthRepository authRepository = new AuthRepository();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // LiveData
    private final MutableLiveData<Resource<List<Post>>> posts = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<Mood>>> moods = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<Activity>>> joinedActivities = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> uploadStatus = new MutableLiveData<>();

    // MỚI: Cho Widget 2 - Mở tab Activity
    private final MutableLiveData<Boolean> shouldOpenActivityTab = new MutableLiveData<>(false);

    // MỚI: Cho Widget 2 - Mở chi tiết posts của activity cụ thể
    private final MutableLiveData<String> selectedActivityIdForDetail = new MutableLiveData<>(null);

    // Giữ nguyên: Mở chi tiết post từ Widget 1
    private final MutableLiveData<String> openPostId = new MutableLiveData<>();

    public MainViewModel() {
        loadPosts();
        loadMoods();
        loadJoinedActivities();
        loadFriendListForMenu();
    }

    // ====================== GETTERS ======================
    public LiveData<Resource<List<Post>>> getPosts() { return posts; }
    public LiveData<Resource<List<Mood>>> getMoods() { return moods; }
    public LiveData<Resource<List<Activity>>> getJoinedActivities() { return joinedActivities; }
    public LiveData<Resource<Boolean>> getUploadStatus() { return uploadStatus; }

    // MỚI: Widget 2
    public LiveData<Boolean> shouldOpenActivityTab() { return shouldOpenActivityTab; }
    public void setShouldOpenActivityTab(boolean open) { shouldOpenActivityTab.setValue(open); }

    public LiveData<String> getSelectedActivityIdForDetail() { return selectedActivityIdForDetail; }
    public void setSelectedActivityIdForDetail(String activityId) { selectedActivityIdForDetail.setValue(activityId); }
    public void clearSelectedActivityIdForDetail() { selectedActivityIdForDetail.setValue(null); }

    // Giữ nguyên: Widget 1
    public LiveData<String> getOpenPostId() { return openPostId; }
    public void setOpenPostId(String postId) { openPostId.setValue(postId); }
    public void clearOpenPostId() { openPostId.setValue(null); }
    private final MutableLiveData<List<User>> friendListForMenu = new MutableLiveData<>(); // [MỚI] Danh sách bạn bè để hiện Menu

    // State lọc hiện tại
    private PostFilterType currentFilterType = PostFilterType.ALL;
    private String currentFilterTargetId = null;

    // ====================== LOAD DATA ======================
    private void loadMoods() {
        db.collection("Mood")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Mood> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        list.add(new Mood(
                                doc.getString("name"),
                                doc.getString("iconUrl"),
                                Boolean.TRUE.equals(doc.getBoolean("isPremium"))
                        ));
                    }
                    moods.postValue(Resource.success(list));
                })
                .addOnFailureListener(e -> moods.postValue(Resource.error(e.getMessage(), null)));
    }

    public void loadJoinedActivities() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("activities")
                .whereArrayContains("participants", uid)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        joinedActivities.postValue(Resource.error(error.getMessage(), null));
                        return;
                    }
                    if (snapshots != null) {
                        List<Activity> activities = snapshots.toObjects(Activity.class);
                        joinedActivities.postValue(Resource.success(activities));
                    }
                });
    }

    // ====================== TẠO ACTIVITY ======================
    public void createActivity(String title,
                               boolean isDaily,
                               int target,
                               long durationSeconds,
                               Timestamp scheduledTime,
                               Uri imageUri) {

        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        String activityId = db.collection("activities").document().getId();

        if (imageUri != null) {
            String fileName = "activity_covers/" + UUID.randomUUID() + ".jpg";
            StorageReference ref = storage.getReference().child(fileName);

            ref.putFile(imageUri)
                    .addOnSuccessListener(task -> ref.getDownloadUrl()
                            .addOnSuccessListener(uri -> saveActivity(activityId, uid, title, isDaily, target, durationSeconds, scheduledTime, uri.toString()))
                            .addOnFailureListener(e -> saveActivity(activityId, uid, title, isDaily, target, durationSeconds, scheduledTime, null)))
                    .addOnFailureListener(e -> saveActivity(activityId, uid, title, isDaily, target, durationSeconds, scheduledTime, null));
        } else {
            saveActivity(activityId, uid, title, isDaily, target, durationSeconds, scheduledTime, null);
        }
    }

    private void saveActivity(String id, String uid, String title, boolean isDaily, int target,
                              long durationSeconds, Timestamp scheduledTime, String imageUrl) {

        Activity activity = new Activity(uid, title, isDaily, target, durationSeconds, scheduledTime);
        activity.setId(id);
        if (imageUrl != null) activity.setImageUrl(imageUrl);

        List<String> participants = new ArrayList<>();
        participants.add(uid);
        activity.setParticipants(participants);
        activity.setProgress(0);

        db.collection("activities").document(id).set(activity);
    }

    // ====================== CHECK-IN ACTIVITY ======================
    public void checkInActivity(Activity activity, String localImagePath, String note) {
        if (auth.getCurrentUser() == null || activity == null) return;

        String uid = auth.getCurrentUser().getUid();
        uploadStatus.postValue(Resource.loading(null));

        if (localImagePath != null) {
            Uri fileUri = Uri.parse("file://" + localImagePath);
            String fileName = "logs/" + activity.getId() + "_" + System.currentTimeMillis() + ".jpg";
            StorageReference ref = storage.getReference().child(fileName);

            ref.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                            .addOnSuccessListener(uri -> saveCheckInLogAndCreatePost(activity, uid, uri.toString(), note))
                            .addOnFailureListener(e -> uploadStatus.postValue(Resource.error("Lỗi lấy link ảnh", false))))
                    .addOnFailureListener(e -> uploadStatus.postValue(Resource.error("Lỗi upload ảnh", false)));
        } else {
            saveCheckInLogAndCreatePost(activity, uid, null, note);
        }
    }

    private void saveCheckInLogAndCreatePost(Activity activity, String uid, String imageUrl, String note) {
        ActivityLog log = new ActivityLog(activity.getId(), uid, imageUrl, note, Timestamp.now());

        db.collection("activities")
                .document(activity.getId())
                .collection("logs")
                .add(log)
                .addOnSuccessListener(documentReference -> {
                    // Tăng progress
                    db.collection("activities")
                            .document(activity.getId())
                            .update("progress", FieldValue.increment(1));

                    // Tạo Post để hiện trong Story và Feed
                    createCheckInPost(activity, uid, imageUrl, note);

                    uploadStatus.postValue(Resource.success(true));
                })
                .addOnFailureListener(e -> uploadStatus.postValue(Resource.error("Lỗi lưu log", false)));
    }

    private void createCheckInPost(Activity activity, String uid, String imageUrl, String note) {
        Post post = new Post();
        post.setUserId(uid);
        post.setType("activity");
        post.setActivityId(activity.getId());
        post.setActivityTitle(activity.getTitle());
        post.setPhotoUrl(imageUrl);
        post.setCaption(note != null ? note : "");
        post.setCreatedAt(Timestamp.now());

        postRepository.createPost(post, null, new MutableLiveData<>());
    }

    // ====================== ĐĂNG MOOD ======================
    public void createPost(String caption, Mood mood) {
        if (auth.getCurrentUser() == null) return;

        Post post = new Post();
        post.setUserId(auth.getCurrentUser().getUid());
        post.setCaption(caption);
        post.setCreatedAt(Timestamp.now());

        if (mood != null) {
            post.setType("mood");
            post.setMoodName(mood.getName());
            post.setMoodIconUrl(mood.getIconUrl());
        }

        postRepository.createPost(post, null, uploadStatus);
    }

    public void joinActivity(String activityId) {
        if (auth.getCurrentUser() == null || activityId == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("activities").document(activityId)
                .update("participants", FieldValue.arrayUnion(uid))
                .addOnSuccessListener(aVoid -> loadJoinedActivities())
                .addOnFailureListener(e -> { /* Xử lý lỗi nếu cần */ });
    }

    // ====================== REFRESH ======================

    public void refreshActivities() {
        loadJoinedActivities();
    }
    // --- GETTERS ---
    public LiveData<List<User>> getFriendListForMenu() { return friendListForMenu; }
    public String getCurrentFilterName() {
        // Helper để UI hiển thị tên
        if (currentFilterType == PostFilterType.ALL) return "Mọi người";
        if (currentFilterType == PostFilterType.SELF) return "Bản thân";
        // Tên người dùng cụ thể sẽ được set trực tiếp ở Fragment khi click
        return "Người dùng";
    }

    // --- LOGIC LOAD BẠN BÈ CHO MENU ---
    private void loadFriendListForMenu() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        // Lấy danh sách quan hệ -> Lấy User info
        db.collection("relationships")
                .whereArrayContains("members", uid)
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<String> friendIds = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        List<String> members = (List<String>) doc.get("members");
                        if (members != null) {
                            for (String memberId : members) {
                                if (!memberId.equals(uid)) friendIds.add(memberId);
                            }
                        }
                    }
                    if (!friendIds.isEmpty()) fetchUsersDetails(friendIds);
                });
    }

    private void fetchUsersDetails(List<String> userIds) {
        // Firestore whereIn giới hạn 10, nên nếu list dài cần chia nhỏ hoặc load từng cái.
        // Ở đây dùng cách load song song để đơn giản
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String id : userIds) {
            tasks.add(db.collection("users").document(id).get());
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(objects -> {
            List<User> users = new ArrayList<>();
            for (Object obj : objects) {
                DocumentSnapshot doc = (DocumentSnapshot) obj;
                if (doc.exists()) {
                    users.add(doc.toObject(User.class));
                }
            }
            friendListForMenu.postValue(users);
        });
    }

    // --- LOGIC LỌC ---
    public void setFilter(PostFilterType type, String targetId) {
        this.currentFilterType = type;
        this.currentFilterTargetId = targetId;
        loadPosts();
    }

    private void loadPosts() {
        postRepository.getPosts(posts, currentFilterType, currentFilterTargetId);
    }

    // Refresh cũng cần tôn trọng filter hiện tại
    public void refreshPosts() {
        loadPosts();
    }
}