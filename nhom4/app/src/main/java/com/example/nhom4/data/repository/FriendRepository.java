package com.example.nhom4.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.nhom4.data.model.FriendRequest;
import com.example.nhom4.data.model.Relationship;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendRepository {

    private final FirebaseFirestore firestore;
    private final CollectionReference friendRequestRef;
    private final CollectionReference relationshipRef;

    public FriendRepository() {
        firestore = FirebaseFirestore.getInstance();
        friendRequestRef = firestore.collection("friend_requests");
        relationshipRef = firestore.collection("relationships");
    }

    // Gửi lời mời
    public void sendFriendRequest(FriendRequest request) {
        String requestId = friendRequestRef.document().getId();
        request.setRequestId(requestId);
        friendRequestRef.document(requestId).set(request);
    }

    // Lấy danh sách yêu cầu đến (incoming) cho user
    public LiveData<List<FriendRequest>> getIncomingRequests(String userId) {
        MutableLiveData<List<FriendRequest>> liveData = new MutableLiveData<>();

        relationshipRef
                .whereEqualTo("recipientId", userId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) return;

                    List<FriendRequest> list = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            String requesterId = doc.getString("requesterId");
                            String recipientId = doc.getString("recipientId");
                            String status = doc.getString("status");
                            List<String> members = (List<String>) doc.get("members");

                            FriendRequest fr = new FriendRequest();
                            fr.setRequestId(doc.getId());
                            fr.setRequesterId(requesterId);
                            fr.setRecipientId(recipientId);
                            fr.setStatus(status);
                            fr.setMembers(members);

                            list.add(fr);
                        }
                    }
                    liveData.setValue(list);
                });

        return liveData;
    }


    // Chấp nhận hoặc từ chối
    public void respondToRequest(String requestId, boolean accept) {
        // Update trực tiếp trong collection relationships
        relationshipRef.document(requestId).update("status", accept ? "accepted" : "declined");
    }

    // Lấy danh sách bạn bè
    public LiveData<List<Relationship>> getFriends(String userId) {
        MutableLiveData<List<Relationship>> liveData = new MutableLiveData<>();

        relationshipRef.whereArrayContains("members", userId)
                .addSnapshotListener((value, error) -> {
                    if (value == null || error != null) return;

                    List<Relationship> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) {
                        Relationship r = doc.toObject(Relationship.class);
                        list.add(r);
                    }
                    liveData.postValue(list);
                });

        return liveData;
    }

    // Lấy danh sách gợi ý bạn bè (chưa phải bạn và chưa gửi request)
    public LiveData<List<String>> getSuggestions(String currentUserId) {
        MutableLiveData<List<String>> liveData = new MutableLiveData<>();

        // Ví dụ: lấy tất cả user (cần collection "users") và loại trừ bạn bè + đã gửi request
        // Đây là ví dụ đơn giản, bạn cần implement logic thực tế
        firestore.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> suggestions = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String uid = doc.getId();
                        if (!uid.equals(currentUserId)) {
                            suggestions.add(doc.getString("username"));
                        }
                    }
                    liveData.postValue(suggestions);
                });

        return liveData;
    }
}
