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

public class PostRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // [SỬA] Hàm lấy bài viết: Chỉ lấy bài của bạn bè + chính mình
    public void getPosts(MutableLiveData<Resource<List<Post>>> result) {
        if (auth.getCurrentUser() == null) return;
        String currentUserId = auth.getCurrentUser().getUid();

        result.postValue(Resource.loading(null));

        // BƯỚC 1: Lấy danh sách ID bạn bè (những người có status = "accepted")
        db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<String> friendIds = new ArrayList<>();
                    friendIds.add(currentUserId); // Thêm chính mình vào để thấy bài của mình

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

                    // BƯỚC 2: Query bài viết
                    db.collection("posts")
                            .orderBy("createdAt", Query.Direction.DESCENDING)
                            .addSnapshotListener((postSnapshots, e) -> {
                                if (e != null) {
                                    result.postValue(Resource.error(e.getMessage(), null));
                                    return;
                                }

                                if (postSnapshots != null) {
                                    List<Post> filteredList = new ArrayList<>();

                                    // Lọc bài viết (Client-side filtering)
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

                                    // Nếu không có bài nào
                                    if (filteredList.isEmpty()) {
                                        result.postValue(Resource.success(new ArrayList<>()));
                                        return;
                                    }

                                    // BƯỚC 3: Load tên người dùng cho các bài viết (để hiển thị tên thay vì ID)
                                    loadUserInfoRecursive(filteredList, 0, new ArrayList<>(), result);
                                }
                            });
                })
                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi lấy danh sách bạn: " + e.getMessage(), null)));
    }

    // Helper: Load thông tin user tuần tự (để tránh bất đồng bộ lộn xộn)
    private void loadUserInfoRecursive(List<Post> sourceList, int index, List<Post> finalResult, MutableLiveData<Resource<List<Post>>> liveData) {
        if (index >= sourceList.size()) {
            liveData.postValue(Resource.success(finalResult));
            return;
        }

        Post currentPost = sourceList.get(index);
        db.collection("users").document(currentPost.getUserId()).get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        currentPost.setUserName(userDoc.getString("username"));
                        currentPost.setUserAvatar(userDoc.getString("profilePhotoUrl"));
                    } else {
                        currentPost.setUserName("Người dùng");
                    }
                    finalResult.add(currentPost);
                    loadUserInfoRecursive(sourceList, index + 1, finalResult, liveData);
                })
                .addOnFailureListener(e -> {
                    currentPost.setUserName("Người dùng");
                    finalResult.add(currentPost);
                    loadUserInfoRecursive(sourceList, index + 1, finalResult, liveData);
                });
    }

    public void createPost(Post post, Uri imageUri, MutableLiveData<Resource<Boolean>> result) {
        result.postValue(Resource.loading(null));

        if (imageUri != null) {
            String fileName = "posts/" + post.getUserId() + "/" + System.currentTimeMillis() + ".jpg";
            StorageReference ref = storage.getReference().child(fileName);

            ref.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        post.setPhotoUrl(uri.toString());
                        savePostToFirestore(post, result);
                    }))
                    .addOnFailureListener(e -> result.postValue(Resource.error("Upload failed: " + e.getMessage(), false)));
        } else {
            savePostToFirestore(post, result);
        }
    }

    private void savePostToFirestore(Post post, MutableLiveData<Resource<Boolean>> result) {
        db.collection("posts").add(post)
                .addOnSuccessListener(doc -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }
}
