package com.example.nhom4.data.repository;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Post;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * PostRepository
 * ----------------------------------------------------
 * Quản lý dữ liệu bài viết: Lấy tin (Feed) và Đăng bài (Create).
 * Đã tối ưu hóa tốc độ tải bằng cách xử lý song song (Parallel execution).
 */
public class PostRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // =================================================================================
    // 1. LẤY DANH SÁCH BÀI VIẾT (FEED)
    // =================================================================================

    public void getPosts(MutableLiveData<Resource<List<Post>>> result) {
        if (auth.getCurrentUser() == null) {
            result.postValue(Resource.error("Chưa đăng nhập", null));
            return;
        }
        String currentUserId = auth.getCurrentUser().getUid();

        result.postValue(Resource.loading(null));

        // BƯỚC 1: Lấy danh sách ID bạn bè (để lọc bài viết)
        db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<String> friendIds = new ArrayList<>();
                    friendIds.add(currentUserId); // [QUAN TRỌNG] Thêm chính mình để thấy bài mình vừa đăng

                    // Duyệt qua các mối quan hệ để lấy ID đối phương
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

                    // BƯỚC 2: Lắng nghe Realtime bảng 'posts'
                    // Lưu ý: Dùng addSnapshotListener để tự động cập nhật khi có bài mới
                    db.collection("posts")
                            .orderBy("createdAt", Query.Direction.DESCENDING) // Bài mới nhất lên đầu
                            .addSnapshotListener((postSnapshots, e) -> {
                                if (e != null) {
                                    Log.e("PostRepo", "Listen failed.", e);
                                    result.postValue(Resource.error(e.getMessage(), null));
                                    return;
                                }

                                if (postSnapshots != null) {
                                    List<Post> tempPosts = new ArrayList<>();

                                    // Lọc client-side: Chỉ lấy bài của người trong list friendIds
                                    for (DocumentSnapshot doc : postSnapshots) {
                                        Post post = doc.toObject(Post.class);
                                        if (post != null) {
                                            // Kiểm tra quyền xem
                                            if (friendIds.contains(post.getUserId())) {
                                                post.setPostId(doc.getId());
                                                tempPosts.add(post);
                                            }
                                        }
                                    }

                                    if (tempPosts.isEmpty()) {
                                        result.postValue(Resource.success(new ArrayList<>()));
                                        return;
                                    }

                                    // BƯỚC 3: Tải thông tin User (Avatar, Tên) SONG SONG cho siêu tốc
                                    fetchUsersForPostsParallel(tempPosts, result);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    result.postValue(Resource.error("Lỗi lấy danh sách bạn bè: " + e.getMessage(), null));
                });
    }

    /**
     * Kỹ thuật Tải Song Song (Parallel Fetching):
     * Gom tất cả request lấy thông tin User vào 1 danh sách và chạy cùng lúc.
     * Nhanh hơn rất nhiều so với chạy vòng lặp tuần tự.
     */
    private void fetchUsersForPostsParallel(List<Post> posts, MutableLiveData<Resource<List<Post>>> result) {
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

        // 1. Tạo danh sách các nhiệm vụ (Task) cần làm
        for (Post post : posts) {
            tasks.add(db.collection("users").document(post.getUserId()).get());
        }

        // 2. Chạy tất cả Task cùng lúc và đợi khi XONG HẾT (whenAllSuccess)
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(objects -> {
            // objects là danh sách kết quả (DocumentSnapshot) trả về theo đúng thứ tự
            for (int i = 0; i < objects.size(); i++) {
                DocumentSnapshot userDoc = (DocumentSnapshot) objects.get(i);
                Post post = posts.get(i); // Bài viết tương ứng

                if (userDoc.exists()) {
                    post.setUserName(userDoc.getString("username"));
                    post.setUserAvatar(userDoc.getString("profilePhotoUrl"));
                } else {
                    post.setUserName("Người dùng ẩn danh");
                }
            }

            // 3. Trả về UI danh sách đã đầy đủ thông tin
            result.postValue(Resource.success(posts));
        });
    }

    // =================================================================================
    // 2. ĐĂNG BÀI VIẾT MỚI (CREATE)
    // =================================================================================

    public void createPost(String caption, String photoPath, String moodName, String activityTitle, MutableLiveData<Resource<Boolean>> result) {
        if (auth.getCurrentUser() == null) return;

        result.postValue(Resource.loading(null));

        Post post = new Post();
        post.setUserId(auth.getCurrentUser().getUid());
        post.setCaption(caption);
        post.setCreatedAt(new com.google.firebase.Timestamp(new java.util.Date()));

        // Phân loại Mood hay Activity
        if (moodName != null) {
            post.setType("mood");
            post.setMoodName(moodName);
            // Có thể set thêm iconUrl cho mood nếu cần
        } else if (activityTitle != null) {
            post.setType("activity");
            post.setActivityTitle(activityTitle);
        } else {
            post.setType("normal");
        }

        // Xử lý ảnh (nếu có)
        if (photoPath != null) {
            Uri fileUri = Uri.fromFile(new java.io.File(photoPath));
            String fileName = "posts/" + post.getUserId() + "/" + System.currentTimeMillis() + ".jpg";
            StorageReference ref = storage.getReference().child(fileName);

            ref.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot ->
                            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                post.setPhotoUrl(uri.toString());
                                savePostToFirestore(post, result);
                            }))
                    .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi upload ảnh: " + e.getMessage(), false)));
        } else {
            // Đăng bài không ảnh (ví dụ Mood)
            savePostToFirestore(post, result);
        }
    }

    // Hàm phụ lưu vào Firestore
    private void savePostToFirestore(Post post, MutableLiveData<Resource<Boolean>> result) {
        db.collection("posts").add(post)
                .addOnSuccessListener(documentReference -> {
                    // Thành công!
                    // Lưu ý: Nhờ addSnapshotListener ở hàm getPosts,
                    // UI bên Feed sẽ tự động nhận được bài mới này ngay lập tức.
                    result.postValue(Resource.success(true));
                })
                .addOnFailureListener(e -> {
                    result.postValue(Resource.error("Lỗi lưu bài viết: " + e.getMessage(), false));
                });
    }
}