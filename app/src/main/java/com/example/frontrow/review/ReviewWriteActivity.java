package com.example.frontrow.review;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontrow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReviewWriteActivity extends AppCompatActivity {

    public static final String EXTRA_PRESET_PLACE = "extra_preset_place";

    public static final String EXTRA_MODE = "extra_mode";
    public static final String MODE_EDIT = "edit";

    public static final String EXTRA_EDIT_REVIEW_ID = "extra_edit_review_id";
    public static final String EXTRA_EDIT_TITLE  = "extra_edit_title";
    public static final String EXTRA_EDIT_PLACE  = "extra_edit_place";
    public static final String EXTRA_EDIT_DATE   = "extra_edit_date";
    public static final String EXTRA_EDIT_TAG1   = "extra_edit_tag1";
    public static final String EXTRA_EDIT_TAG2   = "extra_edit_tag2";
    public static final String EXTRA_EDIT_BODY   = "extra_edit_body";
    public static final String EXTRA_EDIT_RATING = "extra_edit_rating";
    public static final String EXTRA_EDIT_MAIN_PHOTO_URL = "extra_edit_main_photo_url";
    public static final String EXTRA_EDIT_PHOTO_URLS = "extra_edit_photo_urls";

    public static final String RESULT_REVIEW_ID = "result_review_id";

    private static final int REQ_PICK_IMAGES = 1001;
    private static final int MAX_PHOTOS = 10;

    private LinearLayout layoutPhotoAdd;
    private TextView tvPhotoCount;

    private RecyclerView rvPhotos;
    private PhotoThumbAdapter photoAdapter;
    private final List<Uri> photoUris = new ArrayList<>();

    private EditText etTitle, etPlace, etDate, etTag1, etTag2, etTagMore, etBody;

    private int selectedRating = 0; // Represents half-star count (0-10)
    private View[] starWraps;
    private ImageView[] starFg;

    private boolean isEditMode = false;
    private String editReviewId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_write);

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

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
        findViewById(R.id.btnSubmit).setOnClickListener(v -> submitReview());

        initStars();
        initStarClicks();
        setRating(0);

        rvPhotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        photoAdapter = new PhotoThumbAdapter(photoUris);
        rvPhotos.setAdapter(photoAdapter);
        rvPhotos.setVisibility(View.GONE);
        updatePhotoCount();

        layoutPhotoAdd.setOnClickListener(v -> openGalleryForImages());
        etDate.setOnClickListener(v -> showDatePicker());

        String presetPlace = getIntent().getStringExtra(EXTRA_PRESET_PLACE);
        if (presetPlace != null && !presetPlace.trim().isEmpty()) {
            etPlace.setText(presetPlace.trim());
            etPlace.setSelection(etPlace.getText().length());
        }

        isEditMode = MODE_EDIT.equals(getIntent().getStringExtra(EXTRA_MODE));
        if (isEditMode) {
            editReviewId = getIntent().getStringExtra(EXTRA_EDIT_REVIEW_ID);
            etTitle.setText(safe(getIntent().getStringExtra(EXTRA_EDIT_TITLE)));
            etPlace.setText(safe(getIntent().getStringExtra(EXTRA_EDIT_PLACE)));
            etDate.setText(safe(getIntent().getStringExtra(EXTRA_EDIT_DATE)));
            etTag1.setText(safe(getIntent().getStringExtra(EXTRA_EDIT_TAG1)));
            etTag2.setText(safe(getIntent().getStringExtra(EXTRA_EDIT_TAG2)));
            etBody.setText(safe(getIntent().getStringExtra(EXTRA_EDIT_BODY)));
            setRating(parseRatingToHalfStars(getIntent().getStringExtra(EXTRA_EDIT_RATING)));
            ((TextView) findViewById(R.id.tvTopTitle)).setText("후기 수정하기");
            ((Button) findViewById(R.id.btnSubmit)).setText("후기 수정");
        }
    }

    private void submitReview() {
        String title = etTitle.getText().toString().trim();
        String place = etPlace.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String tag1 = etTag1.getText().toString().trim();
        String tag2 = etTag2.getText().toString().trim();
        String body = etBody.getText().toString().trim();

        if (title.isEmpty() || place.isEmpty() || date.isEmpty() || selectedRating == 0 || tag1.isEmpty() || tag2.isEmpty() || body.isEmpty()) {
            toast("모든 필드를 입력해주세요.");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { toast("로그인이 필요합니다."); return; }

        ReviewItem item = new ReviewItem();
        item.title = title;
        item.place = place;
        item.date = date;
        item.rating = String.valueOf(selectedRating / 2.0);
        item.tag1 = tag1;
        item.tag2 = tag2;
        item.body = body;
        item.authorUid = user.getUid();

        if (!photoUris.isEmpty()) {
            uploadAllImagesAndContinue(user.getUid(), photoUris, urls -> {
                item.photoUrls = urls;
                if (!urls.isEmpty()) {
                    item.mainPhotoUrl = urls.get(0);
                }
                saveReviewItem(item);
            });
        } else {
            saveReviewItem(item);
        }
    }

    private interface UrlsCallback { void onDone(List<String> urls); }

    private void uploadAllImagesAndContinue(String uid, List<Uri> uris, UrlsCallback cb) {
        if (uris == null || uris.isEmpty()) {
            cb.onDone(new ArrayList<>());
            return;
        }

        final List<String> uploadedUrls = new ArrayList<>();
        final int totalImages = uris.size();
        final int[] uploadedCount = {0};

        for (Uri uri : uris) {
            String fileName = UUID.randomUUID().toString() + ".jpg";
            StorageReference ref = FirebaseStorage.getInstance().getReference().child("review_images").child(uid).child(fileName);

            ref.putFile(uri)
                    .addOnSuccessListener(task -> ref.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                uploadedUrls.add(downloadUri.toString());
                                uploadedCount[0]++;
                                if (uploadedCount[0] == totalImages) {
                                    cb.onDone(uploadedUrls);
                                }
                            })
                            .addOnFailureListener(e -> {
                                toast("다운로드 URL 실패: " + e.getMessage());
                                uploadedCount[0]++;
                                if (uploadedCount[0] == totalImages) {
                                    cb.onDone(uploadedUrls);
                                }
                            })
                    )
                    .addOnFailureListener(e -> {
                        toast("사진 업로드 실패: " + e.getMessage());
                        uploadedCount[0]++;
                        if (uploadedCount[0] == totalImages) {
                            cb.onDone(uploadedUrls);
                        }
                    });
        }
    }

    private void saveReviewItem(ReviewItem item) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (isEditMode) {
            db.collection("reviews").document(editReviewId).update(item.toMap())
                    .addOnSuccessListener(v -> {
                        toast("수정되었습니다.");
                        Intent result = new Intent();
                        result.putExtra(RESULT_REVIEW_ID, editReviewId);
                        setResult(RESULT_OK, result);
                        finish();
                    })
                    .addOnFailureListener(e -> toast("수정 실패: " + e.getMessage()));
        } else {
            Map<String, Object> data = new HashMap<>(item.toMap());
            data.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
            db.collection("reviews").add(data)
                    .addOnSuccessListener(docRef -> {
                        toast("후기가 등록되었습니다.");
                        Intent result = new Intent();
                        result.putExtra(RESULT_REVIEW_ID, docRef.getId());
                        setResult(RESULT_OK, result);
                        finish();
                    })
                    .addOnFailureListener(e -> toast("저장 실패: " + e.getMessage()));
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
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    if (photoUris.size() >= MAX_PHOTOS) break;
                    photoUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                if (photoUris.size() < MAX_PHOTOS) {
                    photoUris.add(data.getData());
                }
            }
            photoAdapter.notifyDataSetChanged();
            updatePhotoCount();
        }
    }

    private void updatePhotoCount() {
        tvPhotoCount.setText(photoUris.size() + "/" + MAX_PHOTOS);
        rvPhotos.setVisibility(photoUris.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showDatePicker() {
        final Calendar cal = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, y, m, d) -> etDate.setText(String.format("%d.%02d.%02d 방문", y, m + 1, d)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void initStars() {
        starWraps = new View[] {
                findViewById(R.id.starWrap1),
                findViewById(R.id.starWrap2),
                findViewById(R.id.starWrap3),
                findViewById(R.id.starWrap4),
                findViewById(R.id.starWrap5)
        };
        starFg = new ImageView[] {
                findViewById(R.id.starFg1),
                findViewById(R.id.starFg2),
                findViewById(R.id.starFg3),
                findViewById(R.id.starFg4),
                findViewById(R.id.starFg5)
        };
        for (ImageView v : starFg) {
            v.setColorFilter(getResources().getColor(R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN);
            v.setVisibility(View.GONE);
        }
    }

    private void initStarClicks() {
        for (int i = 0; i < starWraps.length; i++) {
            final int idx = i;
            starWraps[i].setOnClickListener(v -> {
                int half = 2 * (idx + 1) - 1;
                int full = 2 * (idx + 1);
                if (selectedRating == half) setRating(full);
                else if (selectedRating == full) setRating(half);
                else setRating(half);
            });
        }
    }

    private void setRating(int halfStarValue) {
        selectedRating = Math.max(0, Math.min(10, halfStarValue));
        int fullStars = selectedRating / 2;
        boolean hasHalf = (selectedRating % 2) == 1;
        for (int i = 0; i < starFg.length; i++) {
            starFg[i].setVisibility(View.GONE);
            starFg[i].setClipBounds(null);
            if (i < fullStars) {
                starFg[i].setVisibility(View.VISIBLE);
            } else if (i == fullStars && hasHalf) {
                starFg[i].setVisibility(View.VISIBLE);
                final int finalI = i;
                starFg[finalI].post(() -> {
                    int w = starFg[finalI].getWidth();
                    int h = starFg[finalI].getHeight();
                    if (w > 0 && h > 0) {
                        starFg[finalI].setClipBounds(new android.graphics.Rect(0, 0, w / 2, h));
                    }
                });
            }
        }
    }

    private int parseRatingToHalfStars(String s) {
        if (s == null) return 0;
        s = s.replace("⭐", "").trim();
        try {
            return (int) Math.round(Double.parseDouble(s) * 2.0);
        } catch (Exception e) {
            return 0;
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
