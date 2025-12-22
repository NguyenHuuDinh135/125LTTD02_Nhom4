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
        result.postValue(Resource.loading(null));

        // B1: Lấy danh sách ID bạn bè
        db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<String> friendIds = new ArrayList<>();
                    friendIds.add(currentUserId); // Thêm mình

                    for (DocumentSnapshot doc : snapshots) {
                        List<String> members = (List<String>) doc.get("members");
                        if (members != null) {
                            for (String memberId : members) {
                                if (!memberId.equals(currentUserId)) friendIds.add(memberId);
                            }
                        }
                    }

                    // B2: Lắng nghe Realtime -> Tự động cập nhật khi có bài mới
                    db.collection("posts")
                            .orderBy("createdAt", Query.Direction.DESCENDING)
                            .addSnapshotListener((postSnapshots, e) -> {
                                if (e != null) {
                                    result.postValue(Resource.error(e.getMessage(), null));
                                    return;
                                }

                                if (postSnapshots != null) {
                                    List<Post> tempPosts = new ArrayList<>();
                                    for (DocumentSnapshot doc : postSnapshots) {
                                        Post post = doc.toObject(Post.class);
                                        if (post != null && friendIds.contains(post.getUserId())) {
                                            post.setPostId(doc.getId());
                                            tempPosts.add(post);
                                        }
                                    }

                                    if (tempPosts.isEmpty()) {
                                        result.postValue(Resource.success(new ArrayList<>()));
                                        return;
                                    }

                                    // B3: Load User Info Song Song
                                    fetchUsersForPostsParallel(tempPosts, result);
                                }
                            });
                })
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
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
        // Khi lưu thành công, Listener ở trên (getPosts) sẽ tự nhận tín hiệu và update UI
        db.collection("posts").add(post)
                .addOnSuccessListener(doc -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
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
}