package com.example.frontrow.mypage;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.frontrow.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.VH> {

    public interface OnThumbClickListener {
        void onThumbClick(String dateKey);
    }

    private final Context context;
    private List<CalendarDay> items;
    private final Map<String, String> dateToThumb; // yyyy.MM.dd -> thumbUrl
    private final OnThumbClickListener listener;

    // ✅ 오늘 날짜 key (yyyy.MM.dd)
    private final String todayKey =
            new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(new Date());

    public CalendarAdapter(Context context,
                           List<CalendarDay> items,
                           Map<String, String> dateToThumb,
                           OnThumbClickListener listener) {
        this.context = context;
        this.items = items;
        this.dateToThumb = dateToThumb;
        this.listener = listener;
    }

    public void setItems(List<CalendarDay> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CalendarDay day = items.get(position);

        // ✅ RecyclerView 재활용 때문에 매번 기본 상태로 리셋
        h.imgThumb.setOnClickListener(null);
        h.imgThumb.setBackgroundResource(R.drawable.bg_calendar_thumb_round);
        h.tvDay.setTypeface(null, Typeface.NORMAL);
        h.tvDay.setTextColor(ContextCompat.getColor(context, R.color.gray));
        h.tvDay.setVisibility(View.VISIBLE);

        // 빈칸
        if (day.isEmpty) {
            h.tvDay.setText("");
            h.tvDay.setVisibility(View.GONE);
            h.imgThumb.setVisibility(View.GONE);
            return;
        }

        boolean isToday = day.dateKey != null && day.dateKey.equals(todayKey);

        String thumbUrl = (day.dateKey == null) ? null : dateToThumb.get(day.dateKey);

        if (thumbUrl != null && !thumbUrl.trim().isEmpty()) {
            // ✅ 후기 있는 날짜 → 날짜 텍스트 숨기고 썸네일만
            h.tvDay.setText("");
            h.tvDay.setVisibility(View.GONE);

            h.imgThumb.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(thumbUrl)
                    .placeholder(R.drawable.img_ecc)
                    .centerCrop()
                    .into(h.imgThumb);

            // ✅ 썸네일 클릭 → Activity로 dateKey 전달
            h.imgThumb.setOnClickListener(v -> {
                if (listener != null) listener.onThumbClick(day.dateKey);
            });

            // ✅ 오늘 날짜면 썸네일 테두리(강조)
            if (isToday) {
                h.imgThumb.setBackgroundResource(R.drawable.bg_calendar_today);
            } else {
                h.imgThumb.setBackgroundResource(R.drawable.bg_calendar_thumb_round);
            }

        } else {
            // ✅ 후기 없는 날짜 → 날짜 숫자만
            h.imgThumb.setVisibility(View.GONE);

            h.tvDay.setVisibility(View.VISIBLE);
            h.tvDay.setText(String.valueOf(day.day));

            // ✅ 오늘 날짜면 숫자 찐하게
            if (isToday) {
                h.tvDay.setTextColor(ContextCompat.getColor(context, R.color.black));
                h.tvDay.setTypeface(h.tvDay.getTypeface(), Typeface.BOLD);
            } else {
                h.tvDay.setTextColor(ContextCompat.getColor(context, R.color.gray));
                h.tvDay.setTypeface(null, Typeface.NORMAL);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDay;
        ImageView imgThumb;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            imgThumb = itemView.findViewById(R.id.imgThumb);
        }
    }
}