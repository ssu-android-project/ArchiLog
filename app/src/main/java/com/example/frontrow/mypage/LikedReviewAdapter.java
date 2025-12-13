package com.example.frontrow.mypage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.frontrow.R;
import com.example.frontrow.review.ReviewItem;

import java.util.List;

public class LikedReviewAdapter extends RecyclerView.Adapter<LikedReviewAdapter.VH> {

    public interface OnClick {
        void onClick(ReviewItem item);
    }

    private final List<ReviewItem> items;
    private final OnClick onClick;

    public LikedReviewAdapter(List<ReviewItem> items, OnClick onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_liked_review, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        if (items == null || position < 0 || position >= items.size()) return;

        ReviewItem item = items.get(position);
        if (item == null) return;

        String url = (item.mainPhotoUrl != null) ? item.mainPhotoUrl.trim() : "";
        if (!url.isEmpty()) {
            Glide.with(h.itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.img_ecc)
                    .error(R.drawable.img_ecc)
                    .into(h.imgThumb);
        } else {
            h.imgThumb.setImageResource(R.drawable.img_ecc);
        }

        h.tvTitle.setText(item.title != null ? item.title : "");

        StringBuilder sb = new StringBuilder();
        if (item.place != null && !item.place.isEmpty()) sb.append(item.place);
        if (item.date != null && !item.date.isEmpty()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(item.date);
        }
        h.tvSub.setText(sb.toString());

        String rating = (item.rating != null) ? item.rating : "";
        if (!rating.isEmpty() && !rating.startsWith("⭐")) rating = "⭐ " + rating;
        h.tvRating.setText(rating);

        h.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvTitle, tvSub, tvRating;

        VH(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.imgThumb);
            tvTitle  = itemView.findViewById(R.id.tvTitle);
            tvSub    = itemView.findViewById(R.id.tvSub);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }
}