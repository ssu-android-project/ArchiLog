package com.example.frontrow.map;

import android.os.Bundle;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.frontrow.R;
import com.example.frontrow.review.ReviewWriteActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PlaceDetailActivity extends AppCompatActivity {

    private ImageView imgBookmark;

    private int ON_COLOR;
    private int OFF_COLOR;

    private int placeIndex = -1;
    private boolean isBookmarked = false;
    private boolean bookmarkChanged = false; // 변경 여부 플래그

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        placeIndex = getIntent().getIntExtra("place_index", -1);
        if (placeIndex < 0 || placeIndex >= PlaceRepository.PLACES.length) {
            finish();
            return;
        }

        Place p = PlaceRepository.PLACES[placeIndex];

        ImageView img = findViewById(R.id.imgDetailBuilding);
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvArchitect = findViewById(R.id.tvDetailArchitect);
        TextView tvDesc = findViewById(R.id.tvDetailDescription);
        TextView tvLoc = findViewById(R.id.tvDetailLocation);
        TextView tvHours = findViewById(R.id.tvDetailHours);
        TextView tvTips = findViewById(R.id.tvDetailTips);
        TextView tvPoints = findViewById(R.id.tvDetailPoints);

        img.setImageResource(p.imageResId);
        tvTitle.setText(p.name);
        tvArchitect.setText(p.architect);
        tvDesc.setText(p.longDesc);
        tvLoc.setText(p.location);
        tvHours.setText(p.hours);
        tvTips.setText(p.tips);
        tvPoints.setText(p.points);

        imgBookmark = findViewById(R.id.imgBookmarkDetail);
        imgBookmark.setImageResource(R.drawable.ic_bookmark);

        ON_COLOR  = ContextCompat.getColor(this, R.color.yellow);
        OFF_COLOR = ContextCompat.getColor(this, R.color.black);

        applyBookmarkUI(false);
        refreshBookmarkState();

        imgBookmark.setOnClickListener(v -> toggleBookmark());

        MaterialButton btnWriteReview = findViewById(R.id.btnWriteReview);
        btnWriteReview.setOnClickListener(v -> {
            Intent intent = new Intent(PlaceDetailActivity.this, ReviewWriteActivity.class);
            intent.putExtra(ReviewWriteActivity.EXTRA_PRESET_PLACE, p.name);
            startActivity(intent);
        });
    }

    private void refreshBookmarkState() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            isBookmarked = false;
            applyBookmarkUI(false);
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(uid)
                .collection("bookmarks")
                .document(String.valueOf(placeIndex))
                .get()
                .addOnSuccessListener(doc -> {
                    isBookmarked = (doc != null && doc.exists());
                    applyBookmarkUI(isBookmarked);
                })
                .addOnFailureListener(e -> {
                    isBookmarked = false;
                    applyBookmarkUI(false);
                });
    }

    private void toggleBookmark() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String docId = String.valueOf(placeIndex);

        if (!isBookmarked) {
            Map<String, Object> data = new HashMap<>();
            data.put("createdAt", FieldValue.serverTimestamp());

            db.collection("users")
                    .document(uid)
                    .collection("bookmarks")
                    .document(docId)
                    .set(data)
                    .addOnSuccessListener(unused -> {
                        isBookmarked = true;
                        applyBookmarkUI(true);
                        bookmarkChanged = true;
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "북마크 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );

        } else {
            db.collection("users")
                    .document(uid)
                    .collection("bookmarks")
                    .document(docId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        isBookmarked = false;
                        applyBookmarkUI(false);
                        bookmarkChanged = true;
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "북마크 해제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void applyBookmarkUI(boolean bookmarked) {
        imgBookmark.setColorFilter(
                bookmarked ? ON_COLOR : OFF_COLOR,
                android.graphics.PorterDuff.Mode.SRC_IN
        );
    }

    @Override
    public void finish() {
        if (bookmarkChanged) {
            setResult(RESULT_OK);
        }
        super.finish();
    }
}
