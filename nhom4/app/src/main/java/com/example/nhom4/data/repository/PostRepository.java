// File: com/example/nhom4/data/repository/PostRepository.java
package com.example.nhom4.data.repository;

import android.net.Uri;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Post;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.List;
import androidx.lifecycle.MutableLiveData;

public class PostRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    public void getPosts(MutableLiveData<Resource<List<Post>>> result) {
        result.postValue(Resource.loading(null));
        db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        result.postValue(Resource.error(e.getMessage(), null));
                        return;
                    }
                    if (snapshots != null) {
                        List<Post> posts = snapshots.toObjects(Post.class);
                        // Map ID nếu cần thiết vì toObjects không tự map ID document
                        for (int i=0; i<snapshots.size(); i++) {
                            posts.get(i).setPostId(snapshots.getDocuments().get(i).getId());
                        }
                        result.postValue(Resource.success(posts));
                    }
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
