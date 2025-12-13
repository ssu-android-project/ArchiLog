package com.example.frontrow.mypage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontrow.R;

import java.util.List;

public class BookmarkedPlaceAdapter extends RecyclerView.Adapter<BookmarkedPlaceAdapter.VH> {

    public interface OnClick {
        void onClick(BookmarkedPlaceItem item);
    }

    private final List<BookmarkedPlaceItem> items;
    private final OnClick onClick;

    public BookmarkedPlaceAdapter(List<BookmarkedPlaceItem> items, OnClick onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bookmarked_place, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        BookmarkedPlaceItem item = items.get(position);

        h.img.setImageResource(item.imageResId);
        h.tvName.setText(item.name);
        h.tvArchitect.setText(item.architect);

        h.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName;
        TextView tvArchitect;

        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            tvName = itemView.findViewById(R.id.tvName);
            tvArchitect = itemView.findViewById(R.id.tvArchitect);
        }
    }
}