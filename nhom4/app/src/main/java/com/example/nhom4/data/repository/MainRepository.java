package com.example.nhom4.data.repository;

import android.net.Uri;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Activity;
import com.example.nhom4.data.bean.Mood;
import com.example.nhom4.data.bean.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.MutableLiveData;

public class MainRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    // 1. Lấy danh sách Mood
    public void getMoods(MutableLiveData<Resource<List<Mood>>> result) {
        db.collection("Mood").get()
                .addOnSuccessListener(snapshots -> {
                    List<Mood> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        list.add(new Mood(doc.getString("name"), doc.getString("iconUrl"), Boolean.TRUE.equals(doc.getBoolean("isPremium"))));
                    }
                    result.postValue(Resource.success(list));
                })
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
    }

    // 2. Lấy bài viết (Filter Bạn bè & Tải Username)
    public void getPosts(MutableLiveData<Resource<List<Post>>> result) {
        if (auth.getCurrentUser() == null) return;
        String currentUserId = auth.getCurrentUser().getUid();
        result.postValue(Resource.loading(null));

        // B1: Lấy danh sách bạn bè (status = accepted)
        db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<String> friendIds = new ArrayList<>();
                    friendIds.add(currentUserId); // Thêm chính mình

                    for (DocumentSnapshot doc : snapshots) {
                        List<String> members = (List<String>) doc.get("members");
                        if (members != null) {
                            for (String memberId : members) {
                                if (!memberId.equals(currentUserId)) {
                                    friendIds.add(memberId);
                                }
                            }
                        }
                    }

                    // B2: Lắng nghe bài viết Realtime
                    db.collection("posts")
                            .orderBy("createdAt", Query.Direction.DESCENDING)
                            .addSnapshotListener((postSnapshots, e) -> {
                                if (e != null) {
                                    result.postValue(Resource.error(e.getMessage(), null));
                                    return;
                                }

                                if (postSnapshots != null) {
                                    List<Post> postList = new ArrayList<>();
                                    List<Post> tempFilteredList = new ArrayList<>();

                                    // Lọc bài viết của bạn bè
                                    for (DocumentSnapshot doc : postSnapshots) {
                                        Post post = doc.toObject(Post.class);
                                        if (post != null && friendIds.contains(post.getUserId())) {
                                            post.setPostId(doc.getId());
                                            tempFilteredList.add(post);
                                        }
                                    }

                                    if (tempFilteredList.isEmpty()) {
                                        result.postValue(Resource.success(new ArrayList<>()));
                                        return;
                                    }

                                    // B3: Lấy thông tin user (Tên, Avatar) cho từng bài viết
                                    // (Logic này chạy async, để đơn giản ta sẽ cập nhật dần dần hoặc dùng count)
                                    loadUsersInfoRecursive(tempFilteredList, 0, postList, result);
                                }
                            });
                })
                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi lấy bạn bè: " + e.getMessage(), null)));
    }

    // Hàm đệ quy để load thông tin user tuần tự (đảm bảo xong hết mới hiển thị để tránh nhảy layout)
    private void loadUsersInfoRecursive(List<Post> sourceList, int index, List<Post> resultList, MutableLiveData<Resource<List<Post>>> liveData) {
        if (index >= sourceList.size()) {
            // Đã load xong hết -> Post lên UI
            liveData.postValue(Resource.success(resultList));
            return;
        }

        Post currentPost = sourceList.get(index);
        db.collection("users").document(currentPost.getUserId()).get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        currentPost.setUserName(userDoc.getString("username"));
                        currentPost.setUserAvatar(userDoc.getString("profilePhotoUrl"));
                    } else {
                        currentPost.setUserName("Người dùng ẩn danh");
                    }
                    resultList.add(currentPost);

                    // Load tiếp bài sau
                    loadUsersInfoRecursive(sourceList, index + 1, resultList, liveData);
                })
                .addOnFailureListener(e -> {
                    // Lỗi thì vẫn add vào nhưng để tên mặc định
                    currentPost.setUserName("Người dùng");
                    resultList.add(currentPost);
                    loadUsersInfoRecursive(sourceList, index + 1, resultList, liveData);
                });
    }

    // 3. Đăng bài viết (Upload ảnh -> Save Firestore)
    public void createPost(String caption, String imagePath, Mood mood, String activityTitle, MutableLiveData<Resource<Boolean>> status) {
        status.postValue(Resource.loading(null));
        if (auth.getCurrentUser() == null) {
            status.postValue(Resource.error("Chưa đăng nhập", false));
            return;
        }
        String uid = auth.getCurrentUser().getUid();

        if (imagePath != null) {
            // Có ảnh -> Upload trước
            Uri fileUri = Uri.fromFile(new File(imagePath));
            StorageReference ref = storage.getReference().child("posts/" + uid + "/" + System.currentTimeMillis() + ".jpg");
            ref.putFile(fileUri)
                    .addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        savePostToFirestore(uid, caption, uri.toString(), mood, activityTitle, status);
                    }))
                    .addOnFailureListener(e -> status.postValue(Resource.error("Lỗi upload ảnh: " + e.getMessage(), false)));
        } else {
            // Không ảnh (Chỉ Mood)
            savePostToFirestore(uid, caption, null, mood, activityTitle, status);
        }
    }

    private void savePostToFirestore(String uid, String caption, String photoUrl, Mood mood, String activityTitle, MutableLiveData<Resource<Boolean>> status) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", uid);
        map.put("caption", caption);
        map.put("photoUrl", photoUrl != null ? photoUrl : "");
        map.put("createdAt", FieldValue.serverTimestamp());

        if (mood != null) {
            map.put("type", "mood");
            map.put("moodName", mood.getName());
            map.put("moodIconUrl", mood.getIconUrl());
        } else if (activityTitle != null) {
            map.put("type", "activity");
            map.put("activityTitle", activityTitle);
        }

        db.collection("posts").add(map)
                .addOnSuccessListener(doc -> status.postValue(Resource.success(true)))
                .addOnFailureListener(e -> status.postValue(Resource.error(e.getMessage(), false)));
    }
}
