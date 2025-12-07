package com.example.frontrow.review;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontrow.R;

import java.util.List;

public class ReviewPagerAdapter extends RecyclerView.Adapter<ReviewPagerAdapter.ReviewViewHolder> {

    public interface OnCardClickListener {
        void onCardClick(ReviewCard card);
    }

    private final List<ReviewCard> items;
    private final OnCardClickListener listener;

    public ReviewPagerAdapter(List<ReviewCard> items, OnCardClickListener listener) {
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
        ReviewCard card = items.get(realPos);

        holder.tvTitle.setText(card.getTitle());
        holder.tvLocation.setText(card.getLocation());
        holder.tvDate.setText(card.getDate());
        holder.tvSubtitle.setText(card.getBody());

        String ratingText = "⭐ " + card.getRating();
        holder.tvRating.setText(ratingText);

        holder.tag1.setText(card.getTag1());
        holder.tag2.setText(card.getTag2());

        holder.imgPlace.setImageResource(card.getImageResId());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCardClick(card);
            }
        });
    }

    @Override
    public int getItemCount() {
        // 무한 캐러셀용
        return (items == null || items.isEmpty()) ? 0 : Integer.MAX_VALUE;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlace;
        TextView tvTitle;
        TextView tvLocation;
        TextView tvDate;
        TextView tvRating;
        TextView tvSubtitle;
        TextView tag1;
        TextView tag2;

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
}