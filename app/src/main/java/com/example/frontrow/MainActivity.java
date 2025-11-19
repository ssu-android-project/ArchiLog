package com.example.frontrow;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.frontrow.map.MapFragment;
import com.example.frontrow.mypage.MyPageFragment;
import com.example.frontrow.review.ReviewFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_MAP      = "MapFragment";
    private static final String TAG_REVIEWS  = "ReviewFragment";
    private static final String TAG_PROFILE  = "ProfileFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // 앱 처음 띄울 때: 지도 탭
        setFragment(TAG_MAP, new MapFragment());
        bottomNav.setSelectedItemId(R.id.nav_explore);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_explore) {
                setFragment(TAG_MAP, new MapFragment());
            } else if (id == R.id.nav_reviews) {
                setFragment(TAG_REVIEWS, new ReviewFragment());
            } else if (id == R.id.nav_profile) {
                setFragment(TAG_PROFILE, new MyPageFragment());
            }
            return true;
        });
    }

    private void setFragment(@NonNull String tag, @NonNull Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();

        // 이미 생성된 프래그먼트들 찾기
        Fragment map      = fm.findFragmentByTag(TAG_MAP);
        Fragment reviews  = fm.findFragmentByTag(TAG_REVIEWS);
        Fragment profile  = fm.findFragmentByTag(TAG_PROFILE);

        // 다 숨기고
        if (map != null)     tx.hide(map);
        if (reviews != null) tx.hide(reviews);
        if (profile != null) tx.hide(profile);

        // 보여줄 프래그먼트 처리
        Fragment target = fm.findFragmentByTag(tag);
        if (target == null) {
            // 처음이면 추가
            tx.add(R.id.fragment_container, fragment, tag);
        } else {
            // 이미 있으면 show만
            tx.show(target);
        }

        tx.commitAllowingStateLoss();
    }
}