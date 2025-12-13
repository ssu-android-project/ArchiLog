package com.example.frontrow.review;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.frontrow.R;

import java.util.List;

public class ReviewPagerAdapter extends RecyclerView.Adapter<ReviewPagerAdapter.ReviewViewHolder> {

    public interface OnCardClickListener {
        void onCardClick(ReviewItem item);
    }

    private final List<ReviewItem> items;
    private final OnCardClickListener listener;

    public ReviewPagerAdapter(List<ReviewItem> items, OnCardClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review_card, parent, false);
        return new ReviewViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        if (items == null || items.isEmpty()) return;

        int realPos = position % items.size();
        ReviewItem item = items.get(realPos);

        holder.tvTitle.setText(safe(item.title));
        holder.tvLocation.setText(safe(item.place));
        holder.tvDate.setText(safe(item.date));
        holder.tvSubtitle.setText(safe(item.body));

        float ratingFloat = 0f;
        try {
            if (item.rating != null && !item.rating.trim().isEmpty()) {
                ratingFloat = Float.parseFloat(item.rating.trim());
            }
        } catch (Exception ignored) {}
        holder.tvRating.setText("⭐ " + ratingFloat);

        holder.tag1.setText(safe(item.tag1));
        holder.tag2.setText(safe(item.tag2));

        String url = (item.mainPhotoUrl != null) ? item.mainPhotoUrl.trim() : "";
        if (!url.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.img_ecc)
                    .error(R.drawable.img_ecc)
                    .into(holder.imgPlace);
        } else {
            holder.imgPlace.setImageResource(R.drawable.img_ecc);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCardClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return (items == null || items.isEmpty()) ? 0 : Integer.MAX_VALUE;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlace;
        TextView tvTitle, tvLocation, tvDate, tvRating, tvSubtitle, tag1, tag2;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlace   = itemView.findViewById(R.id.imgPlace);
            tvTitle    = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate     = itemView.findViewById(R.id.tvDate);
            tvRating   = itemView.findViewById(R.id.tvRating);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tag1       = itemView.findViewById(R.id.tag1);
            tag2       = itemView.findViewById(R.id.tag2);
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }
}