package com.example.frontrow.mypage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontrow.R;
import com.example.frontrow.map.Place;
import com.example.frontrow.map.PlaceDetailActivity;
import com.example.frontrow.map.PlaceRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BookmarkedPlacesActivity extends AppCompatActivity {

    private static final int REQUEST_PLACE_DETAIL = 1001;

    private RecyclerView rvBookmarked;
    private TextView tvEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarked_places);

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvBookmarked = findViewById(R.id.rvBookmarked);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvBookmarked.setLayoutManager(new LinearLayoutManager(this));

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            tvEmpty.setText("로그인이 필요합니다.");
            tvEmpty.setVisibility(TextView.VISIBLE);
            rvBookmarked.setAdapter(null);
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadBookmarkedPlaces(uid);
    }

    private void loadBookmarkedPlaces(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        tvEmpty.setText("불러오는 중...");
        tvEmpty.setVisibility(TextView.VISIBLE);
        rvBookmarked.setAdapter(null);

        db.collection("users")
                .document(uid)
                .collection("bookmarks")
                .get()
                .addOnSuccessListener(qs -> {
                    List<BookmarkedPlaceItem> bookmarked = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : qs) {
                        int idx;
                        try {
                            idx = Integer.parseInt(doc.getId());
                        } catch (Exception e) {
                            continue;
                        }

                        Place p = PlaceRepository.getPlace(idx);
                        if (p == null) continue;

                        bookmarked.add(new BookmarkedPlaceItem(
                                idx,
                                p.name,
                                p.architect,
                                p.imageResId
                        ));
                    }

                    Collections.sort(bookmarked, Comparator.comparingInt(a -> a.placeIndex));

                    if (bookmarked.isEmpty()) {
                        tvEmpty.setText("북마크한 건축물이 없어요.");
                        tvEmpty.setVisibility(TextView.VISIBLE);
                        rvBookmarked.setAdapter(null);
                        return;
                    }

                    tvEmpty.setVisibility(TextView.GONE);

                    BookmarkedPlaceAdapter adapter = new BookmarkedPlaceAdapter(bookmarked, item -> {
                        Intent intent = new Intent(BookmarkedPlacesActivity.this, PlaceDetailActivity.class);
                        intent.putExtra("place_index", item.placeIndex);
                        startActivityForResult(intent, REQUEST_PLACE_DETAIL);
                    });

                    rvBookmarked.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    tvEmpty.setText("불러오기 실패: " + e.getMessage());
                    tvEmpty.setVisibility(TextView.VISIBLE);
                    rvBookmarked.setAdapter(null);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PLACE_DETAIL && resultCode == RESULT_OK) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            loadBookmarkedPlaces(uid);
        }
    }
}