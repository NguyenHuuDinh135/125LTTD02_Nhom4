package com.example.nhom4.ui.page.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom4.R;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.ui.adapter.VerticalPagerAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CenterFragment extends Fragment {

    private ViewPager2 viewPagerVertical;
    private VerticalPagerAdapter adapter;
    private FirebaseFirestore db; // Khai báo Firestore

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_center_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance(); // Khởi tạo Firestore

        viewPagerVertical = view.findViewById(R.id.viewPagerVertical);

        // Gán Adapter
        adapter = new VerticalPagerAdapter(this);
        viewPagerVertical.setAdapter(adapter);
        viewPagerVertical.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        // Tải dữ liệu thật ngay khi tạo view
        loadPostsFromFirebase();
    }

    // Hàm load dữ liệu từ Firebase
    private void loadPostsFromFirebase() {
        db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING) // Bài mới nhất lên trên (ngay sau Camera)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> postList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            post.setPostId(doc.getId());
                            postList.add(post);
                        }
                    }

                    // Cập nhật dữ liệu vào Adapter
                    if (adapter != null) {
                        adapter.setPostList(postList);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CenterFragment", "Lỗi tải bài viết", e);
                    Toast.makeText(getContext(), "Không thể tải bài viết", Toast.LENGTH_SHORT).show();
                });
    }

    public void navigateToCamera() {
        if (viewPagerVertical != null) {
            viewPagerVertical.setCurrentItem(0, true);
        }
    }

    // Reload lại khi quay lại màn hình này (ví dụ sau khi đăng bài xong)
    @Override
    public void onResume() {
        super.onResume();
        loadPostsFromFirebase();
    }
}
