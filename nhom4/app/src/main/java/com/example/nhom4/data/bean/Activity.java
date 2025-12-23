package com.example.nhom4.data.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Activity implements Parcelable {
    private String id;
    private String creatorId;
    private String title;
    private String imageUrl;

    // --- CẤU HÌNH MỤC TIÊU ---
    private boolean isDaily;     // [MỚI] True: Hằng ngày, False: Số lần cụ thể
    private int progress;        // Số lần đã làm
    private int target;          // Mục tiêu (Nếu daily thì target=1/ngày, nếu custom thì là tổng số)

    // --- CẤU HÌNH THỜI GIAN ---
    private long durationSeconds; // [QUAN TRỌNG] (Kết thúc - Bắt đầu)
    private Timestamp scheduledTime; // Lưu giờ bắt đầu (ngày tạo + giờ user chọn)
    private boolean isReminderEnabled;

    private List<String> participants;
    private Timestamp createdAt;

    public Activity() { }

    // Constructor đầy đủ
    public Activity(String creatorId, String title, boolean isDaily, int target, long durationSeconds, Timestamp scheduledTime) {
        this.creatorId = creatorId;
        this.title = title;
        this.isDaily = isDaily;
        this.target = target;
        this.durationSeconds = durationSeconds;
        this.scheduledTime = scheduledTime;

        this.progress = 0;
        this.isReminderEnabled = true;
        this.createdAt = Timestamp.now();
        this.participants = new ArrayList<>();
        this.participants.add(creatorId);
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isDaily() { return isDaily; }
    public void setDaily(boolean daily) { isDaily = daily; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public int getTarget() { return target; }
    public void setTarget(int target) { this.target = target; }

    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }

    public Timestamp getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(Timestamp scheduledTime) { this.scheduledTime = scheduledTime; }

    public boolean isReminderEnabled() { return isReminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { isReminderEnabled = reminderEnabled; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // --- Parcelable Implementation ---
    protected Activity(Parcel in) {
        id = in.readString();
        creatorId = in.readString();
        title = in.readString();
        imageUrl = in.readString();
        isDaily = in.readByte() != 0;
        progress = in.readInt();
        target = in.readInt();
        durationSeconds = in.readLong();
        scheduledTime = in.readParcelable(Timestamp.class.getClassLoader());
        isReminderEnabled = in.readByte() != 0;
        participants = in.createStringArrayList();
        createdAt = in.readParcelable(Timestamp.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(creatorId);
        dest.writeString(title);
        dest.writeString(imageUrl);
        dest.writeByte((byte) (isDaily ? 1 : 0));
        dest.writeInt(progress);
        dest.writeInt(target);
        dest.writeLong(durationSeconds);
        dest.writeParcelable(scheduledTime, flags);
        dest.writeByte((byte) (isReminderEnabled ? 1 : 0));
        dest.writeStringList(participants);
        dest.writeParcelable(createdAt, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Activity> CREATOR = new Creator<Activity>() {
        @Override
        public Activity createFromParcel(Parcel in) {
            return new Activity(in);
        }

        @Override
        public Activity[] newArray(int size) {
            return new Activity[size];
        }
    };
}