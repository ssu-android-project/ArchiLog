package com.example.frontrow.map;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.frontrow.R;

public class PlaceDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            // 이전 화면(맵 프래그먼트 있는 액티비티)으로 돌아감
            finish();
        });

        // MapFragment 에서 넘겨준 place_index 사용
        int index = getIntent().getIntExtra("place_index", -1);
        if (index < 0 || index >= PlaceRepository.PLACES.length) {
            finish();
            return;
        }

        Place p = PlaceRepository.PLACES[index];

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
    }
}