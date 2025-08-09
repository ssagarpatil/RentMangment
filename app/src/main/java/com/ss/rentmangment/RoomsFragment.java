package com.ss.rentmangment;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;
import com.ss.rentmangment.RoomModel;
import com.ss.rentmangment.RoomsAdapter;

import java.util.ArrayList;
import java.util.List;

public class RoomsFragment extends Fragment {

    RecyclerView rvRooms;
    FloatingActionButton fabAdd;
    RoomsAdapter adapter;
    List<RoomModel> roomList = new ArrayList<>();

    DatabaseReference roomsRef;
    String adminId;

    public RoomsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_rooms, container, false);

        rvRooms = view.findViewById(R.id.rvRooms);
        fabAdd = view.findViewById(R.id.fab_add_room);

        rvRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RoomsAdapter(roomList, new RoomsAdapter.OnRoomActionListener() {
            @Override
            public void onEdit(RoomModel room) {
                showAddEditDialog(room); // pass room to edit
            }

            @Override
            public void onDelete(RoomModel room) {
                confirmDelete(room);
            }
        });
        rvRooms.setAdapter(adapter);

        // **Use SharedPreferences to get adminId** (assuming you store mobile or UID under "mobile")
        if (getContext() != null) {
            adminId = getContext().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE)
                    .getString("mobile", "default_admin"); // default fallback
        } else {
            adminId = "default_admin";
        }

        // Update DatabaseReference path accordingly
        roomsRef = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child(adminId).child("Rooms");

        loadRooms();

        fabAdd.setOnClickListener(v -> showAddEditDialog(null));

        return view;
    }

    private void loadRooms() {
        // Listen for realtime changes
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RoomModel r = ds.getValue(RoomModel.class);
                    if (r != null) {
                        roomList.add(r);
                    }
                }
                adapter.setList(roomList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load rooms: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddEditDialog(RoomModel editRoom) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_room, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText etName = dialogView.findViewById(R.id.etRoomName);
        Spinner spinnerType = dialogView.findViewById(R.id.spinnerType);
        CheckBox cbFamily = dialogView.findViewById(R.id.cbFamily);
        CheckBox cbStudents = dialogView.findViewById(R.id.cbStudents);
        EditText etCapacity = dialogView.findViewById(R.id.etCapacity);
        EditText etStudentCapacity = dialogView.findViewById(R.id.etStudentCapacity);
        EditText etRent = dialogView.findViewById(R.id.etRent);
        EditText etDeposit = dialogView.findViewById(R.id.etDeposit);
        EditText etMaintenance = dialogView.findViewById(R.id.etMaintenance);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        String[] types = new String[]{"1BHK", "2BHK", "1RK", "1R", "Dormitory"};
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, types);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(spAdapter);

        cbStudents.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etStudentCapacity.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        boolean isEdit = editRoom != null;
        if (isEdit) {
            etName.setText(editRoom.getName());
            for (int i = 0; i < types.length; i++)
                if (types[i].equals(editRoom.getType()))
                    spinnerType.setSelection(i);
            if (editRoom.getAllowedFor() != null) {
                cbFamily.setChecked(editRoom.getAllowedFor().contains("Family"));
                cbStudents.setChecked(editRoom.getAllowedFor().contains("Students"));
            }
            etCapacity.setText(String.valueOf(editRoom.getCapacity()));
            etStudentCapacity.setText(""); // no separate field, keep empty
            etRent.setText(String.valueOf(editRoom.getRent()));
            etDeposit.setText(String.valueOf(editRoom.getDeposit()));
            etMaintenance.setText(String.valueOf(editRoom.getMaintenanceCharges()));
            etNotes.setText(editRoom.getNotes());
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String type = spinnerType.getSelectedItem().toString();
            boolean family = cbFamily.isChecked();
            boolean students = cbStudents.isChecked();
            String allowedFor = "";
            if (family) allowedFor += "Family";
            if (students) {
                if (!allowedFor.isEmpty()) allowedFor += ",";
                allowedFor += "Students";
            }
            String sCapacity = etCapacity.getText().toString().trim();
            String sRent = etRent.getText().toString().trim();
            String sDeposit = etDeposit.getText().toString().trim();
            String sMaintenance = etMaintenance.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("Enter name");
                return;
            }
            if (TextUtils.isEmpty(sCapacity)) {
                etCapacity.setError("Enter capacity");
                return;
            }

            int capacity, rent, deposit, maintenance;
            try {
                capacity = Integer.parseInt(sCapacity);
                rent = TextUtils.isEmpty(sRent) ? 0 : Integer.parseInt(sRent);
                deposit = TextUtils.isEmpty(sDeposit) ? 0 : Integer.parseInt(sDeposit);
                maintenance = TextUtils.isEmpty(sMaintenance) ? 0 : Integer.parseInt(sMaintenance);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid number values", Toast.LENGTH_SHORT).show();
                return;
            }
            long now = System.currentTimeMillis();

            if (isEdit) {
                String roomId = editRoom.getRoomId();
                int occupied = editRoom.getOccupied();
                RoomModel updated = new RoomModel(roomId, name, type, allowedFor, capacity, occupied, rent, deposit, maintenance, notes, now);
                roomsRef.child(roomId).setValue(updated)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Room updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                String newId = roomsRef.push().getKey();
                if (newId == null) {
                    Toast.makeText(getContext(), "Error creating new id", Toast.LENGTH_SHORT).show();
                    return;
                }
                RoomModel newRoom = new RoomModel(newId, name, type, allowedFor, capacity, 0, rent, deposit, maintenance, notes, now);
                roomsRef.child(newId).setValue(newRoom)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Room added", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Add failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        dialog.show();
    }

    private void confirmDelete(RoomModel room) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete room")
                .setMessage("Are you sure you want to delete " + room.getName() + "? Ensure no tenant is assigned.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    roomsRef.child(room.getRoomId()).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Room deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
