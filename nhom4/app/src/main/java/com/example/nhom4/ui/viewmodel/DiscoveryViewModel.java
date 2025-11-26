package com.example.nhom4.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Activity; // Import Activity
import com.example.nhom4.data.bean.Conversation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // LiveData cho Chat
    private final MutableLiveData<Resource<List<Conversation>>> conversations = new MutableLiveData<>();

    // [MỚI] LiveData cho Activity
    private final MutableLiveData<Resource<List<Activity>>> activities = new MutableLiveData<>();

    public LiveData<Resource<List<Conversation>>> getConversations() { return conversations; }

    // [MỚI] Getter cho Activity
    public LiveData<Resource<List<Activity>>> getActivities() { return activities; }

    // 1. Load Chat (Code cũ giữ nguyên)
    public void loadConversations() {
        if (auth.getCurrentUser() == null) return;
        String currentUserId = auth.getCurrentUser().getUid();
        conversations.postValue(Resource.loading(null));

        db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Conversation> list = new ArrayList<>();
                    if (snapshots.isEmpty()) {
                        conversations.postValue(Resource.success(list));
                        return;
                    }
                    for (DocumentSnapshot doc : snapshots) {
                        String conversationId = doc.getId();
                        List<String> members = (List<String>) doc.get("members");
                        if (members != null) {
                            String friendId = members.get(0).equals(currentUserId) ? members.get(1) : members.get(0);
                            fetchFriendInfo(conversationId, friendId, list, snapshots.size());
                        }
                    }
                })
                .addOnFailureListener(e -> conversations.postValue(Resource.error(e.getMessage(), null)));
    }

    private void fetchFriendInfo(String conversationId, String friendId, List<Conversation> list, int totalExpected) {
        db.collection("users").document(friendId).get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String name = userDoc.getString("username");
                        String avatar = userDoc.getString("profilePhotoUrl");
                        Conversation conv = new Conversation(friendId, name, avatar, "Nhắn tin ngay nào!", System.currentTimeMillis());
                        conv.setConversationId(conversationId);
                        list.add(conv);
                    }
                    if (list.size() > 0) {
                        conversations.postValue(Resource.success(list));
                    }
                });
    }

    // 2. [MỚI] Load Activity User đã tham gia
    public void loadJoinedActivities() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();
        activities.postValue(Resource.loading(null));

        // Lắng nghe realtime collection activities
        db.collection("activities")
                .whereArrayContains("participants", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        activities.postValue(Resource.error(error.getMessage(), null));
                        return;
                    }
                    if (value != null) {
                        List<Activity> list = value.toObjects(Activity.class);
                        activities.postValue(Resource.success(list));
                    }
                });
    }
}
