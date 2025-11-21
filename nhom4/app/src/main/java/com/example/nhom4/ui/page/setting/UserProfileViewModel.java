package com.example.nhom4.ui.page.setting;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.bean.UserProfile;
import com.example.nhom4.data.repository.UserProfileRepository;

public class UserProfileViewModel extends ViewModel {

    private final UserProfileRepository repository;
    private final MutableLiveData<UserProfile> userProfileLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public UserProfileViewModel() {
        repository = new UserProfileRepository();
    }

    public LiveData<UserProfile> getUserProfileLiveData() {
        return userProfileLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    /** Load profile từ repository */
    public void loadUserProfile(String uid) {
        repository.getUserProfile(uid, new UserProfileRepository.OnUserProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                userProfileLiveData.setValue(profile);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    /** Cập nhật toàn bộ profile */
    public void updateUserProfile(UserProfile profile) {
        repository.updateUserProfile(profile, new UserProfileRepository.OnUserProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                userProfileLiveData.setValue(profile);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }
    // UserProfileViewModel.java
    public void logoutUser() {
        repository.logout();
    }

    public void updateUserProfileField(String uid, String field, String value) {
        repository.updateField(uid, field, value,
                aVoid -> {
                    // Cập nhật thành công local LiveData
                    UserProfile profile = userProfileLiveData.getValue();
                    if (profile != null) {
                        switch (field) {
                            case "username": profile.setUsername(value); break;
                            case "email": profile.setEmail(value); break;
                            case "birthday": profile.setBirthday(value); break;
                        }
                        userProfileLiveData.setValue(profile);
                    }
                },
                e -> errorLiveData.setValue(e.getMessage())
        );
    }
}
