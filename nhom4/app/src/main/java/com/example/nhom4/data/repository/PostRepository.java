package com.example.nhom4.data.repository;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Activity;
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

public class PostRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // --- 1. GET POSTS (FEED) ---
    public void getPosts(MutableLiveData<Resource<List<Post>>> result) {
        if (auth.getCurrentUser() == null) {
            result.postValue(Resource.error("Chưa đăng nhập", null));
            return;
        }
        String currentUserId = auth.getCurrentUser().getUid();
        List<String> validUserIds = new ArrayList<>();
        validUserIds.add(currentUserId);

        // Lấy bạn bè
        db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .whereEqualTo("status", "accepted")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            List<String> members = (List<String>) doc.get("members");
                            if (members != null) {
                                for (String memberId : members) {
                                    if (!memberId.equals(currentUserId)) {
                                        validUserIds.add(memberId);
                                    }
                                }
                            }
                        }
                    }
                    // Dù có bạn hay không → bắt đầu listen realtime
                    listenToPosts(validUserIds, result);
                });
    }

    private void listenToPosts(List<String> validUserIds, MutableLiveData<Resource<List<Post>>> result) {
        result.postValue(Resource.loading(null));

        db.collection("posts")
                .whereIn("userId", validUserIds)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("PostRepo", "Listen failed.", error);
                        result.postValue(Resource.error("Lỗi kết nối", null));
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        result.postValue(Resource.success(new ArrayList<>()));
                        return;
                    }

                    List<Post> tempPosts = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            post.setPostId(doc.getId());
                            if (post.getCreatedAt() == null) {
                                post.setCreatedAt(com.google.firebase.Timestamp.now());
                            }
                            tempPosts.add(post);
                        }
                    }

                    // Trực tiếp fetch user info và emit → realtime nhanh, không delay
                    fetchUsersForPostsParallel(tempPosts, result);
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
                    post.setUserName("Người dùng");
                }
            }
            // Trả về UI danh sách mới nhất
            result.postValue(Resource.success(posts));
        });
    }

    // --- 2. CREATE POST ---
    public void createPost(Post post, Uri imageUri, MutableLiveData<Resource<Boolean>> result) {
        if (auth.getCurrentUser() == null) return;
        result.postValue(Resource.loading(null));

        if (imageUri != null) {
            String fileName = "posts/" + post.getUserId() + "/" + System.currentTimeMillis() + ".jpg";
            StorageReference ref = storage.getReference().child(fileName);

            ref.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                post.setPhotoUrl(uri.toString());
                                savePostToFirestore(post, result);
                            }))
                    .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
        } else {
            savePostToFirestore(post, result);
        }
    }

    private void savePostToFirestore(Post post, MutableLiveData<Resource<Boolean>> result) {

        // 🔥 TẠO DOCUMENT TRƯỚC → LẤY postId
        String postId = db.collection("posts").document().getId();

        // 🔥 GÁN postId VÀO POST
        post.setPostId(postId);

        // 🔥 LƯU VỚI ID ĐÃ BIẾT
        db.collection("posts")
                .document(postId)
                .set(post)
                .addOnSuccessListener(unused -> {
                    result.postValue(Resource.success(true));
                })
                .addOnFailureListener(e -> {
                    result.postValue(Resource.error(e.getMessage(), false));
                });
    }

    // --- 3. GET ALL USER POSTS (Dùng cho Story/Streak/Calendar) ---
    public void getAllUserPosts(MutableLiveData<Resource<List<Post>>> result) {
        if (auth.getCurrentUser() == null) {
            result.postValue(Resource.error("Chưa đăng nhập", null));
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();

        // Chỉ lấy bài của User hiện tại
        db.collection("posts")
                .whereEqualTo("userId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Post> userPosts = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            post.setPostId(doc.getId());

                            // [DEBUG QUAN TRỌNG] In log để kiểm tra xem có lấy được không
                            Log.d("DEBUG_STORY", "Lấy được post: " + post.getPostId() + " - Photo: " + post.getPhotoUrl());

                            userPosts.add(post);
                        }
                    }

                    if (userPosts.isEmpty()) {
                        Log.d("DEBUG_STORY", "Query thành công nhưng list rỗng (User chưa đăng bài nào)");
                    }

                    // Không cần load thông tin User vì đây là bài của chính mình
                    // Có thể set cứng thông tin user hiện tại nếu cần
                    result.postValue(Resource.success(userPosts));
                })
                .addOnFailureListener(e -> {
                    Log.e("DEBUG_STORY", "Lỗi query: " + e.getMessage());
                    result.postValue(Resource.error(e.getMessage(), null));
                });
    }
    // Lấy post mới nhất
    public void getLatestPost(MutableLiveData<Post> result) {
        if (auth.getCurrentUser() == null) return;

        db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            post.setPostId(doc.getId());
                            result.postValue(post);
                        }
                    }
                });
    }
}