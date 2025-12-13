package com.example.frontrow.review;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.frontrow.R;

import java.util.List;

public class DetailPhotoAdapter extends RecyclerView.Adapter<DetailPhotoAdapter.VH> {

    public interface OnPhotoClickListener {
        void onClick(String url);
    }

    private final List<String> photoUrls;
    private final OnPhotoClickListener listener;

    public DetailPhotoAdapter(List<String> photoUrls, OnPhotoClickListener listener) {
        this.photoUrls = photoUrls;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_detail_photo_thumb, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String url = photoUrls.get(position);

        Glide.with(holder.itemView.getContext())
                .load(url)
                .placeholder(R.drawable.img_ecc)
                .error(R.drawable.img_ecc)
                .into(holder.img);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(url);
        });
    }

    @Override
    public int getItemCount() {
        return photoUrls == null ? 0 : photoUrls.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgThumb);
        }
    }
}