package com.example.nhom4.data.repository;

import android.net.Uri;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;

/**
 * PostRepository
 * ----------------------------------------------------
 * Class này chịu trách nhiệm quản lý dữ liệu Bài viết (Newsfeed):
 * 1. Lấy danh sách bài viết từ Firestore (lọc theo bạn bè).
 * 2. Upload ảnh và tạo bài viết mới.
 */
public class PostRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    /**
     * Hàm lấy bài viết (Get Posts)
     * ----------------------------
     * Logic phức tạp: Cần kết hợp dữ liệu từ 3 bảng (Relationships -> Posts -> Users).
     *
     * Quy trình 3 bước:
     * B1: Lấy danh sách ID bạn bè.
     * B2: Lắng nghe toàn bộ bài viết (Realtime) và lọc thủ công (Client-side filter).
     * B3: Điền thông tin người đăng (Tên, Avatar) vào từng bài viết.
     */
    // [SỬA] Hàm lấy bài viết: Chỉ lấy bài của bạn bè + chính mình
    public void getPosts(MutableLiveData<Resource<List<Post>>> result) {
        if (auth.getCurrentUser() == null) return;
        String currentUserId = auth.getCurrentUser().getUid();

        result.postValue(Resource.loading(null)); // Báo UI hiện loading

        // --- BƯỚC 1: Xác định "Mình được xem bài của ai?" ---
        // Lấy danh sách ID bạn bè (những người có status = "accepted")
        db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<String> friendIds = new ArrayList<>();
                    friendIds.add(currentUserId); // QUAN TRỌNG: Thêm chính mình vào để thấy bài của mình

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

                    // --- BƯỚC 2: Query bài viết (Realtime) ---
                    // Lưu ý: Firestore khó query "vừa sort thời gian, vừa lọc theo list bạn bè".
                    // Nên ta lấy bài viết về rồi tự lọc bằng code Java (Client-side).
                    db.collection("posts")
                            .orderBy("createdAt", Query.Direction.DESCENDING) // Bài mới nhất lên đầu
                            .addSnapshotListener((postSnapshots, e) -> {
                                if (e != null) {
                                    result.postValue(Resource.error(e.getMessage(), null));
                                    return;
                                }

                                if (postSnapshots != null) {
                                    List<Post> filteredList = new ArrayList<>();

                                    // Vòng lặp lọc bài viết (Client-side filtering)
                                    for (DocumentSnapshot doc : postSnapshots) {
                                        Post post = doc.toObject(Post.class);
                                        if (post != null) {
                                            // [QUAN TRỌNG] Chỉ thêm nếu userId nằm trong list bạn bè
                                            if (friendIds.contains(post.getUserId())) {
                                                post.setPostId(doc.getId());
                                                filteredList.add(post);
                                            }
                                        }
                                    }

                                    // Nếu lọc xong mà không có bài nào
                                    if (filteredList.isEmpty()) {
                                        result.postValue(Resource.success(new ArrayList<>()));
                                        return;
                                    }

                                    // --- BƯỚC 3: "Join" bảng User để lấy tên và avatar ---
                                    // Gọi hàm đệ quy để xử lý bất đồng bộ, tránh lỗi hiển thị thiếu tên
                                    loadUserInfoRecursive(filteredList, 0, new ArrayList<>(), result);
                                }
                            });
                })
                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi lấy danh sách bạn: " + e.getMessage(), null)));
    }

    /**
     * Helper: Load thông tin user tuần tự (Recursive)
     * ------------------------------------------------
     * TẠI SAO DÙNG ĐỆ QUY?
     * Vì việc gọi db...get() là bất đồng bộ (Async).
     * Nếu dùng vòng lặp 'for', code sẽ chạy hết vòng lặp TRƯỚC KHI dữ liệu user kịp tải về.
     * Cách này đảm bảo: Tải xong User cho bài 1 -> Mới tải tiếp cho bài 2 -> ... -> Xong hết mới trả về UI.
     */
    private void loadUserInfoRecursive(List<Post> sourceList, int index, List<Post> finalResult, MutableLiveData<Resource<List<Post>>> liveData) {
        // Điều kiện dừng: Khi đã duyệt hết danh sách
        if (index >= sourceList.size()) {
            liveData.postValue(Resource.success(finalResult)); // Trả về danh sách hoàn chỉnh
            return;
        }

        Post currentPost = sourceList.get(index);

        // Truy vấn thông tin người đăng bài
        db.collection("users").document(currentPost.getUserId()).get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        // Map dữ liệu User vào bài viết
                        currentPost.setUserName(userDoc.getString("username"));
                        currentPost.setUserAvatar(userDoc.getString("profilePhotoUrl"));
                    } else {
                        // Fallback nếu không tìm thấy user
                        currentPost.setUserName("Người dùng");
                    }
                    finalResult.add(currentPost);

                    // GỌI LẠI CHÍNH NÓ: Tăng index lên 1 để xử lý bài tiếp theo
                    loadUserInfoRecursive(sourceList, index + 1, finalResult, liveData);
                })
                .addOnFailureListener(e -> {
                    // Nếu lỗi mạng, vẫn thêm bài viết vào list nhưng để tên mặc định
                    currentPost.setUserName("Người dùng");
                    finalResult.add(currentPost);
                    loadUserInfoRecursive(sourceList, index + 1, finalResult, liveData);
                });
    }

    /**
     * Tạo bài viết mới
     * Chia làm 2 trường hợp:
     * 1. Có ảnh: Upload ảnh lên Storage -> Lấy URL -> Lưu vào Firestore.
     * 2. Không ảnh: Lưu thẳng vào Firestore.
     */
    public void createPost(Post post, Uri imageUri, MutableLiveData<Resource<Boolean>> result) {
        result.postValue(Resource.loading(null));

        if (imageUri != null) {
            // --- CASE 1: CÓ ẢNH ---
            // Đặt tên file theo timestamp để tránh trùng lặp
            String fileName = "posts/" + post.getUserId() + "/" + System.currentTimeMillis() + ".jpg";
            StorageReference ref = storage.getReference().child(fileName);

            ref.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            // Upload xong, cần lấy download URL
                            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                post.setPhotoUrl(uri.toString()); // Gán link ảnh vào object Post
                                savePostToFirestore(post, result); // Lưu vào DB
                            }))
                    .addOnFailureListener(e -> result.postValue(Resource.error("Upload failed: " + e.getMessage(), false)));
        } else {
            // --- CASE 2: KHÔNG CÓ ẢNH ---
            savePostToFirestore(post, result);
        }
    }

    // Hàm phụ trợ lưu data vào Firestore để tránh viết lặp code
    private void savePostToFirestore(Post post, MutableLiveData<Resource<Boolean>> result) {
        db.collection("posts").add(post)
                .addOnSuccessListener(doc -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }
}