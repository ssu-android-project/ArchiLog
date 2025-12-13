package com.example.frontrow.mypage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontrow.R;
import com.example.frontrow.review.ReviewDetailActivity;
import com.example.frontrow.review.ReviewItem;
import com.example.frontrow.review.ReviewRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyCalendarActivity extends AppCompatActivity {

    private TextView tvMonth;
    private RecyclerView rvCalendar;

    private Calendar currentMonth = Calendar.getInstance();
    private CalendarAdapter adapter;

    // yyyy.MM.dd -> thumbnail url
    private final Map<String, String> dateToThumb = new HashMap<>();

    // ✅ yyyy.MM.dd -> reviewId (상세로 넘어가려면 이게 필요)
    private final Map<String, String> dateToReviewId = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_calendar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvMonth = findViewById(R.id.tvMonth);
        rvCalendar = findViewById(R.id.rvCalendar);

        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));

        adapter = new CalendarAdapter(
                this,
                new ArrayList<>(),
                dateToThumb,
                dateKey -> {
                    // ✅ 썸네일 클릭 → 해당 날짜의 reviewId로 상세 이동
                    String reviewId = dateToReviewId.get(dateKey);
                    if (reviewId == null) {
                        Toast.makeText(this, "해당 날짜의 후기를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(this, ReviewDetailActivity.class);
                    intent.putExtra(ReviewDetailActivity.EXTRA_REVIEW_KEY, reviewId);
                    startActivity(intent);
                }
        );

        rvCalendar.setAdapter(adapter);

        findViewById(R.id.btnPrev).setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            renderMonth();
        });

        findViewById(R.id.btnNext).setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            renderMonth();
        });

        loadMyReviews();
    }

    private void loadMyReviews() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            renderMonth();
            return;
        }

        ReviewRepository.getReviewsByAuthor(uid,
                new ReviewRepository.Callback<List<ReviewItem>>() {
                    @Override
                    public void onSuccess(List<ReviewItem> list) {
                        dateToThumb.clear();
                        dateToReviewId.clear();

                        if (list != null) {
                            for (ReviewItem item : list) {
                                String key = parseDateKey(item.date);
                                if (key == null) continue;

                                // ✅ 썸네일 세팅
                                if (item.mainPhotoUrl != null) {
                                    dateToThumb.put(key, item.mainPhotoUrl);
                                }

                                // ✅ 상세 이동용 reviewId 세팅
                                // 같은 날짜에 여러 개면 "가장 최근에 들어온 것"으로 덮어씀(일단 단순 정책)
                                if (item.reviewId != null) {
                                    dateToReviewId.put(key, item.reviewId);
                                }
                            }
                        }

                        renderMonth();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(MyCalendarActivity.this,
                                "후기 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        renderMonth();
                    }
                });
    }

    private void renderMonth() {
        tvMonth.setText(String.format(
                Locale.KOREA,
                "%04d.%02d",
                currentMonth.get(Calendar.YEAR),
                currentMonth.get(Calendar.MONTH) + 1
        ));

        adapter.setItems(buildMonthCells());
    }

    private List<CalendarDay> buildMonthCells() {
        List<CalendarDay> result = new ArrayList<>();

        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDay = cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
        if (firstDay < 0) firstDay += 7;

        for (int i = 0; i < firstDay; i++) {
            result.add(CalendarDay.empty());
        }

        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH) + 1;

        for (int d = 1; d <= maxDay; d++) {
            String key = String.format(Locale.KOREA,
                    "%04d.%02d.%02d", y, m, d);
            result.add(CalendarDay.of(d, key));
        }

        while (result.size() % 7 != 0) {
            result.add(CalendarDay.empty());
        }

        return result;
    }

    // "2025.12.13 방문" -> "2025.12.13"
    private String parseDateKey(String date) {
        if (date == null) return null;
        return date.replace("방문", "").trim();
    }
}