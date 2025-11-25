package com.example.nhom4.ui.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Mood;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.data.repository.AuthRepository;
import com.example.nhom4.data.repository.PostRepository;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {
    private final PostRepository postRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<List<Post>>> posts = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<Mood>>> moods = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> uploadStatus = new MutableLiveData<>();

    public MainViewModel() {
        postRepository = new PostRepository();
        authRepository = new AuthRepository();
        loadPosts();
        loadMoods();
    }

    public LiveData<Resource<List<Post>>> getPosts() { return posts; }
    public LiveData<Resource<List<Mood>>> getMoods() { return moods; }
    public LiveData<Resource<Boolean>> getUploadStatus() { return uploadStatus; }

    private void loadPosts() {
        postRepository.getPosts(posts);
    }

    private void loadMoods() {
        // Logic load Mood đơn giản, có thể chuyển vào MetadataRepository nếu muốn
        FirebaseFirestore.getInstance().collection("Mood").get()
                .addOnSuccessListener(snapshots -> {
                    List<Mood> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        list.add(new Mood(doc.getString("name"), doc.getString("iconUrl"), Boolean.TRUE.equals(doc.getBoolean("isPremium"))));
                    }
                    moods.postValue(Resource.success(list));
                });
    }

    public void createPost(String caption, String imagePath, Mood mood, String activityTitle) {
        if (authRepository.getCurrentUser() == null) return;

        Post post = new Post();
        post.setUserId(authRepository.getCurrentUser().getUid());
        post.setCaption(caption);
        post.setCreatedAt(com.google.firebase.Timestamp.now());

        if (mood != null) {
            post.setType("mood");
            post.setMoodName(mood.getName());
            post.setMoodIconUrl(mood.getIconUrl());
        } else {
            post.setType("activity");
            post.setActivityTitle(activityTitle);
        }

        Uri uri = imagePath != null ? Uri.parse("file://" + imagePath) : null;
        postRepository.createPost(post, uri, uploadStatus);
    }
}
