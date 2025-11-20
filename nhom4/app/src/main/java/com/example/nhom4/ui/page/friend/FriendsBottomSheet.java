package com.example.nhom4.ui.page.friend;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.Arrays;
import java.util.List;

public class FriendsBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        return inflater.inflate(R.layout.bottom_sheet_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rcvFriends = view.findViewById(R.id.rcvFriends);
        RecyclerView rcvSuggest = view.findViewById(R.id.rcvSuggestions);


        List<String> friends = Arrays.asList("Ava Thompson", "Jessica", "Kyle Sanok");
        List<String> suggest = Arrays.asList("Sabrina", "Laurel Marsden", "Britney");

        rcvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvFriends.setAdapter(new FriendsAdapter(friends));

        rcvSuggest.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvSuggest.setAdapter(new SuggestionAdapter(suggest));
    }
    @Override
    public void onStart() {
        super.onStart();

        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

        if (bottomSheet != null) {
            // Set chiều cao ~95% màn hình
            int height = (int) (Resources.getSystem().getDisplayMetrics().heightPixels * 0.95);
            bottomSheet.getLayoutParams().height = height;
            bottomSheet.requestLayout();

            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setPeekHeight(height);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            // Bo góc chuẩn
            ShapeAppearanceModel shapeModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setTopLeftCornerSize(24f)
                    .setTopRightCornerSize(24f)
                    .build();

            int colorSurface = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorSurface, 0);
            MaterialShapeDrawable drawable = new MaterialShapeDrawable(shapeModel);
            drawable.setTint(colorSurface);
            drawable.setShadowCompatibilityMode(MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS);
            drawable.setElevation(8f);
            bottomSheet.setBackground(drawable);

        }
    }
}
