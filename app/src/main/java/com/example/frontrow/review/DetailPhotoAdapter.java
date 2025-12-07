package com.example.frontrow.review;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontrow.R;

import java.util.List;

public class DetailPhotoAdapter extends RecyclerView.Adapter<DetailPhotoAdapter.PhotoViewHolder> {

    public interface OnPhotoClickListener {
        void onPhotoClick(int resId);
    }

    private final List<Integer> photoResIds;
    private final OnPhotoClickListener listener;

    public DetailPhotoAdapter(List<Integer> photoResIds, OnPhotoClickListener listener) {
        this.photoResIds = photoResIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_detail_photo_thumb, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        int resId = photoResIds.get(position);
        holder.imgThumb.setImageResource(resId);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPhotoClick(resId);
        });
    }

    @Override
    public int getItemCount() {
        return photoResIds != null ? photoResIds.size() : 0;
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.imgThumb);
        }
    }
}
