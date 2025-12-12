package com.caftans.mobile.ui.caftan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.caftans.mobile.R;
import com.caftans.mobile.data.api.ApiClient;
import com.caftans.mobile.data.models.Caftan;
import java.util.List;

public class CaftanAdapter extends RecyclerView.Adapter<CaftanAdapter.ViewHolder> {
    
    private List<Caftan> caftans;
    private OnCaftanClickListener listener;
    
    public interface OnCaftanClickListener {
        void onCaftanClick(Caftan caftan);
    }
    
    public CaftanAdapter(List<Caftan> caftans, OnCaftanClickListener listener) {
        this.caftans = caftans;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_caftan, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Caftan caftan = caftans.get(position);
        holder.tvName.setText(caftan.getName());
        holder.tvPrice.setText(String.format("%.0f MAD/jour", caftan.getPricePerDay()));
        
        // Load image with Glide
        String imageUrl = caftan.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String fullUrl = imageUrl.startsWith("http") ? imageUrl : 
                ApiClient.BASE_URL_WITHOUT_API + imageUrl;
            Glide.with(holder.itemView.getContext())
                    .load(fullUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_image);
        }
        
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCaftanClick(caftan);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return caftans.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageView;
        TextView tvName;
        TextView tvPrice;
        
        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}

