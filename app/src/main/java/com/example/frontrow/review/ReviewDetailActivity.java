package com.example.frontrow.review;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.frontrow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReviewDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_PLACE = "extra_place";
    public static final String EXTRA_DATE = "extra_date";
    public static final String EXTRA_RATING = "extra_rating";
    public static final String EXTRA_TAG1 = "extra_tag1";
    public static final String EXTRA_TAG2 = "extra_tag2";
    public static final String EXTRA_BODY = "extra_body";
    public static final String EXTRA_MAIN_PHOTO_URL = "extra_main_photo_url";
    public static final String EXTRA_PHOTO_URLS = "extra_photo_urls";

    public static final String EXTRA_REVIEW_KEY = "extra_review_key";
    public static final String EXTRA_AUTHOR_UID = "extra_author_uid";

    public static final String RESULT_ACTION = "result_action";
    public static final String ACTION_DELETED = "deleted";
    public static final String ACTION_UPDATED = "updated";
    public static final String RESULT_REVIEW_ID = "result_review_id";

    private ImageView imgMain, btnLike, btnEdit, btnDelete;
    private TextView tvTitle, tvLocationDate, tvRating, tvTag1, tvTag2, tvBody, tvTopTitle;
    private RecyclerView rvDetailPhotos;

    private int LIKE_ON_COLOR, LIKE_OFF_COLOR;

    private String reviewId, authorUid;
    private boolean isLiked = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_detail);

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        imgMain = findViewById(R.id.imgMain);
        tvTopTitle = findViewById(R.id.tvTopTitle);
        tvTitle = findViewById(R.id.tvTitle);
        tvLocationDate = findViewById(R.id.tvLocationDate);
        tvRating = findViewById(R.id.tvRating);
        tvTag1 = findViewById(R.id.tvTag1);
        tvTag2 = findViewById(R.id.tvTag2);
        tvBody = findViewById(R.id.tvBody);
        rvDetailPhotos = findViewById(R.id.rvDetailPhotos);
        btnLike = findViewById(R.id.btnLike);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        rvDetailPhotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        LIKE_ON_COLOR = ContextCompat.getColor(this, R.color.red);
        LIKE_OFF_COLOR = ContextCompat.getColor(this, R.color.black);
        btnLike.setImageResource(R.drawable.ic_like);

        reviewId = getIntent().getStringExtra(EXTRA_REVIEW_KEY);
        authorUid = getIntent().getStringExtra(EXTRA_AUTHOR_UID);

        // 1) 먼저 인텐트로 온 값으로 그려보고
        bindFromIntent(getIntent());

        // 2) 인텐트에 데이터가 거의 없으면(캘린더/딥링크) Firestore에서 로드
        boolean hasEnoughExtras = hasDetailExtras(getIntent());
        if (!hasEnoughExtras && reviewId != null && !reviewId.trim().isEmpty()) {
            loadFromFirestore(reviewId);
        } else {
            // 인텐트로 충분히 왔으면 기존 흐름 유지
            fetchUserNameAndApply(authorUid);
            applyOwnerMenuVisibility();
        }

        // like는 reviewId가 있어야 하니까 여기서
        refreshLikeState();

        btnLike.setOnClickListener(v -> toggleLike());
        btnEdit.setOnClickListener(v -> goEdit());
        btnDelete.setOnClickListener(v -> askDelete());
    }

    private boolean hasDetailExtras(Intent intent) {
        if (intent == null) return false;
        String title = intent.getStringExtra(EXTRA_TITLE);
        String place = intent.getStringExtra(EXTRA_PLACE);
        String date  = intent.getStringExtra(EXTRA_DATE);
        String body  = intent.getStringExtra(EXTRA_BODY);
        // 타이틀/본문/장소/날짜 중 2개 이상 들어오면 "충분"으로 간주
        int cnt = 0;
        if (title != null && !title.trim().isEmpty()) cnt++;
        if (place != null && !place.trim().isEmpty()) cnt++;
        if (date  != null && !date.trim().isEmpty()) cnt++;
        if (body  != null && !body.trim().isEmpty()) cnt++;
        return cnt >= 2;
    }

    private void loadFromFirestore(String id) {
        tvTopTitle.setText("상세 후기");

        FirebaseFirestore.getInstance().collection("reviews").document(id).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "후기를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ✅ 너 Firestore 필드명에 맞춰서 여기만 맞추면 됨
                    String title = doc.getString("title");
                    String place = doc.getString("place");
                    String date = doc.getString("date");
                    String rating = doc.getString("rating");
                    String tag1 = doc.getString("tag1");
                    String tag2 = doc.getString("tag2");
                    String body = doc.getString("body");
                    String mainPhotoUrl = doc.getString("mainPhotoUrl");

                    // authorUid를 Firestore에서 가져오는 케이스도 고려
                    String aUid = doc.getString("authorUid");
                    if (aUid != null && !aUid.trim().isEmpty()) {
                        authorUid = aUid;
                    }

                    // photoUrls(List)
                    List<String> photoUrls = null;
                    Object raw = doc.get("photoUrls");
                    if (raw instanceof List) {
                        //noinspection unchecked
                        photoUrls = (List<String>) raw;
                    }

                    // 화면 적용
                    applyDataToViews(title, place, date, rating, tag1, tag2, body, mainPhotoUrl, photoUrls);

                    fetchUserNameAndApply(authorUid);
                    applyOwnerMenuVisibility();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "후기 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void applyDataToViews(String title, String place, String date, String rating,
                                  String tag1, String tag2, String body,
                                  String mainPhotoUrl, List<String> photoUrls) {

        tvTitle.setText(title == null ? "" : title);
        tvTag1.setText(tag1 == null ? "" : tag1);
        tvTag2.setText(tag2 == null ? "" : tag2);
        tvBody.setText(body == null ? "" : body);

        String locDate = "";
        if (place != null && !place.isEmpty()) locDate += place;
        if (date != null && !date.isEmpty()) {
            if (!locDate.isEmpty()) locDate += " · ";
            locDate += date;
        }
        tvLocationDate.setText(locDate);

        if (rating != null && !rating.isEmpty()) {
            if (!rating.startsWith("⭐")) rating = "⭐ " + rating;
            tvRating.setText(rating);
        } else {
            tvRating.setText("");
        }

        // 사진 처리 (우선순위: photoUrls[0] > mainPhotoUrl)
        ArrayList<String> list = new ArrayList<>();
        if (photoUrls != null) {
            for (String u : photoUrls) {
                if (u != null && !u.trim().isEmpty()) list.add(u);
            }
        }
        if (list.isEmpty() && mainPhotoUrl != null && !mainPhotoUrl.trim().isEmpty()) {
            list.add(mainPhotoUrl);
        }

        if (!list.isEmpty()) {
            imgMain.setVisibility(View.VISIBLE);
            Glide.with(this).load(list.get(0)).placeholder(R.drawable.img_ecc).into(imgMain);

            DetailPhotoAdapter adapter = new DetailPhotoAdapter(list, url -> {
                Glide.with(this).load(url).placeholder(R.drawable.img_ecc).into(imgMain);
            });
            rvDetailPhotos.setAdapter(adapter);
            rvDetailPhotos.setVisibility(View.VISIBLE);
        } else {
            imgMain.setVisibility(View.GONE);
            rvDetailPhotos.setVisibility(View.GONE);
        }
    }

    private void fetchUserNameAndApply(String uid) {
        tvTopTitle.setText("상세 후기");
        if (uid == null || uid.trim().isEmpty()) return;

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.trim().isEmpty()) {
                            tvTopTitle.setText(name + "님의 후기");
                            return;
                        }
                    }
                    tvTopTitle.setText("회원님의 후기");
                })
                .addOnFailureListener(e -> tvTopTitle.setText("회원님의 후기"));
    }

    private void bindFromIntent(Intent intent) {
        if (intent == null) return;

        tvTitle.setText(intent.getStringExtra(EXTRA_TITLE));
        tvTag1.setText(intent.getStringExtra(EXTRA_TAG1));
        tvTag2.setText(intent.getStringExtra(EXTRA_TAG2));
        tvBody.setText(intent.getStringExtra(EXTRA_BODY));

        String place = intent.getStringExtra(EXTRA_PLACE);
        String date = intent.getStringExtra(EXTRA_DATE);
        String locDate = "";
        if (place != null && !place.isEmpty()) locDate += place;
        if (date != null && !date.isEmpty()) {
            if (!locDate.isEmpty()) locDate += " · ";
            locDate += date;
        }
        tvLocationDate.setText(locDate);

        String rating = intent.getStringExtra(EXTRA_RATING);
        if (rating != null && !rating.isEmpty()) {
            if (!rating.startsWith("⭐")) rating = "⭐ " + rating;
            tvRating.setText(rating);
        }

        ArrayList<String> photoUrls = intent.getStringArrayListExtra(EXTRA_PHOTO_URLS);
        if (photoUrls != null && !photoUrls.isEmpty()) {
            imgMain.setVisibility(View.VISIBLE);
            Glide.with(this).load(photoUrls.get(0)).placeholder(R.drawable.img_ecc).into(imgMain);

            DetailPhotoAdapter adapter = new DetailPhotoAdapter(photoUrls, url -> {
                Glide.with(this).load(url).placeholder(R.drawable.img_ecc).into(imgMain);
            });
            rvDetailPhotos.setAdapter(adapter);
            rvDetailPhotos.setVisibility(View.VISIBLE);
        } else {
            // 인텐트만 기준으로는 숨김. (Firestore 로드 시 다시 보여줄 수 있음)
            imgMain.setVisibility(View.GONE);
            rvDetailPhotos.setVisibility(View.GONE);
        }
    }

    private void applyOwnerMenuVisibility() {
        btnEdit.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);
        String myUid = FirebaseAuth.getInstance().getUid();
        if (myUid != null && authorUid != null && myUid.equals(authorUid)) {
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
        }
    }

    private void askDelete() {
        new AlertDialog.Builder(this)
                .setTitle("후기 삭제")
                .setMessage("정말로 삭제 하시겠습니까?")
                .setPositiveButton("삭제", (d, w) -> deleteReview())
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteReview() {
        if (reviewId == null) return;

        FirebaseFirestore.getInstance().collection("reviews").document(reviewId).delete()
                .addOnSuccessListener(v -> {
                    Intent result = new Intent();
                    result.putExtra(RESULT_ACTION, ACTION_DELETED);
                    result.putExtra(RESULT_REVIEW_ID, reviewId);
                    setResult(RESULT_OK, result);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "삭제 실패", Toast.LENGTH_SHORT).show());
    }

    private void refreshLikeState() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || reviewId == null) return;

        FirebaseFirestore.getInstance().collection("users").document(uid)
                .collection("likes").document(reviewId).get()
                .addOnSuccessListener(d -> {
                    isLiked = d.exists();
                    applyLikeUI(isLiked);
                });
    }

    private void toggleLike() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || reviewId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (!isLiked) {
            db.collection("users").document(uid).collection("likes").document(reviewId).set(new HashMap<>());
        } else {
            db.collection("users").document(uid).collection("likes").document(reviewId).delete();
        }
        isLiked = !isLiked;
        applyLikeUI(isLiked);
    }

    private void applyLikeUI(boolean liked) {
        btnLike.setColorFilter(
                liked ? LIKE_ON_COLOR : LIKE_OFF_COLOR,
                android.graphics.PorterDuff.Mode.SRC_IN
        );
    }

    private void goEdit() {
        Intent i = new Intent(this, ReviewWriteActivity.class);
        i.putExtra(ReviewWriteActivity.EXTRA_MODE, ReviewWriteActivity.MODE_EDIT);
        i.putExtra(ReviewWriteActivity.EXTRA_EDIT_REVIEW_ID, reviewId);
        i.putExtra(ReviewWriteActivity.EXTRA_EDIT_TITLE, tvTitle.getText().toString());
        i.putExtra(ReviewWriteActivity.EXTRA_EDIT_BODY, tvBody.getText().toString());
        // 나머지도 필요하면 Firestore 로드 후 값을 넣도록 확장 가능
        startActivity(i);
    }

    @Override
    public void finish() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_REVIEW_ID, reviewId);
        setResult(RESULT_OK, resultIntent);
        super.finish();
    }
}