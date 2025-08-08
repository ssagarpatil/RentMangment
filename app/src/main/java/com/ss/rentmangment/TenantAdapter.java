package com.ss.rentmangment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;

import java.util.List;

public class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.TenantViewHolder> {

    private Context context;
    private List<Tenant> tenantList;

    public TenantAdapter(Context context, List<Tenant> tenantList) {
        this.context = context;
        this.tenantList = tenantList;
    }

    @NonNull
    @Override
    public TenantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TenantViewHolder(LayoutInflater.from(context).inflate(R.layout.item_tenant, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TenantViewHolder holder, int position) {
        Tenant tenant = tenantList.get(position);
        holder.tvName.setText(tenant.name);
        holder.tvRoom.setText("Room: " + tenant.roomNumber);
        holder.tvPhone.setText(tenant.mobile);

        // Load image if exists
        if (tenant.photoUrl != null && !tenant.photoUrl.isEmpty()) {
            Glide.with(context).load(tenant.photoUrl).placeholder(R.drawable.ic_person).into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    public int getItemCount() {
        return tenantList.size();
    }

    static class TenantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRoom, tvPhone;
        ImageView ivPhoto;

        TenantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTenantName);
            tvRoom = itemView.findViewById(R.id.tvTenantRoom);
            tvPhone = itemView.findViewById(R.id.tvTenantPhone);
            ivPhoto = itemView.findViewById(R.id.ivTenantPhoto);
        }
    }
}
