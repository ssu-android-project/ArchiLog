package com.example.frontrow.review;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ViewPager2 vpReview;
    private ReviewPagerAdapter adapter;
    private List<ReviewCard> data;   // 더미 + 사용자가 작성한 후기까지 담는 리스트

    // ★ 후기 작성 화면 결과 받는 런처
    private final ActivityResultLauncher<Intent> reviewWriteLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Intent dataIntent = result.getData();

                            String title = dataIntent.getStringExtra(ReviewWriteActivity.RESULT_TITLE);
                            String place = dataIntent.getStringExtra(ReviewWriteActivity.RESULT_PLACE);
                            String date  = dataIntent.getStringExtra(ReviewWriteActivity.RESULT_DATE);
                            String tag1  = dataIntent.getStringExtra(ReviewWriteActivity.RESULT_TAG1);
                            String tag2  = dataIntent.getStringExtra(ReviewWriteActivity.RESULT_TAG2);
                            String body  = dataIntent.getStringExtra(ReviewWriteActivity.RESULT_BODY);
                            int ratingInt = dataIntent.getIntExtra(ReviewWriteActivity.RESULT_RATING, 0);

                            // 필수값 없으면 추가 X
                            if (title == null || place == null || date == null || body == null || ratingInt == 0) {
                                return;
                            }

                            // 한 줄 요약 = 본문 앞부분 잘라서 사용
                            String subtitle = body.length() > 40 ? body.substring(0, 40) + "..." : body;

                            ReviewCard newCard = new ReviewCard(
                                    title,
                                    place,
                                    date,
                                    (float) ratingInt,   // 지금은 1~5 정수 그대로 사용
                                    tag1,
                                    tag2,
                                    subtitle,
                                    body,
                                    R.drawable.img_ddp   // ★ 새 후기 기본 썸네일 (나중에 Uri 연동 가능)
                            );

                            data.add(newCard);
                            adapter.notifyDataSetChanged();
                        }
                    }
            );

    public ReviewFragment() { }

    public static ReviewFragment newInstance(String param1, String param2) {
        ReviewFragment fragment = new ReviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vpReview = view.findViewById(R.id.vpReview);

        // ★ ArrayList로 생성 (나중에 후기 추가하려고)
        data = new ArrayList<>();

        // ===== 더미 데이터 3개 =====
        data.add(new ReviewCard(
                "DDP 야간 답사",
                "서울특별시 중구",
                "2024. 10. 02 방문",
                4.7f,
                "파라메트릭 디자인",
                "미래적 금속 파사드",
                "야간 조명이 너무 예뻐서 사진 찍기 최고였음.",
                "해가 완전히 진 시간에 갔더니 금속 패널에 네온사인, 자동차 헤드라이트가 뒤섞여서 진짜 SF 영화 세트장 느낌이었음. " +
                        "곡선형 파사드 덕분에 어느 각도에서 찍어도 사진이 잘 나와서 카메라를 쉬지 않고 들게 됨. " +
                        "지하 전시장 내려가는 램프 동선도 부드럽게 이어져서, ‘걸으면서 공간을 감상하는 경험’이 아주 좋았던 곳.",
                R.drawable.img_ddp
        ));

        data.add(new ReviewCard(
                "송은문화재단 방문기",
                "서울특별시 강남구 청담동",
                "2024. 09. 18 방문",
                4.3f,
                "노출 콘크리트",
                "입면 레이어링",
                "콘크리트 질감이 정말 근사했고 내부 동선이 흥미로웠음.",
                "외관부터 딱 ‘도시 속 작은 미술관 박스’ 같은 느낌. 노출 콘크리트 표면이 거칠지 않고 은은하게 마감돼 있어서, " +
                        "햇빛이 비칠 때 생기는 그림자가 더 선명하게 느껴졌음. " +
                        "전시실로 올라가는 계단 동선이 단순히 층을 이동하는 기능이 아니라, 창을 통해 청담동 골목 풍경을 잘라서 보여주는 프레임 역할을 해서 " +
                        "‘밖과 안이 겹쳐지는 느낌’을 계속 주는 게 인상적이었음.",
                R.drawable.img_songeun
        ));

        data.add(new ReviewCard(
                "더현대 서울 탐방",
                "서울특별시 영등포구 여의도",
                "2024. 11. 01 방문",
                4.8f,
                "실내 보이드",
                "자연광 아트리움",
                "야경도 예쁘고 실내 보이드 공간이 확 트여서 인상 깊었음.",
                "쇼핑몰이라기보다는 거대한 실내 공원에 가까운 느낌. 중앙 보이드가 워낙 크게 뚫려 있어서, " +
                        "위층에서 아래층을 내려다보거나, 반대로 아래에서 천장을 올려다볼 때 스케일감이 확 살아남. " +
                        "자연광이 층층이 떨어지면서 식재랑 어우러져서 ‘실내지만 야외 같은 밝기’가 유지되는 것도 좋았고, " +
                        "층별 동선이 빙글빙글 돌아가는 구조라서 돌아다니면서 계속 새로운 뷰포인트가 나오는 게 재미있었음.",
                R.drawable.img_thehyundai
        ));

        // ===== 어댑터 + 카드 클릭 시 상세화면 이동 =====
        adapter = new ReviewPagerAdapter(data, card -> {
            Intent intent = new Intent(requireContext(), ReviewDetailActivity.class);
            intent.putExtra(ReviewDetailActivity.EXTRA_TITLE,  card.getTitle());
            intent.putExtra(ReviewDetailActivity.EXTRA_PLACE,  card.getLocation());
            intent.putExtra(ReviewDetailActivity.EXTRA_DATE,   card.getDate());
            intent.putExtra(ReviewDetailActivity.EXTRA_RATING, String.valueOf(card.getRating()));
            intent.putExtra(ReviewDetailActivity.EXTRA_TAG1,   card.getTag1());
            intent.putExtra(ReviewDetailActivity.EXTRA_TAG2,   card.getTag2());
            intent.putExtra(ReviewDetailActivity.EXTRA_BODY,   card.getBody());
            intent.putExtra(ReviewDetailActivity.EXTRA_IMAGE_RES_ID, card.getImageResId());
            startActivity(intent);
        });

        vpReview.setAdapter(adapter);
        vpReview.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        vpReview.setOffscreenPageLimit(3);

        // ViewPager2 내부 RecyclerView 클리핑 해제
        RecyclerView recyclerView = (RecyclerView) vpReview.getChildAt(0);
        if (recyclerView != null) {
            recyclerView.setClipToPadding(false);
        }

        int pageOffsetPx = (int) (40 * getResources().getDisplayMetrics().density);
        vpReview.setPageTransformer(new CardCarouselTransformer(pageOffsetPx));

        // 무한 루프 시작 위치 가운데로
        if (data.size() > 1) {
            int middle = Integer.MAX_VALUE / 2;
            int start = middle - (middle % data.size());
            vpReview.setCurrentItem(start, false);
        }

        // ★ 플로팅 버튼 → 후기 작성 화면 (결과를 런처로 받음)
        ImageView btnAddReview = view.findViewById(R.id.btnAddReview);
        btnAddReview.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ReviewWriteActivity.class);
            reviewWriteLauncher.launch(intent);
        });
    }
}