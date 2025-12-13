package com.example.frontrow.mypage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.frontrow.R;
import com.example.frontrow.login.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyPageFragment extends Fragment {

    private ImageView imgAvatar;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    this::onGalleryResult
            );

    public MyPageFragment() { }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_my_page, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        imgAvatar = view.findViewById(R.id.imgAvatar);

        ImageView btnEditAvatar = view.findViewById(R.id.btnEditAvatar);
        TextView tvUserName = view.findViewById(R.id.tvUserName);
        TextView tvUserId   = view.findViewById(R.id.tvUserId);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String name  = user.getDisplayName();

            if (name == null || name.isEmpty()) name = "익명 사용자";
            tvUserName.setText(name);

            if (email == null) email = "";
            tvUserId.setText(email);
        } else {
            tvUserName.setText("로그인이 필요합니다");
            tvUserId.setText("");
        }

        btnEditAvatar.setOnClickListener(v -> openGallery());

        LinearLayout rowMyCard = view.findViewById(R.id.rowMyCard);
        rowMyCard.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), MyReviewsActivity.class));
        });

        LinearLayout rowCalendar = view.findViewById(R.id.rowCalendar);
        rowCalendar.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), MyCalendarActivity.class));
        });

        LinearLayout rowBookmarked = view.findViewById(R.id.rowBookmarked);
        rowBookmarked.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), BookmarkedPlacesActivity.class));
        });

        LinearLayout rowLikedReviews = view.findViewById(R.id.rowLikedReviews);
        rowLikedReviews.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), LikedReviewsActivity.class));
        });

        LinearLayout rowLogout = view.findViewById(R.id.rowLogout);
        rowLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void onGalleryResult(ActivityResult result) {
        if (result.getResultCode() != Activity.RESULT_OK) return;

        Intent data = result.getData();
        if (data == null) return;

        Uri imageUri = data.getData();
        if (imageUri == null) return;

        if (imgAvatar != null) {
            imgAvatar.setImageURI(imageUri);
        }
    }

    private void showLogoutDialog() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("로그아웃")
                .setMessage("정말로 로그아웃 하시겠습니까?")
                .setPositiveButton("로그아웃", (d, which) -> {

                    FirebaseAuth.getInstance().signOut();

                    GoogleSignIn.getClient(
                            requireContext(),
                            new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                    ).signOut();

                    SharedPreferences prefs = requireContext()
                            .getSharedPreferences("frontrow_prefs", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("auto_login_enabled", false)
                            .apply();

                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    requireActivity().finish();
                })
                .setNegativeButton("취소", null)
                .create();

        dialog.show();

        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        int black = ContextCompat.getColor(requireContext(), R.color.black);
        positive.setTextColor(black);
        negative.setTextColor(black);
    }
}
