package com.example.nhom4.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Activity;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.data.repository.PostRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DetailViewModel extends ViewModel {

    private final PostRepository postRepository;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final MutableLiveData<Resource<Activity>> activityDetail = new MutableLiveData<>();
    private final MutableLiveData<Resource<Integer>> progress = new MutableLiveData<>();

    private Activity currentActivity;

    public DetailViewModel() {
        postRepository = new PostRepository();
    }

    /**
     * Set Activity hiện tại (gọi từ DetailActivity)
     */
    public void setCurrentActivity(Activity activity) {
        this.currentActivity = activity;
        activityDetail.postValue(Resource.success(activity));
        loadProgress();
        // Không cần load check-in ở đây, sẽ load khi cần qua isDayCheckedIn()
    }

    public LiveData<Resource<Activity>> getActivityDetail() {
        return activityDetail;
    }

    /**
     * Load progress (số lần check-in / target)
     */
    public void loadProgress() {
        if (currentActivity == null || currentActivity.getId() == null) return;

        db.collection("posts")
                .whereEqualTo("activityId", currentActivity.getId())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    progress.postValue(Resource.success(count));
                })
                .addOnFailureListener(e -> progress.postValue(Resource.error(e.getMessage(), 0)));
    }

    public LiveData<Resource<Integer>> getProgress() {
        return progress;
    }

    /**
     * Kiểm tra ngày có check-in không (dùng cho grid ngày)
     * @param day Ngày trong tháng (1-31)
     * @return true nếu có ít nhất 1 post check-in trong ngày đó
     */
    public LiveData<Boolean> isDayCheckedIn(String activityId, int day) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startOfDay = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = cal.getTime();

        db.collection("posts")
                .whereEqualTo("activityId", activityId)
                .whereGreaterThanOrEqualTo("createdAt", new Timestamp(startOfDay))
                .whereLessThan("createdAt", new Timestamp(endOfDay))
                .get()
                .addOnSuccessListener(query -> {
                    result.postValue(!query.isEmpty());
                })
                .addOnFailureListener(e -> result.postValue(false));

        return result;
    }
    public LiveData<String> getFirstPhotoUrlOfDay(String activityId, int day) {
        MutableLiveData<String> result = new MutableLiveData<>();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startOfDay = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = cal.getTime();

        db.collection("posts")
                .whereEqualTo("activityId", activityId)
                .whereGreaterThanOrEqualTo("createdAt", new Timestamp(startOfDay))
                .whereLessThan("createdAt", new Timestamp(endOfDay))
                .orderBy("createdAt", Query.Direction.DESCENDING) // Mới nhất trước
                .limit(1) // Chỉ lấy 1 post đầu
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Post post = query.getDocuments().get(0).toObject(Post.class);
                        if (post != null && post.getPhotoUrl() != null && !post.getPhotoUrl().isEmpty()) {
                            result.postValue(post.getPhotoUrl());
                        } else {
                            result.postValue(null);
                        }
                    } else {
                        result.postValue(null);
                    }
                })
                .addOnFailureListener(e -> result.postValue(null));

        return result;
    }
}