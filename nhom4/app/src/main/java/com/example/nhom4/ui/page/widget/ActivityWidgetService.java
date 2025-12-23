    package com.example.nhom4.ui.page.widget;

    import android.app.PendingIntent;
    import android.appwidget.AppWidgetManager;
    import android.content.ComponentName;
    import android.content.Context;
    import android.content.Intent;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.util.Log;
    import android.widget.RemoteViews;

    import androidx.annotation.NonNull;
    import androidx.core.app.JobIntentService;

    import com.example.nhom4.MainActivity;
    import com.example.nhom4.R;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.Query;
    import com.google.android.gms.tasks.Tasks;
    import com.squareup.picasso.Picasso;

    import java.io.IOException;

    public class ActivityWidgetService extends JobIntentService {

        private static final int JOB_ID = 1001;

        public static void enqueueWork(Context context, @NonNull Intent work) {
            enqueueWork(context, ActivityWidgetService.class, JOB_ID, work);
        }

        @Override
        protected void onHandleWork(@NonNull Intent intent) {

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            try {
                var querySnapshot = Tasks.await(
                        db.collection("posts")
                                .orderBy("createdAt", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                );

                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);

                    // 🔥 LẤY POST ID TẠI ĐÂY
                    String postId = doc.getId();

                    // Lấy dữ liệu post
                    String type = doc.getString("type");
                    String title = doc.getString("activityTitle");
                    String caption = doc.getString("caption");
                    String moodName = doc.getString("moodName");

                    // Lấy thông tin user
                    String displayName = doc.getString("displayName");
                    String avatarUrl = doc.getString("avatarUrl");

                    // Xác định photoUrl theo type
                    String photoUrl = doc.getString("photoUrl");
                    if ("mood".equals(type) && (photoUrl == null || photoUrl.isEmpty())) {
                        photoUrl = doc.getString("moodIconUrl");
                    }

                    // 🔥 TRUYỀN dữ liệu xuống widget
                    updateWidget(this, postId, type, title, caption, photoUrl, moodName, displayName, avatarUrl);

                } else {
                    Log.d("WidgetService", "No posts found.");
                }

            } catch (Exception e) {
                Log.e("WidgetService", "Firestore fetch failed: " + e.getMessage(), e);
            }
        }



        private void updateWidget(Context context,String postId, String type, String title, String caption, String photoUrl, String moodName, String displayName,
                                  String avatarUrl) {
            // Tạo RemoteViews
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.activity_widget);

            // --- Set displayName ---
            views.setTextViewText(R.id.tv_display_name, displayName != null ? displayName : "Người dùng");

            // --- Load avatar ---
            try {
                Bitmap avatarBmp;
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    avatarBmp = Picasso.get()
                            .load(avatarUrl)
                            .resize(100, 100)  // avatar nhỏ
                            .centerCrop()
                            .get();
                } else {
                    avatarBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_image);
                }
                views.setImageViewBitmap(R.id.img_avatar, avatarBmp);
            } catch (IOException e) {
                e.printStackTrace();
                views.setImageViewResource(R.id.img_avatar, R.drawable.default_image);
            }

            // --- Set text theo type ---
            if ("activity".equals(type)) {
                views.setTextViewText(R.id.tv_title, title != null ? title : "Hoạt động");
                views.setTextViewText(R.id.tv_caption, caption != null ? caption : "");
            } else if ("mood".equals(type)) {
                views.setTextViewText(R.id.tv_title, moodName != null ? moodName : "");
                views.setTextViewText(R.id.tv_caption, caption != null ? caption : "");
            } else {
                views.setTextViewText(R.id.tv_title, "Post mới");
                views.setTextViewText(R.id.tv_caption, caption != null ? caption : "");
            }

            // --- Load ảnh post ---
            try {
                Bitmap bmp;
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    bmp = Picasso.get()
                            .load(photoUrl)
                            .resize(300, 300)
                            .centerCrop()
                            .get();
                } else {
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_image);
                }
                views.setImageViewBitmap(R.id.img_photo, bmp);
            } catch (IOException e) {
                e.printStackTrace();
                views.setImageViewResource(R.id.img_photo, R.drawable.default_image);
            }

            // --- Click mở app ---
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("OPEN_POST", true);
            intent.putExtra("POST_ID", postId);
            intent.putExtra("POST_TYPE", type);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.widget_root_layout, pendingIntent);

            // --- Cập nhật widget ---
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName widget = new ComponentName(context, ActivityWidgetProvider.class);
            manager.updateAppWidget(widget, views);
        }
    }