package com.example.nhom4.ui.page.friend;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.bean.FriendRequest;
import com.example.nhom4.data.model.Relationship;
import com.example.nhom4.data.repository.FriendRepository;

import java.util.List;

public class FriendViewModel extends ViewModel {

    private final FriendRepository repository;
    private LiveData<List<FriendRequest>> incomingRequests;
    private LiveData<List<Relationship>> friends;

    public FriendViewModel() {
        repository = new FriendRepository();
    }

    public void sendFriendRequest(String requesterId, String recipientId) {
        FriendRequest request = new FriendRequest(requesterId, recipientId);
        repository.sendFriendRequest(request);
    }

    public LiveData<List<FriendRequest>> getIncomingRequests(String userId) {
        incomingRequests = repository.getIncomingRequests(userId);
        return incomingRequests;
    }

    public void respondToRequest(String requestId, boolean accept) {
        repository.respondToRequest(requestId, accept);
    }

    public LiveData<List<Relationship>> getFriends(String userId) {
        if (friends == null) {
            friends = repository.getFriends(userId);
        }
        return friends;
    }
}