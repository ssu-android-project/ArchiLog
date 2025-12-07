package com.example.frontrow.review;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class CardCarouselTransformer implements ViewPager2.PageTransformer {

    private int pageOffsetPx;

    public CardCarouselTransformer(int pageOffsetPx) {
        this.pageOffsetPx = pageOffsetPx;
    }

    @Override
    public void transformPage(@NonNull View page, float position) {
        float absPos = Math.abs(position);

        // 가운데 카드 크게, 양 옆은 작게
        float scale = 0.85f + (1 - absPos) * 0.15f;
        page.setScaleX(scale);
        page.setScaleY(scale);

        // 살짝 겹쳐보이게 X 이동
        page.setTranslationX(-pageOffsetPx * position);

        // 양 옆은 반투명
        float alpha = 0.5f + (1 - absPos) * 0.5f;
        page.setAlpha(alpha);
    }
}