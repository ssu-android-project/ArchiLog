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

import com.example.frontrow.R;
import com.example.frontrow.login.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyPageFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ImageView imgAvatar;

    // 갤러리에서 이미지 선택 결과 받는 런처
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    this::onGalleryResult
            );

    public MyPageFragment() { }

    public static MyPageFragment newInstance(String param1, String param2) {
        MyPageFragment fragment = new MyPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

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

        ImageView btnEditAvatar = view.findViewById(R.id.btnEditAvatar);
        TextView tvUserName = view.findViewById(R.id.tvUserName);
        TextView tvUserId   = view.findViewById(R.id.tvUserId);

        // Firebase 유저 정보 표시
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String name  = user.getDisplayName();

            // 이메일/이름이 null일 수 있으니 방어 코드
            if (name == null || name.isEmpty()) {
                name = "익명 사용자";
            }
            tvUserName.setText(name);

            if (email == null) email = "";
            tvUserId.setText(email);
        }

        // 아바타 / 수정 아이콘 클릭 시 갤러리 열기
        View.OnClickListener openGalleryListener = v2 -> openGallery();
        btnEditAvatar.setOnClickListener(openGalleryListener);

        // 로그아웃 메뉴
        LinearLayout rowLogout = view.findViewById(R.id.rowLogout);
        rowLogout.setOnClickListener(v -> showLogoutDialog());
    }

    // 갤러리 열기 & 결과 처리
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

        // 여기서 실제로 ImageView에 반영
        imgAvatar.setImageURI(imageUri);

        // (선택) 나중에 다시 앱 켰을 때도 유지하려면 SharedPreferences에 Uri를 문자열로 저장해두고,
        // onViewCreated에서 불러와서 setImageURI 해주면 됨.
    }

    // 로그아웃 다이얼로그
    private void showLogoutDialog() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.LogoutDialogTheme)
                .setTitle("로그아웃")
                .setMessage("정말로 로그아웃 하시겠습니까?")
                .setPositiveButton("예", (d, which) -> {
                    // Firebase 로그아웃
                    FirebaseAuth.getInstance().signOut();

                    // 구글 로그아웃
                    GoogleSignIn.getClient(
                            requireContext(),
                            new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                    ).signOut();

                    // 자동 로그인 플래그 정리
                    SharedPreferences prefs = requireContext()
                            .getSharedPreferences("frontrow_prefs", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("auto_login_enabled", false)
                            .apply();

                    // 로그인 화면으로 이동 + 백스택 제거
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    requireActivity().finish();
                })
                .setNegativeButton("아니오", (d, which) -> d.dismiss())
                .create();

        dialog.show();

        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        int black = ContextCompat.getColor(requireContext(), R.color.black);
        positive.setTextColor(black);
        negative.setTextColor(black);
    }
}