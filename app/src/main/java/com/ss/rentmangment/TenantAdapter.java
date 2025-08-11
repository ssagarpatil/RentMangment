package com.ss.rentmangment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.TenantViewHolder> {

    private Context context;
    private List<Tenant> tenantList;
    private String adminMobile;

    public TenantAdapter(Context context, List<Tenant> tenantList) {
        this.context = context;
        this.tenantList = tenantList;

        // Get admin mobile from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        this.adminMobile = sharedPreferences.getString("mobile", "");
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

        // Show assigned room name if available, otherwise show roomNumber
        String roomText = "Room: ";
        if (tenant.assignedRoomName != null && !tenant.assignedRoomName.isEmpty()) {
            roomText += tenant.assignedRoomName;
        } else if (tenant.roomNumber != null && !tenant.roomNumber.isEmpty()) {
            roomText += tenant.roomNumber;
        } else {
            roomText += "Not Assigned";
        }
        holder.tvRoom.setText(roomText);

        holder.tvPhone.setText(tenant.mobile);

        // Load image if exists
        if (tenant.photoUrl != null && !tenant.photoUrl.isEmpty()) {
            Glide.with(context).load(tenant.photoUrl).placeholder(R.drawable.ic_person).into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_person);
        }

        // Set click listener for item
        holder.itemView.setOnClickListener(v -> showOptionsMenu(v, tenant, position));

        // Long click for options
        holder.itemView.setOnLongClickListener(v -> {
            showOptionsMenu(v, tenant, position);
            return true;
        });
    }

    private void showOptionsMenu(View view, Tenant tenant, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenu().add(0, 1, 0, "Edit");
        popupMenu.getMenu().add(0, 2, 0, "Delete");

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    editTenant(tenant);
                    return true;
                case 2:
                    deleteTenant(tenant, position);
                    return true;
                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    private void editTenant(Tenant tenant) {
        Intent intent = new Intent(context, AddTenantActivity.class);
        intent.putExtra("tenant_mobile", tenant.mobile);
        intent.putExtra("edit_mode", true);
        context.startActivity(intent);
    }

    private void deleteTenant(Tenant tenant, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Tenant")
                .setMessage("Are you sure you want to delete this tenant?\n\n" +
                        "Name: " + tenant.name + "\n" +
                        "Mobile: " + tenant.mobile + "\n" +
                        "Room: " + (tenant.assignedRoomName != null ? tenant.assignedRoomName : "Not Assigned") +
                        "\n\nThis will automatically free up space in the assigned room.\n" +
                        "This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    performDelete(tenant, position);
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performDelete(Tenant tenant, int position) {
        // Show progress dialog
        AlertDialog progressDialog = new AlertDialog.Builder(context)
                .setTitle("Deleting Tenant")
                .setMessage("Please wait...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        DatabaseReference tenantRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(adminMobile)
                .child("tenants")
                .child(tenant.mobile);

        tenantRef.removeValue().addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                // Note: Room occupancy will be automatically updated by RoomsFragment listener
                // No need to manually update room occupancy here

                // Remove from list and update adapter
                tenantList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, tenantList.size());

                Toast.makeText(context, "Tenant deleted successfully. Room occupancy updated automatically.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error deleting tenant: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return tenantList.size();
    }

    public void updateList(List<Tenant> newList) {
        this.tenantList = newList;
        notifyDataSetChanged();
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
