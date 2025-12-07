package com.example.frontrow.review;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;              // ← 이미 있음
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;      // ★ 추가

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontrow.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReviewWriteActivity extends AppCompatActivity {

    // ReviewFragment로 돌려줄 때 사용할 키들
    public static final String RESULT_TITLE  = "result_title";
    public static final String RESULT_PLACE  = "result_place";
    public static final String RESULT_DATE   = "result_date";
    public static final String RESULT_TAG1   = "result_tag1";
    public static final String RESULT_TAG2   = "result_tag2";
    public static final String RESULT_BODY   = "result_body";
    public static final String RESULT_RATING = "result_rating";
    private static final int REQ_PICK_IMAGES = 1001;
    private static final int MAX_PHOTOS = 10;

    private LinearLayout layoutPhotoAdd;
    private TextView tvPhotoCount;

    private RecyclerView rvPhotos;
    private PhotoThumbAdapter photoAdapter;
    private final List<Uri> photoUris = new ArrayList<>();

    private EditText etTitle, etPlace, etDate, etTag1, etTag2, etTagMore, etBody;

    // ★ 별점 관련
    private ImageView star1, star2, star3, star4, star5;
    private int selectedRating = 0;   // 0~5

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_write);

        // ===== View 찾기 =====
        layoutPhotoAdd = findViewById(R.id.layoutPhotoAdd);
        tvPhotoCount   = findViewById(R.id.tvPhotoCount);
        rvPhotos       = findViewById(R.id.rvPhotos);

        etTitle   = findViewById(R.id.etTitle);
        etPlace   = findViewById(R.id.etPlace);
        etDate    = findViewById(R.id.etDate);
        etTag1    = findViewById(R.id.etTag1);
        etTag2    = findViewById(R.id.etTag2);
        etTagMore = findViewById(R.id.etTagMore);
        etBody    = findViewById(R.id.etBody);

        Button btnSubmit = findViewById(R.id.btnSubmit);
        TextView btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> finish());

        // ★ 별 이미지 뷰 찾기
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);

        initStarClicks();   // ↓ 이 함수 호출
        setRating(0);       // 초기 상태: 전부 비활성

        // ===== 사진 RecyclerView 초기화 =====
        rvPhotos.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        photoAdapter = new PhotoThumbAdapter(photoUris);
        rvPhotos.setAdapter(photoAdapter);

        // 처음엔 아무 사진도 없으니 숨겨두기
        rvPhotos.setVisibility(View.GONE);
        updatePhotoCount();

        // ===== 사진 추가 버튼 클릭 =====
        layoutPhotoAdd.setOnClickListener(v -> openGalleryForImages());

        // ===== 날짜 선택 =====
        etDate.setOnClickListener(v -> showDatePicker());

        // ===== 후기 등록 버튼 =====
        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void initStarClicks() {
        ImageView[] stars = {star1, star2, star3, star4, star5};

        for (int i = 0; i < stars.length; i++) {
            final int ratingValue = i + 1; // 1 ~ 5

            stars[i].setOnClickListener(v -> {
                // 같은 별을 다시 누르면 0점으로 초기화 (토글 느낌)
                if (selectedRating == ratingValue) {
                    setRating(0);
                } else {
                    setRating(ratingValue);
                }
            });
        }
    }

    private void setRating(int value) {
        selectedRating = value;

        ImageView[] stars = {star1, star2, star3, star4, star5};

        int activeColor   = androidx.core.content.ContextCompat.getColor(this, R.color.yellow); // 채워진 별 색
        int inactiveColor = androidx.core.content.ContextCompat.getColor(this, android.R.color.darker_gray); // 비활성 별 색

        for (int i = 0; i < stars.length; i++) {
            ImageView star = stars[i];

            if (i < value) {
                // 선택된 개수까지 채워진 별
                star.setColorFilter(activeColor, android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                // 나머지는 회색
                star.setColorFilter(inactiveColor, android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }
    }

    private void openGalleryForImages() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "사진 선택"), REQ_PICK_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PICK_IMAGES && resultCode == RESULT_OK && data != null) {

            ClipData clipData = data.getClipData();

            if (clipData != null) {
                // 여러 장 선택
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    if (photoUris.size() >= MAX_PHOTOS) {
                        Toast.makeText(this, "최대 " + MAX_PHOTOS + "장까지 선택할 수 있어요.", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    Uri uri = clipData.getItemAt(i).getUri();
                    if (!photoUris.contains(uri)) {
                        photoUris.add(uri);
                    }
                }
            } else {
                // 한 장만 선택
                Uri uri = data.getData();
                if (uri != null) {
                    if (photoUris.size() >= MAX_PHOTOS && !photoUris.contains(uri)) {
                        Toast.makeText(this, "최대 " + MAX_PHOTOS + "장까지 선택할 수 있어요.", Toast.LENGTH_SHORT).show();
                    } else if (!photoUris.contains(uri)) {
                        photoUris.add(uri);
                    }
                }
            }

            photoAdapter.notifyDataSetChanged();
            updatePhotoCount();
        }
    }

    private void updatePhotoCount() {
        tvPhotoCount.setText(photoUris.size() + "/" + MAX_PHOTOS);

        // 개수에 따라 RecyclerView 보이기/숨기기
        if (photoUris.isEmpty()) {
            rvPhotos.setVisibility(View.GONE);
        } else {
            rvPhotos.setVisibility(View.VISIBLE);
        }
    }

    private void showDatePicker() {
        final Calendar cal = Calendar.getInstance();
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day   = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    String text = String.format("%d.%02d.%02d 방문", y, m + 1, d);
                    etDate.setText(text);
                },
                year, month, day
        );
        dialog.show();
    }

    private void submitReview() {
        String title  = etTitle.getText().toString().trim();
        String place  = etPlace.getText().toString().trim();
        String date   = etDate.getText().toString().trim();
        String tag1   = etTag1.getText().toString().trim();
        String tag2   = etTag2.getText().toString().trim();
        String body   = etBody.getText().toString().trim();

        // 간단 검증 (필수값)
        if (title.isEmpty()) {
            Toast.makeText(this, "제목을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (place.isEmpty()) {
            Toast.makeText(this, "방문한 건축물을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (date.isEmpty()) {
            Toast.makeText(this, "방문일자를 선택해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedRating == 0) {
            Toast.makeText(this, "별점을 선택해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tag1.isEmpty() || tag2.isEmpty()) {
            Toast.makeText(this, "감성 태그를 최소 2개 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (body.isEmpty()) {
            Toast.makeText(this, "자세한 후기를 적어 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ReviewFragment로 결과 전달
        Intent result = new Intent();
        result.putExtra(RESULT_TITLE,  title);
        result.putExtra(RESULT_PLACE,  place);
        result.putExtra(RESULT_DATE,   date);
        result.putExtra(RESULT_TAG1,   tag1);
        result.putExtra(RESULT_TAG2,   tag2);
        result.putExtra(RESULT_BODY,   body);
        result.putExtra(RESULT_RATING, selectedRating);   // 1~5 정수 형태

        setResult(RESULT_OK, result);

        Toast.makeText(this, "후기가 저장되었습니다.", Toast.LENGTH_SHORT).show();
        finish();
    }
}