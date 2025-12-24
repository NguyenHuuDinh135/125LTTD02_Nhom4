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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import com.example.nhom4.data.bean.PostFilterType;
public class PostRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // Biến để quản lý Listener, giúp hủy lắng nghe khi cần thiết tránh rò rỉ bộ nhớ
    private ListenerRegistration postsListenerRegistration;
    private ListenerRegistration friendsListenerRegistration;

    // --- 1. GET POSTS (FEED) - ĐÃ SỬA REALTIME ---
    public void getPosts(MutableLiveData<Resource<List<Post>>> result) {
        if (auth.getCurrentUser() == null) {
            result.postValue(Resource.error("Chưa đăng nhập", null));
            return;
        }
        String currentUserId = auth.getCurrentUser().getUid();

        // Bước 1: Lắng nghe danh sách bạn bè Realtime
        // Thay vì .get() -> dùng .addSnapshotListener
        friendsListenerRegistration = db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .whereEqualTo("status", "accepted")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("PostRepo", "Lỗi lấy bạn bè", error);
                        return;
                    }

                    List<String> validUserIds = new ArrayList<>();
                    validUserIds.add(currentUserId); // Luôn thêm bản thân để thấy bài mình

                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots) {
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

                    // Bước 2: Khi danh sách bạn bè thay đổi -> Gọi lại hàm lắng nghe bài viết
                    // Danh sách ID mới sẽ bao gồm người bạn vừa kết bạn
                    listenToPosts(validUserIds, result);
                });
    }

    private void listenToPosts(List<String> validUserIds, MutableLiveData<Resource<List<Post>>> result) {
        // [QUAN TRỌNG] Hủy listener cũ trước khi tạo cái mới để tránh trùng lặp dữ liệu
        if (postsListenerRegistration != null) {
            postsListenerRegistration.remove();
        }

        result.postValue(Resource.loading(null));

        // Lưu ý: Firestore giới hạn 'whereIn' tối đa 10 giá trị.
        // Nếu bạn bè > 10, cần giải thuật chia nhỏ list (chunking).
        // Tạm thời code này hoạt động tốt với < 10 người (bao gồm bản thân).

        if (validUserIds.isEmpty()) {
            result.postValue(Resource.success(new ArrayList<>()));
            return;
        }

        postsListenerRegistration = db.collection("posts")
                .whereIn("userId", validUserIds) // Lọc bài viết theo danh sách ID mới nhất
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("PostRepo", "Listen posts failed.", error);
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
                            // Fallback nếu chưa có timestamp
                            if (post.getCreatedAt() == null) {
                                post.setCreatedAt(com.google.firebase.Timestamp.now());
                            }
                            tempPosts.add(post);
                        }
                    }

                    // Lấy thông tin user (Tên, Avatar) cho từng bài viết
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

    // --- 2. CREATE POST (GIỮ NGUYÊN) ---
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
        String postId = db.collection("posts").document().getId();
        post.setPostId(postId);

        db.collection("posts")
                .document(postId)
                .set(post)
                .addOnSuccessListener(unused -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }

    // --- 3. GET ALL USER POSTS (GIỮ NGUYÊN) ---
    public void getAllUserPosts(MutableLiveData<Resource<List<Post>>> result) {
        if (auth.getCurrentUser() == null) {
            result.postValue(Resource.error("Chưa đăng nhập", null));
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();

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
                            userPosts.add(post);
                        }
                    }
                    result.postValue(Resource.success(userPosts));
                })
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
    }

    // --- HỦY LISTENER KHI KHÔNG CẦN THIẾT (Optional) ---
    public void cleanup() {
        if (postsListenerRegistration != null) postsListenerRegistration.remove();
        if (friendsListenerRegistration != null) friendsListenerRegistration.remove();
    }
    /**
     * @param targetUserId: ID của người muốn xem (dùng khi type = SPECIFIC_USER hoặc SELF)
     */
    public void getPosts(MutableLiveData<Resource<List<Post>>> result, PostFilterType filterType, String targetUserId) {
        if (auth.getCurrentUser() == null) {
            result.postValue(Resource.error("Chưa đăng nhập", null));
            return;
        }
        String currentUserId = auth.getCurrentUser().getUid();

        // -------------------------------------------------------------
        // TRƯỜNG HỢP 1: LỌC MỘT NGƯỜI CỤ THỂ (Bản thân hoặc 1 người bạn)
        // -------------------------------------------------------------
        if (filterType == PostFilterType.SPECIFIC_USER || filterType == PostFilterType.SELF) {
            // Xác định ID cần lấy: nếu là SELF thì lấy currentUserId, nếu SPECIFIC thì lấy targetUserId
            String finalTargetId = (filterType == PostFilterType.SELF) ? currentUserId : targetUserId;

            if (finalTargetId == null) return;

            // Hủy listener cũ của Feed (nếu có)
            if (friendsListenerRegistration != null) friendsListenerRegistration.remove();

            // Gọi hàm lắng nghe đơn giản chỉ cho 1 user
            listenToPosts(java.util.Collections.singletonList(finalTargetId), result);
            return;
        }

        // -------------------------------------------------------------
        // TRƯỜNG HỢP 2: LẤY FEED TỔNG HỢP (ALL) - Giữ nguyên logic cũ
        // -------------------------------------------------------------
        if (filterType == PostFilterType.ALL) {
            if (friendsListenerRegistration != null) {
                friendsListenerRegistration.remove();
            }

            friendsListenerRegistration = db.collection("relationships")
                    .whereArrayContains("members", currentUserId)
                    .whereEqualTo("status", "accepted")
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null) {
                            Log.e("PostRepo", "Lỗi lấy bạn bè", error);
                            return;
                        }

                        List<String> validUserIds = new ArrayList<>();
                        validUserIds.add(currentUserId); // Luôn thấy bài mình

                        if (snapshots != null) {
                            for (DocumentSnapshot doc : snapshots) {
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
                        // Lắng nghe bài viết từ danh sách ID này
                        listenToPosts(validUserIds, result);
                    });
        }
    }
}