package com.example.frontrow.review;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.frontrow.R;

import java.util.ArrayList;
import java.util.List;

public class ReviewFragment extends Fragment {

    private ViewPager2 vpReview;
    private ReviewPagerAdapter adapter;

    private final List<ReviewItem> reviews = new ArrayList<>();
    private String scrollTargetReviewId = null;

    private final ActivityResultLauncher<Intent> reviewWriteLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            scrollTargetReviewId = result.getData().getStringExtra(ReviewWriteActivity.RESULT_REVIEW_ID);
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> reviewDetailLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            scrollTargetReviewId = result.getData().getStringExtra(ReviewDetailActivity.RESULT_REVIEW_ID);
                        }
                    }
            );

    public ReviewFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vpReview = view.findViewById(R.id.vpReview);

        adapter = new ReviewPagerAdapter(reviews, item -> {
            Intent intent = new Intent(requireContext(), ReviewDetailActivity.class);
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
                intent.putStringArrayListExtra(ReviewDetailActivity.EXTRA_PHOTO_URLS, new ArrayList<>(item.photoUrls));
            }
            reviewDetailLauncher.launch(intent);
        });

        vpReview.setAdapter(adapter);
        vpReview.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        vpReview.setOffscreenPageLimit(3);

        RecyclerView recyclerView = (RecyclerView) vpReview.getChildAt(0);
        if (recyclerView != null) recyclerView.setClipToPadding(false);

        int pageOffsetPx = (int) (40 * getResources().getDisplayMetrics().density);
        vpReview.setPageTransformer(new CardCarouselTransformer(pageOffsetPx));

        ImageView btnAddReview = view.findViewById(R.id.btnAddReview);
        btnAddReview.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ReviewWriteActivity.class);
            reviewWriteLauncher.launch(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReviewsAndScroll();
    }

    private void loadReviewsAndScroll() {
        ReviewRepository.getAllReviews(new ReviewRepository.Callback<List<ReviewItem>>() {
            @Override
            public void onSuccess(List<ReviewItem> list) {
                reviews.clear();
                if (list != null) reviews.addAll(list);
                if (adapter != null) adapter.notifyDataSetChanged();

                if (vpReview == null || reviews.isEmpty()) {
                    scrollTargetReviewId = null;
                    return;
                }

                int targetPosition = -1;
                if (scrollTargetReviewId != null) {
                    for (int i = 0; i < reviews.size(); i++) {
                        if (scrollTargetReviewId.equals(reviews.get(i).reviewId)) {
                            targetPosition = i;
                            break;
                        }
                    }
                }

                scrollTargetReviewId = null;

                int middle = Integer.MAX_VALUE / 2;
                int start = middle - (middle % reviews.size());

                if (targetPosition != -1) {
                    vpReview.setCurrentItem(start + targetPosition, false);
                } else {
                    vpReview.setCurrentItem(start, false);
                }
            }

            @Override
            public void onError(Exception e) {
                scrollTargetReviewId = null;
                if (getContext() != null) {
                    Toast.makeText(getContext(), "리뷰 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
