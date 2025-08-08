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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.ss.rentmangment.R;
import com.ss.rentmangment.RoomsAdapter;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoomsFragment extends Fragment {

    RecyclerView rvRooms;
    ImageButton dummy; // placeholder if needed
    com.google.android.material.floatingactionbutton.FloatingActionButton fabAdd;
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

        // adminId from FirebaseAuth if logged-in
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            adminId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            adminId = "default_admin"; // fallback for dev/testing
        }

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

        // spinner data
        String[] types = new String[]{"1BHK", "2BHK", "1RK", "1R", "Dormitory"};
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, types);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(spAdapter);

        // show/hide student capacity
        cbStudents.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etStudentCapacity.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // if editing, prefill values
        final boolean isEdit = editRoom != null;
        if (isEdit) {
            etName.setText(editRoom.getName());
            // set spinner selection
            for (int i = 0; i < types.length; i++) if (types[i].equals(editRoom.getType())) spinnerType.setSelection(i);
            String allowed = editRoom.getAllowedFor();
            if (allowed != null) {
                cbFamily.setChecked(allowed.contains("Family"));
                cbStudents.setChecked(allowed.contains("Students"));
            }
            etCapacity.setText(String.valueOf(editRoom.getCapacity()));
            etStudentCapacity.setText(""); // optional: not stored separately in model
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
                etName.setError("Enter room name");
                return;
            }
            if (TextUtils.isEmpty(sCapacity)) {
                etCapacity.setError("Enter capacity");
                return;
            }

            int capacity = Integer.parseInt(sCapacity);
            int rent = TextUtils.isEmpty(sRent) ? 0 : Integer.parseInt(sRent);
            int deposit = TextUtils.isEmpty(sDeposit) ? 0 : Integer.parseInt(sDeposit);
            int maintenance = TextUtils.isEmpty(sMaintenance) ? 0 : Integer.parseInt(sMaintenance);

            long now = System.currentTimeMillis();
            if (isEdit) {
                // update existing
                String roomId = editRoom.getRoomId();
                // keep occupied as before
                int occupied = editRoom.getOccupied();
                RoomModel updated = new RoomModel(roomId, name, type, allowedFor, capacity, occupied, rent, deposit, maintenance, notes, now);
                roomsRef.child(roomId).setValue(updated)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Room updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                // new room
                String newId = roomsRef.push().getKey();
                if (newId == null) {
                    Toast.makeText(getContext(), "Unable to create id", Toast.LENGTH_SHORT).show();
                    return;
                }
                int occupied = 0; // new room starts empty
                RoomModel newRoom = new RoomModel(newId, name, type, allowedFor, capacity, occupied, rent, deposit, maintenance, notes, now);
                roomsRef.child(newId).setValue(newRoom)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Room added", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        dialog.show();
    }

    private void confirmDelete(RoomModel room) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete room")
                .setMessage("Are you sure you want to delete " + room.getName() + "? (Make sure no tenant assigned.)")
                .setPositiveButton("Delete", (dialog, which) -> {
                    roomsRef.child(room.getRoomId()).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
