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
    // 1. LẤY DANH SÁCH BÀI VIẾT (FEED) - GIỮ NGUYÊN LOGIC TỐI ƯU
    // =================================================================================

    public void getPosts(MutableLiveData<Resource<List<Post>>> result) {
        if (auth.getCurrentUser() == null) {
            result.postValue(Resource.error("Chưa đăng nhập", null));
            return;
        }
        String currentUserId = auth.getCurrentUser().getUid();

        result.postValue(Resource.loading(null));

        // BƯỚC 1: Lấy danh sách ID bạn bè
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

                    // BƯỚC 2: Lắng nghe Realtime
                    db.collection("posts")
                            .orderBy("createdAt", Query.Direction.DESCENDING)
                            .addSnapshotListener((postSnapshots, e) -> {
                                if (e != null) {
                                    Log.e("PostRepo", "Listen failed.", e);
                                    result.postValue(Resource.error(e.getMessage(), null));
                                    return;
                                }

                                if (postSnapshots != null) {
                                    List<Post> tempPosts = new ArrayList<>();

                                    for (DocumentSnapshot doc : postSnapshots) {
                                        Post post = doc.toObject(Post.class);
                                        if (post != null) {
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

                                    // BƯỚC 3: Tải thông tin User SONG SONG
                                    fetchUsersForPostsParallel(tempPosts, result);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    result.postValue(Resource.error("Lỗi lấy danh sách bạn bè: " + e.getMessage(), null));
                });
    }

    private void fetchUsersForPostsParallel(List<Post> posts, MutableLiveData<Resource<List<Post>>> result) {
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

        for (Post post : posts) {
            tasks.add(db.collection("users").document(post.getUserId()).get());
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(objects -> {
            for (int i = 0; i < objects.size(); i++) {
                DocumentSnapshot userDoc = (DocumentSnapshot) objects.get(i);
                Post post = posts.get(i);

                if (userDoc.exists()) {
                    post.setUserName(userDoc.getString("username"));
                    post.setUserAvatar(userDoc.getString("profilePhotoUrl"));
                } else {
                    post.setUserName("Người dùng ẩn danh");
                }
            }
            result.postValue(Resource.success(posts));
        });
    }

    // =================================================================================
    // 2. ĐĂNG BÀI VIẾT MỚI (CREATE) - ĐÃ SỬA LẠI SIGNATURE CHO KHỚP VIEWMODEL
    // =================================================================================

    /**
     * Hàm này nhận trực tiếp object Post đã được build từ ViewModel.
     * Nhiệm vụ của nó chỉ là:
     * 1. Upload ảnh (nếu imageUri != null) -> Lấy URL -> Gán vào Post.
     * 2. Lưu Post vào Firestore.
     */
    public void createPost(Post post, Uri imageUri, MutableLiveData<Resource<Boolean>> result) {
        if (auth.getCurrentUser() == null) return;

        result.postValue(Resource.loading(null));

        if (imageUri != null) {
            // --- TRƯỜNG HỢP CÓ ẢNH (Activity Post) ---
            String fileName = "posts/" + post.getUserId() + "/" + System.currentTimeMillis() + ".jpg";
            StorageReference ref = storage.getReference().child(fileName);

            ref.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Upload xong, lấy link ảnh gán ngược vào Post object
                                post.setPhotoUrl(uri.toString());
                                // Sau đó lưu toàn bộ object xuống Firestore
                                savePostToFirestore(post, result);
                            }))
                    .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi upload ảnh: " + e.getMessage(), false)));
        } else {
            // --- TRƯỜNG HỢP KHÔNG CÓ ẢNH (Mood Post) ---
            savePostToFirestore(post, result);
        }
    }

    // Hàm phụ lưu vào Firestore
    private void savePostToFirestore(Post post, MutableLiveData<Resource<Boolean>> result) {
        db.collection("posts").add(post)
                .addOnSuccessListener(documentReference -> {
                    // Thành công!
                    result.postValue(Resource.success(true));
                })
                .addOnFailureListener(e -> {
                    result.postValue(Resource.error("Lỗi lưu bài viết: " + e.getMessage(), false));
                });
    }
}