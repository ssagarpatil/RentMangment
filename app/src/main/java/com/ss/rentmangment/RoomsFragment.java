package com.ss.rentmangment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomsFragment extends Fragment {

    RecyclerView rvRooms;
    FloatingActionButton fabAdd;
    RoomsAdapter adapter;
    List<RoomModel> roomList = new ArrayList<>();

    DatabaseReference roomsRef;
    DatabaseReference tenantsRef;
    String adminId;

    public RoomsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rooms, container, false);

        rvRooms = view.findViewById(R.id.rvRooms);
        fabAdd = view.findViewById(R.id.fab_add_room);

        rvRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RoomsAdapter(roomList, new RoomsAdapter.OnRoomActionListener() {
            @Override
            public void onEdit(RoomModel room) {
                showAddEditDialog(room);
            }

            @Override
            public void onDelete(RoomModel room) {
                confirmDelete(room);
            }
        });
        rvRooms.setAdapter(adapter);

        // Get adminId from SharedPreferences
        if (getContext() != null) {
            adminId = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    .getString("mobile", "default_admin");
        } else {
            adminId = "default_admin";
        }

        // Set path: /users/{adminId}/rooms
        roomsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(adminId)
                .child("rooms");

        // Set path: /users/{adminId}/tenants for listening to tenant changes
        tenantsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(adminId)
                .child("tenants");

        loadRooms();
        listenToTenantChanges();

        fabAdd.setOnClickListener(v -> showAddEditDialog(null));

        return view;
    }

    /** Load rooms from DB */
    private void loadRooms() {
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

                // Update room occupancy based on current tenants
                updateRoomOccupancyFromTenants();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load rooms: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Listen to tenant changes and update room occupancy accordingly */
    private void listenToTenantChanges() {
        tenantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updateRoomOccupancyFromTenants();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to listen to tenant changes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Update room occupancy based on current tenants */
    private void updateRoomOccupancyFromTenants() {
        tenantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot tenantsSnapshot) {
                // Count tenants per room
                Map<String, Integer> roomOccupancyCount = new HashMap<>();
                Map<String, String> roomTenantTypes = new HashMap<>();

                for (DataSnapshot tenantSnapshot : tenantsSnapshot.getChildren()) {
                    Tenant tenant = tenantSnapshot.getValue(Tenant.class);
                    if (tenant != null && tenant.roomNumber != null && !tenant.roomNumber.isEmpty()) {
                        String roomId = tenant.roomNumber;

                        // Count tenants in each room
                        roomOccupancyCount.put(roomId, roomOccupancyCount.getOrDefault(roomId, 0) + 1);

                        // Store tenant type for the room (assuming all tenants in a room are same type)
                        // This helps determine if it's family (full occupancy) or students (partial)
                        roomTenantTypes.put(roomId, determineTenantType(tenant));
                    }
                }

                // Update room occupancy in Firebase
                for (RoomModel room : roomList) {
                    String roomId = room.getRoomId();
                    int currentOccupancy = roomOccupancyCount.getOrDefault(roomId, 0);
                    String tenantType = roomTenantTypes.get(roomId);

                    // For family rooms, if there's any tenant, mark as full occupancy
                    if ("Family".equals(tenantType) && currentOccupancy > 0) {
                        currentOccupancy = room.getCapacity();
                    }

                    // Update only if occupancy has changed
                    if (room.getOccupied() != currentOccupancy) {
                        updateRoomOccupancy(roomId, currentOccupancy);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to update room occupancy: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Determine tenant type based on tenant data or room assignment logic */
    private String determineTenantType(Tenant tenant) {
        // You can implement logic here to determine if it's family or student
        // For now, we'll use a simple heuristic:
        // If tenant has emergency contact, likely family, otherwise student
        if (tenant.emergencyContactName != null && !tenant.emergencyContactName.isEmpty()) {
            return "Family";
        } else {
            return "Students";
        }
    }

    /** Update room occupancy in Firebase */
    private void updateRoomOccupancy(String roomId, int newOccupancy) {
        roomsRef.child(roomId).child("occupied").setValue(newOccupancy)
                .addOnSuccessListener(aVoid -> {
                    // Update local room list for immediate UI update
                    for (RoomModel room : roomList) {
                        if (room.getRoomId().equals(roomId)) {
                            room.setOccupied(newOccupancy);
                            break;
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update room occupancy: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /** Show add/edit dialog */
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

            // Make allowedFor final
            final String allowedFor;
            if (family && students) {
                allowedFor = "Family,Students";
            } else if (family) {
                allowedFor = "Family";
            } else if (students) {
                allowedFor = "Students";
            } else {
                allowedFor = "";
            }

            String sCapacity = etCapacity.getText().toString().trim();
            String sRent = etRent.getText().toString().trim();
            String sDeposit = etDeposit.getText().toString().trim();
            String sMaintenance = etMaintenance.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("Enter room name/number");
                return;
            }
            if (TextUtils.isEmpty(sCapacity)) {
                etCapacity.setError("Enter capacity");
                return;
            }
            if (!family && !students) {
                Toast.makeText(getContext(), "Select at least one allowed occupant type", Toast.LENGTH_SHORT).show();
                return;
            }

            int capacity, rent, deposit, maintenance;
            try {
                capacity = Integer.parseInt(sCapacity);
                rent = TextUtils.isEmpty(sRent) ? 0 : Integer.parseInt(sRent);
                deposit = TextUtils.isEmpty(sDeposit) ? 0 : Integer.parseInt(sDeposit);
                maintenance = TextUtils.isEmpty(sMaintenance) ? 0 : Integer.parseInt(sMaintenance);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Enter valid number values", Toast.LENGTH_SHORT).show();
                return;
            }

            if (capacity <= 0) {
                etCapacity.setError("Capacity must be greater than 0");
                return;
            }

            // Clean room name to make it suitable as Firebase key
            String roomKey = name.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();

            if (isEdit) {
                // If room name changed, delete old entry and create new one
                String originalRoomKey = editRoom.getName().replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
                if (!roomKey.equals(originalRoomKey)) {
                    roomsRef.child(originalRoomKey).removeValue();
                }

                int occupied = editRoom.getOccupied();
                long now = System.currentTimeMillis();
                RoomModel updated = new RoomModel(roomKey, name, type, allowedFor, capacity, occupied, rent, deposit, maintenance, notes, now);

                roomsRef.child(roomKey).setValue(updated)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Room updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                // Check if room name already exists
                roomsRef.child(roomKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(getContext(), "Room " + name + " already exists!", Toast.LENGTH_SHORT).show();
                        } else {
                            long now = System.currentTimeMillis();
                            RoomModel newRoom = new RoomModel(roomKey, name, type, allowedFor, capacity, 0, rent, deposit, maintenance, notes, now);

                            roomsRef.child(roomKey).setValue(newRoom)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Room added", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Add failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error checking room: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.show();
    }

    /** Delete a room */
    private void confirmDelete(RoomModel room) {
        // First check if room has any tenants
        tenantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasActiveTenants = false;
                int tenantCount = 0;

                for (DataSnapshot tenantSnapshot : snapshot.getChildren()) {
                    Tenant tenant = tenantSnapshot.getValue(Tenant.class);
                    if (tenant != null && room.getRoomId().equals(tenant.roomNumber)) {
                        hasActiveTenants = true;
                        tenantCount++;
                    }
                }

                if (hasActiveTenants) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Cannot Delete Room")
                            .setMessage("Room " + room.getName() + " has " + tenantCount + " active tenant(s). " +
                                    "Please move or remove all tenants before deleting this room.")
                            .setPositiveButton("OK", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    // Proceed with deletion
                    showDeleteConfirmation(room);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error checking tenants: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation(RoomModel room) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Room")
                .setMessage("Are you sure you want to delete room " + room.getName() + "?\n\n" +
                        "Room Type: " + room.getType() + "\n" +
                        "Capacity: " + room.getCapacity() + "\n" +
                        "Current Occupancy: " + room.getOccupied() + "\n\n" +
                        "This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    roomsRef.child(room.getRoomId()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Room " + room.getName() + " deleted successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove listeners to prevent memory leaks
        if (roomsRef != null) {
            roomsRef.removeEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {}
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        if (tenantsRef != null) {
            tenantsRef.removeEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {}
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }
}
