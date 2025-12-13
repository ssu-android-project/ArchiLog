package com.example.frontrow.mypage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontrow.R;
import com.example.frontrow.review.ReviewDetailActivity;
import com.example.frontrow.review.ReviewItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LikedReviewsActivity extends AppCompatActivity {

    private static final int REQUEST_REVIEW_DETAIL = 1002;

    private RecyclerView rvLiked;
    private TextView tvEmpty;

    private final List<ReviewItem> liked = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_reviews);

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvLiked = findViewById(R.id.rvLiked);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvLiked.setLayoutManager(new LinearLayoutManager(this));

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            tvEmpty.setText("로그인이 필요합니다.");
            tvEmpty.setVisibility(TextView.VISIBLE);
            rvLiked.setAdapter(null);
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadLikedReviews(uid);
    }

    private void loadLikedReviews(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(uid)
                .collection("likes")
                .get()
                .addOnSuccessListener(qs -> {
                    List<String> reviewIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : qs) {
                        reviewIds.add(doc.getId()); // docId == reviewId
                    }

                    if (reviewIds.isEmpty()) {
                        tvEmpty.setText("좋아요 누른 후기가 없어요.");
                        tvEmpty.setVisibility(TextView.VISIBLE);
                        rvLiked.setAdapter(null);
                        return;
                    }

                    fetchReviewsWhereInChunks(reviewIds);
                })
                .addOnFailureListener(e -> {
                    tvEmpty.setText("불러오기 실패: " + e.getMessage());
                    tvEmpty.setVisibility(TextView.VISIBLE);
                    rvLiked.setAdapter(null);
                });
    }

    private void fetchReviewsWhereInChunks(List<String> reviewIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        liked.clear();
        tvEmpty.setText("불러오는 중...");
        tvEmpty.setVisibility(TextView.VISIBLE);

        List<List<String>> chunks = chunk(reviewIds, 10);
        final int totalChunks = chunks.size();
        final int[] doneChunks = {0};

        for (List<String> c : chunks) {
            db.collection("reviews")
                    .whereIn(FieldPath.documentId(), c)
                    .get()
                    .addOnSuccessListener(qs -> {
                        for (int i = 0; i < qs.getDocuments().size(); i++) {
                            ReviewItem r = ReviewItem.fromDoc(qs.getDocuments().get(i));
                            if (r != null) liked.add(r);
                        }
                        doneChunks[0]++;
                        if (doneChunks[0] == totalChunks) showList();
                    })
                    .addOnFailureListener(e -> {
                        doneChunks[0]++;
                        if (doneChunks[0] == totalChunks) showList();
                    });
        }
    }

    private void showList() {
        if (liked.isEmpty()) {
            tvEmpty.setText("좋아요 누른 후기가 없어요.");
            tvEmpty.setVisibility(TextView.VISIBLE);
            rvLiked.setAdapter(null);
            return;
        }

        tvEmpty.setVisibility(TextView.GONE);

        LikedReviewAdapter adapter = new LikedReviewAdapter(liked, item -> {
            Intent intent = new Intent(LikedReviewsActivity.this, ReviewDetailActivity.class);

            intent.putExtra(ReviewDetailActivity.EXTRA_TITLE, item.title);
            intent.putExtra(ReviewDetailActivity.EXTRA_PLACE, item.place);
            intent.putExtra(ReviewDetailActivity.EXTRA_DATE, item.date);
            intent.putExtra(ReviewDetailActivity.EXTRA_RATING, item.rating);
            intent.putExtra(ReviewDetailActivity.EXTRA_TAG1, item.tag1);
            intent.putExtra(ReviewDetailActivity.EXTRA_TAG2, item.tag2);
            intent.putExtra(ReviewDetailActivity.EXTRA_BODY, item.body);

            intent.putExtra(ReviewDetailActivity.EXTRA_MAIN_PHOTO_URL, item.mainPhotoUrl);

            intent.putExtra(ReviewDetailActivity.EXTRA_REVIEW_KEY, item.reviewId);

            startActivityForResult(intent, REQUEST_REVIEW_DETAIL);
        });

        rvLiked.setAdapter(adapter);
    }

    private List<List<String>> chunk(List<String> list, int size) {
        List<List<String>> res = new ArrayList<>();
        if (list == null || list.isEmpty()) return res;

        for (int i = 0; i < list.size(); i += size) {
            int end = Math.min(list.size(), i + size);
            res.add(new ArrayList<>(list.subList(i, end)));
        }
        return res;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_REVIEW_DETAIL && resultCode == RESULT_OK) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            loadLikedReviews(uid);
        }
    }
}