package com.example.nhom4.ui.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Activity;
import com.example.nhom4.data.bean.Mood;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * ViewModel phụ trách CRUD hoạt động cá nhân và logic mở khoá mood thưởng.
 */
public class ActivityViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private final MutableLiveData<Resource<List<Activity>>> myActivities = new MutableLiveData<>();
    private final MutableLiveData<Resource<Mood>> unlockedMood = new MutableLiveData<>();

    public LiveData<Resource<List<Activity>>> getMyActivities() { return myActivities; }
    public LiveData<Resource<Mood>> getUnlockedMood() { return unlockedMood; }

    // 1. Load danh sách Activity (Realtime)
    public void loadMyActivities() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        myActivities.postValue(Resource.loading(null));

        // LƯU Ý: Query này YÊU CẦU INDEX trong Firestore Console.
        // Nếu log báo lỗi "The query requires an index", hãy bấm vào link trong log để tạo.
        db.collection("activities")
                .whereArrayContains("participants", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        myActivities.postValue(Resource.error(error.getMessage(), null));
                        return;
                    }
                    if (value != null) {
                        List<Activity> list = value.toObjects(Activity.class); // Firestore mapping sang model
                        myActivities.postValue(Resource.success(list));
                    }
                });
    }

    // 2. Tạo Activity mới (Có upload ảnh)
    public void createActivity(String title, String desc, Uri imageUri) {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();
        String id = db.collection("activities").document().getId();

        if (imageUri != null) {
            // Upload ảnh trước
            String fileName = UUID.randomUUID().toString() + ".jpg";
            StorageReference storageRef = storage.getReference().child("activity_images/" + fileName);

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Có URL ảnh -> Lưu Activity
                        saveActivityToFirestore(id, uid, title, desc, uri.toString());
                    }))
                    .addOnFailureListener(e -> {
                        // Lỗi upload -> Vẫn lưu activity nhưng không có ảnh (hoặc xử lý báo lỗi)
                        saveActivityToFirestore(id, uid, title, desc, null);
                    });
        } else {
            // Không chọn ảnh
            saveActivityToFirestore(id, uid, title, desc, null);
        }
    }

    /**
     * Lưu dữ liệu activity xuống Firestore sau khi đã có (hoặc không có) URL ảnh.
     */
    private void saveActivityToFirestore(String id, String uid, String title, String desc, String imageUrl) {
        // Constructor chỉ có (creatorId, title) -> Các field khác dùng Setter
        Activity activity = new Activity(uid, title);
        activity.setId(id);
        activity.setDescription(desc);
        if (imageUrl != null) {
            activity.setImageUrl(imageUrl);
        }

        db.collection("activities").document(id).set(activity);
    }

    // 3. Logic Random Mood (khi đủ progress)
    public void incrementProgress(String activityId) {
        db.collection("activities").document(activityId).get().addOnSuccessListener(doc -> {
            Activity act = doc.toObject(Activity.class);
            if (act != null && !act.isRewardClaimed()) {
                int newProgress = act.getProgress() + 1;
                db.collection("activities").document(activityId).update("progress", newProgress); // Đồng bộ progress mới

                if (newProgress >= act.getTarget()) {
                    unlockRandomPremiumMood(activityId);
                }
            }
        });
    }

    /**
     * Khi đạt target, random một mood premium và lưu vào user.
     */
    private void unlockRandomPremiumMood(String activityId) {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        List<Mood> premiums = new ArrayList<>();
        premiums.add(new Mood("Vua Hề", "https://img.icons8.com/emoji/96/clown-face.png", true));
        premiums.add(new Mood("Siêu Saiyan", "https://img.icons8.com/emoji/96/superhero.png", true));

        Mood reward = premiums.get(new Random().nextInt(premiums.size()));

        db.collection("activities").document(activityId).update("isRewardClaimed", true);
        db.collection("users").document(uid).collection("unlockedMoods").add(reward)
                .addOnSuccessListener(doc -> unlockedMood.postValue(Resource.success(reward)));
    }
}
