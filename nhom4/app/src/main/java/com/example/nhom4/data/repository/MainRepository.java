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

/**
 * MainRepository
 * --------------------------------------------------
 * Class này quản lý logic cho màn hình chính (Home/Newsfeed):
 * 1. Load danh sách Mood (Biểu cảm) để user chọn khi đăng bài.
 * 2. Load Newfeed (Bài viết của bạn bè và bản thân).
 * 3. Xử lý Đăng bài viết mới (bao gồm upload ảnh lên Storage).
 */
public class MainRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    // 1. Lấy danh sách Mood (Cảm xúc) từ Firestore
    // Dữ liệu này ít thay đổi nên chỉ dùng .get() một lần
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

    /**
     * 2. Lấy bài viết (Newfeed)
     * Logic phức tạp: Kết hợp dữ liệu từ 3 bảng (Relationships -> Posts -> Users).
     *
     * Quy trình xử lý:
     * B1: Tìm danh sách những người là bạn bè (để biết mình được xem bài của ai).
     * B2: Lắng nghe Realtime toàn bộ bài viết mới nhất.
     * B3: Lọc thủ công (Client-side) chỉ giữ lại bài của bạn bè.
     * B4: Lấy thông tin người đăng (Tên, Avatar) cho từng bài viết.
     */
    public void getPosts(MutableLiveData<Resource<List<Post>>> result) {
        if (auth.getCurrentUser() == null) return;
        String currentUserId = auth.getCurrentUser().getUid();
        result.postValue(Resource.loading(null));

        // --- BƯỚC 1: Lấy danh sách ID bạn bè (status = 'accepted') ---
        db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<String> friendIds = new ArrayList<>();
                    friendIds.add(currentUserId); // Quan trọng: Thêm chính mình để thấy bài của mình

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

                    // --- BƯỚC 2: Lắng nghe bài viết Realtime ---
                    // Lưu ý: Lấy về tất cả bài viết rồi mới lọc (Do Firestore hạn chế query IN + Sort)
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

                                    // Lọc bài viết của bạn bè (Client-side filtering)
                                    for (DocumentSnapshot doc : postSnapshots) {
                                        Post post = doc.toObject(Post.class);
                                        // Chỉ lấy bài nếu tác giả nằm trong friendIds
                                        if (post != null && friendIds.contains(post.getUserId())) {
                                            post.setPostId(doc.getId());
                                            tempFilteredList.add(post);
                                        }
                                    }

                                    if (tempFilteredList.isEmpty()) {
                                        result.postValue(Resource.success(new ArrayList<>()));
                                        return;
                                    }

                                    // --- BƯỚC 3: Điền thông tin User vào bài viết ---
                                    // Gọi hàm đệ quy để xử lý bất đồng bộ tuần tự
                                    loadUsersInfoRecursive(tempFilteredList, 0, postList, result);
                                }
                            });
                })
                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi lấy bạn bè: " + e.getMessage(), null)));
    }

    /**
     * Hàm đệ quy để load thông tin user (Tên, Avatar)
     * Tác dụng: Đảm bảo tải xong thông tin người này mới tải tiếp người kia.
     * Tránh việc hiển thị bài viết khi chưa có tên người đăng.
     */
    private void loadUsersInfoRecursive(List<Post> sourceList, int index, List<Post> resultList, MutableLiveData<Resource<List<Post>>> liveData) {
        // Điều kiện dừng: Khi đã duyệt hết danh sách bài viết cần hiển thị
        if (index >= sourceList.size()) {
            // Đã load xong hết -> Post kết quả cuối cùng lên UI
            liveData.postValue(Resource.success(resultList));
            return;
        }

        Post currentPost = sourceList.get(index);
        // Query lấy thông tin user tác giả
        db.collection("users").document(currentPost.getUserId()).get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        currentPost.setUserName(userDoc.getString("username"));
                        currentPost.setUserAvatar(userDoc.getString("profilePhotoUrl"));
                    } else {
                        currentPost.setUserName("Người dùng ẩn danh");
                    }
                    resultList.add(currentPost);

                    // Đệ quy: Gọi lại chính nó để load bài tiếp theo (index + 1)
                    loadUsersInfoRecursive(sourceList, index + 1, resultList, liveData);
                })
                .addOnFailureListener(e -> {
                    // Nếu lỗi mạng, vẫn thêm bài vào list nhưng để tên mặc định
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
            // Trường hợp 1: Có ảnh -> Upload ảnh lên Storage trước
            Uri fileUri = Uri.fromFile(new File(imagePath));
            // Đặt tên file theo timestamp để không bị trùng
            StorageReference ref = storage.getReference().child("posts/" + uid + "/" + System.currentTimeMillis() + ".jpg");

            ref.putFile(fileUri)
                    .addOnSuccessListener(task ->
                            // Upload xong -> Lấy link download (URL)
                            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Có link ảnh -> Gọi hàm lưu vào Firestore
                                savePostToFirestore(uid, caption, uri.toString(), mood, activityTitle, status);
                            }))
                    .addOnFailureListener(e -> status.postValue(Resource.error("Lỗi upload ảnh: " + e.getMessage(), false)));
        } else {
            // Trường hợp 2: Không ảnh (Chỉ có text/mood) -> Lưu thẳng vào Firestore
            savePostToFirestore(uid, caption, null, mood, activityTitle, status);
        }
    }

    // Hàm phụ trợ: Lưu dữ liệu bài viết vào Firestore
    private void savePostToFirestore(String uid, String caption, String photoUrl, Mood mood, String activityTitle, MutableLiveData<Resource<Boolean>> status) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", uid);
        map.put("caption", caption);
        map.put("photoUrl", photoUrl != null ? photoUrl : "");
        map.put("createdAt", FieldValue.serverTimestamp()); // Lấy giờ server

        // Kiểm tra loại bài viết (Mood hay Activity) để lưu các trường tương ứng
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