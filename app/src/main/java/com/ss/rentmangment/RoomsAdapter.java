package com.ss.rentmangment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ss.rentmangment.R;

import java.util.List;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomVH> {

    public interface OnRoomActionListener {
        void onEdit(RoomModel room);
        void onDelete(RoomModel room);
    }

    private List<RoomModel> list;
    private OnRoomActionListener listener;

    public RoomsAdapter(List<RoomModel> list, OnRoomActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomVH holder, int position) {
        RoomModel r = list.get(position);
        holder.tvRoomName.setText(r.getName());
        holder.tvTypeCapacity.setText(r.getType() + " • Capacity: " + r.getCapacity());
        int occupied = r.getOccupied();
        int available = r.getCapacity() - occupied;
        holder.tvOccupiedAvailable.setText("Occupied: " + occupied + " • Available: " + available);
        holder.tvRent.setText("₹" + r.getRent());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(r);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(r);
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void setList(List<RoomModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    static class RoomVH extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvTypeCapacity, tvOccupiedAvailable, tvRent;
        ImageButton btnEdit, btnDelete;

        public RoomVH(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvTypeCapacity = itemView.findViewById(R.id.tvTypeCapacity);
            tvOccupiedAvailable = itemView.findViewById(R.id.tvOccupiedAvailable);
            tvRent = itemView.findViewById(R.id.tvRent);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
