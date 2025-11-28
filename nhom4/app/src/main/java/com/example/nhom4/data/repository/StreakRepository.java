package com.example.nhom4.data.repository;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;
import androidx.lifecycle.MutableLiveData;

/**
 * StreakRepository
 * --------------------------------------------------
 * Class này chịu trách nhiệm lấy dữ liệu bài viết của CHÍNH NGƯỜI DÙNG HIỆN TẠI.
 *
 * Mục đích sử dụng:
 * 1. Hiển thị danh sách bài viết trên trang Profile (Trang cá nhân).
 * 2. Cung cấp dữ liệu để tính toán "Streak" (Chuỗi ngày đăng bài liên tiếp).
 */
public class StreakRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    /**
     * Hàm lấy toàn bộ bài viết của User đang đăng nhập.
     * Khác với Newfeed (lấy bài bạn bè), hàm này filter cứng theo UID của mình.
     */
    public void getAllUserPosts(MutableLiveData<Resource<List<Post>>> result) {
        if (auth.getCurrentUser() == null) return;

        // Lấy ID của user ĐANG ĐĂNG NHẬP
        String uid = auth.getCurrentUser().getUid();

        // Báo UI hiển thị loading
        result.postValue(Resource.loading(null));

        // Thực hiện Query Firestore:
        // 1. whereEqualTo("userId", uid): Chỉ lấy bài viết do chính mình tạo ra.
        // 2. orderBy("createdAt", DESCENDING): Sắp xếp bài mới nhất lên đầu.
        //    -> Rất quan trọng để thuật toán tính Streak kiểm tra ngày gần nhất nhanh hơn.
        // 3. get(): Chỉ lấy dữ liệu 1 lần (One-time fetch).
        //    Lý do: Profile cá nhân không cần cập nhật liên tục realtime như Chat hay Feed, giúp tiết kiệm chi phí đọc DB.
        db.collection("posts")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    // Convert dữ liệu từ DocumentSnapshot sang List<Post>
                    List<Post> posts = snapshots.toObjects(Post.class);

                    // Trả về danh sách thành công
                    result.postValue(Resource.success(posts));
                })
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
    }
}