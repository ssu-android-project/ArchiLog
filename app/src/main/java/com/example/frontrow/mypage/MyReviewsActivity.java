package com.example.frontrow.mypage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.frontrow.R;
import com.example.frontrow.review.CardCarouselTransformer;
import com.example.frontrow.review.ReviewDetailActivity;
import com.example.frontrow.review.ReviewItem;
import com.example.frontrow.review.ReviewPagerAdapter;
import com.example.frontrow.review.ReviewRepository;
import com.example.frontrow.review.ReviewWriteActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MyReviewsActivity extends AppCompatActivity {

    private ViewPager2 vpMyReviews;
    private ReviewPagerAdapter adapter;
    private TextView tvEmpty;

    private final List<ReviewItem> myReviews = new ArrayList<>();
    private String scrollTargetReviewId = null;

    private final ActivityResultLauncher<Intent> reviewDetailLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            scrollTargetReviewId = result.getData().getStringExtra(ReviewDetailActivity.RESULT_REVIEW_ID);
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> reviewWriteLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            scrollTargetReviewId = result.getData().getStringExtra(ReviewWriteActivity.RESULT_REVIEW_ID);
                        }
                    }
            );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        vpMyReviews = findViewById(R.id.vpMyReviews);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new ReviewPagerAdapter(myReviews, item -> {
            Intent intent = new Intent(this, ReviewDetailActivity.class);
            intent.putExtra(ReviewDetailActivity.EXTRA_REVIEW_KEY, item.reviewId);
            intent.putExtra(ReviewDetailActivity.EXTRA_AUTHOR_UID, item.authorUid);
            intent.putExtra(ReviewDetailActivity.EXTRA_TITLE, item.title);
            intent.putExtra(ReviewDetailActivity.EXTRA_PLACE, item.place);
            intent.putExtra(ReviewDetailActivity.EXTRA_DATE, item.date);
            intent.putExtra(ReviewDetailActivity.EXTRA_RATING, item.rating);
            intent.putExtra(ReviewDetailActivity.EXTRA_TAG1, item.tag1);
            intent.putExtra(ReviewDetailActivity.EXTRA_TAG2, item.tag2);
            intent.putExtra(ReviewDetailActivity.EXTRA_BODY, item.body);
            intent.putExtra(ReviewDetailActivity.EXTRA_MAIN_PHOTO_URL, item.mainPhotoUrl);

            if (item.photoUrls != null) {
                intent.putStringArrayListExtra(
                        ReviewDetailActivity.EXTRA_PHOTO_URLS,
                        new ArrayList<>(item.photoUrls)
                );
            }

            reviewDetailLauncher.launch(intent);
        });

        vpMyReviews.setAdapter(adapter);
        vpMyReviews.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        vpMyReviews.setOffscreenPageLimit(3);

        RecyclerView recyclerView = (RecyclerView) vpMyReviews.getChildAt(0);
        if (recyclerView != null) recyclerView.setClipToPadding(false);

        int pageOffsetPx = (int) (40 * getResources().getDisplayMetrics().density);
        vpMyReviews.setPageTransformer(new CardCarouselTransformer(pageOffsetPx));

        loadMyReviewsAndScroll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyReviewsAndScroll();
    }

    private void loadMyReviewsAndScroll() {
        String myUid = FirebaseAuth.getInstance().getUid();
        if (myUid == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("로그인이 필요합니다.");
            vpMyReviews.setVisibility(View.GONE);
            scrollTargetReviewId = null;
            return;
        }

        // 로딩 중 표시(흰 화면 방지)
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText("불러오는 중...");
        vpMyReviews.setVisibility(View.GONE);

        ReviewRepository.getReviewsByAuthor(myUid, new ReviewRepository.Callback<List<ReviewItem>>() {
            @Override
            public void onSuccess(List<ReviewItem> list) {
                Log.d("MyReviews", "onSuccess count=" + (list == null ? 0 : list.size()));

                myReviews.clear();
                if (list != null) myReviews.addAll(list);
                adapter.notifyDataSetChanged();

                if (myReviews.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("내가 작성한 후기가 없습니다.");
                    vpMyReviews.setVisibility(View.GONE);
                    scrollTargetReviewId = null;
                    return;
                }

                tvEmpty.setVisibility(View.GONE);
                vpMyReviews.setVisibility(View.VISIBLE);

                // 목표 리뷰 위치 찾기
                int targetPosition = -1;
                if (scrollTargetReviewId != null) {
                    for (int i = 0; i < myReviews.size(); i++) {
                        if (scrollTargetReviewId.equals(myReviews.get(i).reviewId)) {
                            targetPosition = i;
                            break;
                        }
                    }
                }
                scrollTargetReviewId = null;

                // 무한캐러셀 시작 위치
                int middle = Integer.MAX_VALUE / 2;
                int start = middle - (middle % myReviews.size());

                vpMyReviews.setCurrentItem(
                        targetPosition != -1 ? (start + targetPosition) : start,
                        false
                );
            }

            @Override
            public void onError(Exception e) {
                Log.e("MyReviews", "onError", e);

                scrollTargetReviewId = null;
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("후기를 불러오는 데 실패했습니다.\n" + e.getMessage());
                vpMyReviews.setVisibility(View.GONE);

                Toast.makeText(MyReviewsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}