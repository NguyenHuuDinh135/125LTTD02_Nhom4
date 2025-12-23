package com.example.nhom4.ui.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Activity;
import com.example.nhom4.data.bean.ActivityLog;
import com.example.nhom4.data.bean.Mood;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.data.repository.AuthRepository;
import com.example.nhom4.data.repository.PostRepository;
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

    public MainViewModel() {
        loadPosts();
        loadMoods();
        loadJoinedActivities();
    }

    // ====================== GETTERS ======================
    public LiveData<Resource<List<Post>>> getPosts() { return posts; }
    public LiveData<Resource<List<Mood>>> getMoods() { return moods; }
    public LiveData<Resource<List<Activity>>> getJoinedActivities() { return joinedActivities; }
    public LiveData<Resource<Boolean>> getUploadStatus() { return uploadStatus; }

    // ====================== LOAD DATA ======================
    private void loadPosts() {
        postRepository.getPosts(posts);
    }

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
        post.setType("activity"); // Phân biệt với mood
        post.setActivityId(activity.getId());
        post.setActivityTitle(activity.getTitle()); // Dùng để hiển thị tên activity trong Story
        post.setPhotoUrl(imageUrl);
        post.setCaption(note != null ? note : "");
        post.setCreatedAt(Timestamp.now());

        // Tạo post trong collection posts → sẽ realtime hiện trong Story/Feed
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

        // Cập nhật Firestore: Thêm uid vào mảng participants
        db.collection("activities").document(activityId)
                .update("participants", FieldValue.arrayUnion(uid))
                .addOnSuccessListener(aVoid -> {
                    // Refresh lại danh sách activity để UI cập nhật ngay lập tức
                    loadJoinedActivities();
                })
                .addOnFailureListener(e -> {
                    // Xử lý lỗi nếu cần
                });
    }
    // ====================== REFRESH ======================
    public void refreshPosts() {
        loadPosts();
    }

    public void refreshActivities() {
        loadJoinedActivities();
    }
    // ====================== OPEN POST FROM WIDGET ======================
    private final MutableLiveData<String> openPostId = new MutableLiveData<>();

    public LiveData<String> getOpenPostId() {
        return openPostId;
    }

    public void setOpenPostId(String postId) {
        openPostId.setValue(postId);
    }

    public void clearOpenPostId() {
        openPostId.setValue(null);
    }

}