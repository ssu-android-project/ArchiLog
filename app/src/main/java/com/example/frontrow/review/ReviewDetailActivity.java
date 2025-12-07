package com.example.frontrow.review;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontrow.R;

import java.util.ArrayList;
import java.util.List;

public class ReviewDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE   = "extra_title";
    public static final String EXTRA_PLACE   = "extra_place";
    public static final String EXTRA_DATE    = "extra_date";
    public static final String EXTRA_RATING  = "extra_rating";
    public static final String EXTRA_TAG1    = "extra_tag1";
    public static final String EXTRA_TAG2    = "extra_tag2";
    public static final String EXTRA_BODY    = "extra_body";
    public static final String EXTRA_IMAGE_RES_ID  = "extra_image_res_id";
    public static final String EXTRA_IMAGE_RES_IDS = "extra_image_res_ids";

    private ImageView imgMain;
    private TextView tvTitle, tvLocationDate, tvRating, tvTag1, tvTag2, tvBody;
    private RecyclerView rvDetailPhotos;

    private final List<Integer> photoResIds = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_detail);

        TextView btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> finish());

        imgMain        = findViewById(R.id.imgMain);
        tvTitle        = findViewById(R.id.tvTitle);
        tvLocationDate = findViewById(R.id.tvLocationDate);
        tvRating       = findViewById(R.id.tvRating);
        tvTag1         = findViewById(R.id.tvTag1);
        tvTag2         = findViewById(R.id.tvTag2);
        tvBody         = findViewById(R.id.tvBody);
        rvDetailPhotos = findViewById(R.id.rvDetailPhotos);

        rvDetailPhotos.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        bindFromIntent(getIntent());
    }

    private void bindFromIntent(Intent intent) {
        if (intent == null) return;

        String title  = intent.getStringExtra(EXTRA_TITLE);
        String place  = intent.getStringExtra(EXTRA_PLACE);
        String date   = intent.getStringExtra(EXTRA_DATE);
        String rating = intent.getStringExtra(EXTRA_RATING);
        String tag1   = intent.getStringExtra(EXTRA_TAG1);
        String tag2   = intent.getStringExtra(EXTRA_TAG2);
        String body   = intent.getStringExtra(EXTRA_BODY);
        int mainImage = intent.getIntExtra(EXTRA_IMAGE_RES_ID, R.drawable.img_ecc);

        int[] imageArray = intent.getIntArrayExtra(EXTRA_IMAGE_RES_IDS);
        photoResIds.clear();
        if (imageArray != null && imageArray.length > 0) {
            for (int resId : imageArray) {
                photoResIds.add(resId);
            }
        } else {
            photoResIds.add(mainImage);
        }

        imgMain.setImageResource(photoResIds.get(0));

        DetailPhotoAdapter adapter = new DetailPhotoAdapter(photoResIds, resId -> {
            imgMain.setImageResource(resId);
        });
        rvDetailPhotos.setAdapter(adapter);

        if (title != null)  tvTitle.setText(title);

        if (place != null || date != null) {
            String locDate = "";
            if (place != null && !place.isEmpty()) locDate += place;
            if (date != null && !date.isEmpty()) {
                if (!locDate.isEmpty()) locDate += " · ";
                locDate += date;
            }
            tvLocationDate.setText(locDate);
        }

        if (rating != null && !rating.isEmpty()) {
            if (!rating.startsWith("⭐")) rating = "⭐ " + rating;
            tvRating.setText(rating);
        }

        if (tag1 != null && !tag1.isEmpty()) tvTag1.setText(tag1);
        if (tag2 != null && !tag2.isEmpty()) tvTag2.setText(tag2);
        if (body != null) tvBody.setText(body);
    }
}