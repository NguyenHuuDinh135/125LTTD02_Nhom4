package com.example.nhom4.data.repository;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;
import androidx.lifecycle.MutableLiveData;

public class StreakRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public void getAllUserPosts(MutableLiveData<Resource<List<Post>>> result) {
        if (auth.getCurrentUser() == null) return;

        // Lấy ID của user ĐANG ĐĂNG NHẬP
        String uid = auth.getCurrentUser().getUid();

        result.postValue(Resource.loading(null));

        // Chỉ query bài viết có userId == uid hiện tại
        db.collection("posts")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Post> posts = snapshots.toObjects(Post.class);
                    result.postValue(Resource.success(posts));
                })
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
    }
}
