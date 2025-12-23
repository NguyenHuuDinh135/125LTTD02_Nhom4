package com.example.nhom4.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.nhom4.data.bean.Post;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

public class ActivityPostsViewModel extends ViewModel {

    private final MutableLiveData<List<Post>> posts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public void loadPostsForActivity(String activityId) {
        isLoading.setValue(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("logs")
                .whereEqualTo("activityId", activityId)
                .whereEqualTo("type", "activity")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Post> postList = snapshot.toObjects(Post.class);

                    for (int i = 0; i < postList.size(); i++) {
                        Post post = postList.get(i);
                        if (post.getPostId() == null || post.getPostId().isEmpty()) {
                            post.setPostId(snapshot.getDocuments().get(i).getId());
                        }
                        if (post.getActivityId() == null) {
                            post.setActivityId(activityId);
                        }
                    }

                    posts.postValue(postList);
                    isLoading.postValue(false);
                })
                .addOnFailureListener(e -> {
                    error.postValue("Lỗi tải bài đăng: " + e.getMessage());
                    posts.postValue(null);
                    isLoading.postValue(false);
                });
    }

    public LiveData<List<Post>> getPosts() {
        return posts;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }
}