package com.caftans.mobile.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.caftans.mobile.R;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class HeroAdapter extends RecyclerView.Adapter<HeroAdapter.HeroViewHolder> {

    private List<HeroItem> heroItems;
    private OnHeroActionListener listener;

    public interface OnHeroActionListener {
        void onActionClick(HeroItem item);
    }

    public HeroAdapter(List<HeroItem> heroItems, OnHeroActionListener listener) {
        this.heroItems = heroItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HeroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hero_banner, parent, false);
        return new HeroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HeroViewHolder holder, int position) {
        HeroItem item = heroItems.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvDescription.setText(item.getDescription());
        holder.btnAction.setText(item.getActionButtonText());

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(holder.ivHeroImage);
        } else {
            holder.ivHeroImage.setImageResource(R.drawable.placeholder_image);
        }

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onActionClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return heroItems.size();
    }

    static class HeroViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHeroImage;
        TextView tvTitle;
        TextView tvDescription;
        MaterialButton btnAction;

        HeroViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHeroImage = itemView.findViewById(R.id.ivHeroImage);
            tvTitle = itemView.findViewById(R.id.tvHeroTitle);
            tvDescription = itemView.findViewById(R.id.tvHeroDescription);
            btnAction = itemView.findViewById(R.id.btnHeroAction);
        }
    }
}
