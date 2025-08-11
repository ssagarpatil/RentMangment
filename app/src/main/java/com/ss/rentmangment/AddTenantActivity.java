//package com.ss.rentmangment;
//
//import android.app.AlertDialog;
//import android.app.DatePickerDialog;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.util.Patterns;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.CheckBox;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.RadioButton;
//import android.widget.RadioGroup;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//import java.util.HashMap;
//import java.util.Map;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.card.MaterialCardView;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//import java.util.UUID;
//
//public class AddTenantActivity extends AppCompatActivity {
//
//    // Essential Fields (Always Visible)
//    private EditText etName, etMobile, etRent, etLeaseStart;
//    private Spinner spinnerRoomSelection;
//    private MaterialButton btnSelectRoom;
//    private RadioGroup rgTenantType;
//    private RadioButton rbFamily, rbStudent;
//
//    // Additional Fields (Initially Hidden)
//    private EditText etEmail, etLeaseEnd, etDeposit, etEmergencyName,
//            etEmergencyPhone, etIdProofType, etIdProofNumber, etNotes;
//
//    // UI Components
//    private MaterialButton btnSave;
//    private TextView tvMoreInfo, tvSelectedRoom;
//    private MaterialCardView cardMoreInfo;
//    private LinearLayout layoutAdditionalFields;
//
//    private DatabaseReference usersRef;
//    private SharedPreferences sharedPreferences;
//    private String adminMobile;
//
//    private List<RoomModel> availableRooms = new ArrayList<>();
//    private List<RoomModel> selectedRooms = new ArrayList<>();
//    private String selectedRoomKey = "";
//    private String selectedRoomName = "";
//    private boolean isMoreInfoExpanded = false;
//    private String tenantType = "Family"; // Default to Family
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_tenant);
//
//        initViews();
//        setupClickListeners();
//
//        usersRef = FirebaseDatabase.getInstance().getReference("users");
//        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
//        adminMobile = sharedPreferences.getString("mobile", "");
//
//        loadAvailableRooms();
//    }
//
//    private void initViews() {
//        // Essential Fields
//        etName = findViewById(R.id.etName);
//        etMobile = findViewById(R.id.etMobile);
//        etRent = findViewById(R.id.etRentAmount);
//        etLeaseStart = findViewById(R.id.etLeaseStart);
//        spinnerRoomSelection = findViewById(R.id.spinnerRoomSelection);
//        btnSelectRoom = findViewById(R.id.btnSelectRoom);
//        tvSelectedRoom = findViewById(R.id.tvSelectedRoom);
//
//        // Tenant Type Selection
//        rgTenantType = findViewById(R.id.rgTenantType);
//        rbFamily = findViewById(R.id.rbFamily);
//        rbStudent = findViewById(R.id.rbStudent);
//
//        // Additional Fields
//        etEmail = findViewById(R.id.etEmail);
//        etLeaseEnd = findViewById(R.id.etLeaseEnd);
//        etDeposit = findViewById(R.id.etSecurityDeposit);
//        etEmergencyName = findViewById(R.id.etEmergencyName);
//        etEmergencyPhone = findViewById(R.id.etEmergencyPhone);
//        etIdProofType = findViewById(R.id.etIdProofType);
//        etIdProofNumber = findViewById(R.id.etIdProofNumber);
//        etNotes = findViewById(R.id.etNotes);
//
//        // UI Components
//        btnSave = findViewById(R.id.btnSaveTenant);
//        tvMoreInfo = findViewById(R.id.tvMoreInfo);
//        cardMoreInfo = findViewById(R.id.cardMoreInfo);
//        layoutAdditionalFields = findViewById(R.id.layoutAdditionalFields);
//
//        // Initially hide additional fields
//        layoutAdditionalFields.setVisibility(View.GONE);
//
//        // Hide spinner initially (we'll use checkbox selection)
//        spinnerRoomSelection.setVisibility(View.GONE);
//
//        // Show initial selection text
//        tvSelectedRoom.setText("No room selected");
//
//        // Set default tenant type
//        rbFamily.setChecked(true);
//        tenantType = "Family";
//    }
//
//    private void setupClickListeners() {
//        etLeaseStart.setOnClickListener(v -> showDatePicker(etLeaseStart));
//        etLeaseEnd.setOnClickListener(v -> showDatePicker(etLeaseEnd));
//        btnSave.setOnClickListener(v -> saveTenant());
//        btnSelectRoom.setOnClickListener(v -> showRoomSelectionDialog());
//
//        // Tenant type selection listener
//        rgTenantType.setOnCheckedChangeListener((group, checkedId) -> {
//            if (checkedId == R.id.rbFamily) {
//                tenantType = "Family";
//            } else if (checkedId == R.id.rbStudent) {
//                tenantType = "Students";
//            }
//
//            // Clear previous selection when tenant type changes
//            selectedRoomKey = "";
//            selectedRoomName = "";
//            selectedRooms.clear();
//            tvSelectedRoom.setText("No room selected");
//
//            // Reload available rooms based on new tenant type
//            loadAvailableRooms();
//        });
//
//        // More Info toggle
//        tvMoreInfo.setOnClickListener(v -> toggleMoreInfo());
//        cardMoreInfo.setOnClickListener(v -> toggleMoreInfo());
//    }
//
//    private void loadAvailableRooms() {
//        DatabaseReference roomsRef = usersRef.child(adminMobile).child("rooms");
//        DatabaseReference tenantsRef = usersRef.child(adminMobile).child("tenants");
//
//        roomsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot roomSnapshot) {
//                // First get all tenants to understand room occupancy types
//                tenantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot tenantSnapshot) {
//                        availableRooms.clear();
//
//                        // Maps to track current room occupancy and tenant types
//                        Map<String, Boolean> roomHasFamily = new HashMap<>();
//                        Map<String, Integer> roomStudentCount = new HashMap<>();
//                        Map<String, String> roomOccupancyType = new HashMap<>(); // Track first occupant type
//
//                        // Analyze current tenants to determine room occupancy patterns
//                        for (DataSnapshot ds : tenantSnapshot.getChildren()) {
//                            Tenant tenant = ds.getValue(Tenant.class);
//                            if (tenant != null && tenant.roomNumber != null && !tenant.roomNumber.isEmpty()) {
//                                String roomId = tenant.roomNumber;
//
//                                // Determine if tenant is family (has emergency contact) or student
//                                boolean isFamily = (tenant.emergencyContactName != null &&
//                                        !tenant.emergencyContactName.isEmpty());
//
//                                if (isFamily) {
//                                    roomHasFamily.put(roomId, true);
//                                    roomOccupancyType.put(roomId, "Family");
//                                } else {
//                                    roomStudentCount.put(roomId, roomStudentCount.getOrDefault(roomId, 0) + 1);
//                                    // Only set as "Students" if no family is already assigned
//                                    if (!roomHasFamily.getOrDefault(roomId, false)) {
//                                        roomOccupancyType.put(roomId, "Students");
//                                    }
//                                }
//                            }
//                        }
//
//                        // Filter rooms based on tenant type and current occupancy
//                        for (DataSnapshot ds : roomSnapshot.getChildren()) {
//                            RoomModel room = ds.getValue(RoomModel.class);
//                            if (room != null && room.getAllowedFor() != null) {
//
//                                String roomId = room.getRoomId();
//                                boolean hasFamily = roomHasFamily.getOrDefault(roomId, false);
//                                int studentCount = roomStudentCount.getOrDefault(roomId, 0);
//                                String currentOccupancyType = roomOccupancyType.get(roomId);
//
//                                // Check if room supports the current tenant type
//                                boolean roomSupportsCurrentType = room.getAllowedFor().contains(tenantType);
//
//                                if (!roomSupportsCurrentType) {
//                                    continue; // Skip if room doesn't support this tenant type
//                                }
//
//                                if (tenantType.equals("Family")) {
//                                    // Family selection logic:
//                                    // 1. Room must be completely empty (no current occupants)
//                                    // 2. OR room must already have family (for edit mode scenarios)
//                                    if (room.getOccupied() == 0 ||
//                                            (hasFamily && "Family".equals(currentOccupancyType))) {
//                                        // Only add if no students are present
//                                        if (studentCount == 0) {
//                                            availableRooms.add(room);
//                                        }
//                                    }
//                                } else { // Students selection logic
//                                    // Students can book if:
//                                    // 1. No family is present AND room has available capacity
//                                    // 2. OR room already has students (sharing scenario)
//                                    if (!hasFamily && studentCount < room.getCapacity()) {
//                                        availableRooms.add(room);
//                                    }
//                                    // Additional check: if room is empty and supports students
//                                    else if (room.getOccupied() == 0 &&
//                                            room.getAllowedFor().contains("Students")) {
//                                        availableRooms.add(room);
//                                    }
//                                }
//                            }
//                        }
//
//                        updateButtonText();
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        Toast.makeText(AddTenantActivity.this, "Failed to check tenant data", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(AddTenantActivity.this, "Failed to load rooms", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void updateButtonText() {
//        String buttonText;
//        if (tenantType.equals("Family")) {
//            buttonText = "Select Room (" + availableRooms.size() + " available for family)";
//        } else {
//            buttonText = "Select Room (" + availableRooms.size() + " available for students)";
//        }
//        btnSelectRoom.setText(buttonText);
//
//        if (availableRooms.isEmpty()) {
//            btnSelectRoom.setEnabled(false);
//            if (tenantType.equals("Family")) {
//                btnSelectRoom.setText("No available rooms for family");
//            } else {
//                btnSelectRoom.setText("No available rooms for students");
//            }
//        } else {
//            btnSelectRoom.setEnabled(true);
//        }
//    }
//
//    private void showRoomSelectionDialog() {
//        if (availableRooms.isEmpty()) {
//            String message;
//            if (tenantType.equals("Family")) {
//                message = "No rooms available for family.\n\n" +
//                        "Possible reasons:\n" +
//                        "• All rooms are occupied by students\n" +
//                        "• Rooms are at full capacity\n" +
//                        "• No rooms allow family occupancy";
//            } else {
//                message = "No rooms available for students.\n\n" +
//                        "Possible reasons:\n" +
//                        "• All rooms are occupied by families\n" +
//                        "• Student-allowed rooms are at capacity\n" +
//                        "• No rooms allow student occupancy";
//            }
//
//            new AlertDialog.Builder(this)
//                    .setTitle("No Available Rooms")
//                    .setMessage(message)
//                    .setPositiveButton("OK", null)
//                    .show();
//            return;
//        }
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Select Room for " + tenantType);
//
//        LinearLayout layout = new LinearLayout(this);
//        layout.setOrientation(LinearLayout.VERTICAL);
//        layout.setPadding(50, 40, 50, 10);
//
//        // Add information header
//        TextView infoText = new TextView(this);
//        if (tenantType.equals("Family")) {
//            infoText.setText("🏠 Family Rooms\n" +
//                    "✅ First to book gets exclusive access\n" +
//                    "🚫 Students cannot book once family is assigned\n\n");
//        } else {
//            infoText.setText("🎓 Student Rooms\n" +
//                    "✅ Sharing allowed with other students\n" +
//                    "🚫 Cannot book if family is present\n\n");
//        }
//        infoText.setTextSize(12);
//        infoText.setPadding(10, 10, 10, 20);
//        layout.addView(infoText);
//
//        RadioGroup radioGroup = new RadioGroup(this);
//
//        for (int i = 0; i < availableRooms.size(); i++) {
//            RoomModel room = availableRooms.get(i);
//            RadioButton radioButton = new RadioButton(this);
//
//            String roomText = "🏠 " + room.getName() + " (" + room.getType() + ")";
//
//            // Show current occupancy status
//            if (room.getOccupied() == 0) {
//                roomText += "\n   ✨ Empty room - You'll be the first occupant";
//            } else {
//                if (tenantType.equals("Family")) {
//                    roomText += "\n   👥 Currently: " + room.getOccupied() + "/" + room.getCapacity() + " occupied";
//                    roomText += "\n   ⚠️ Will become exclusive to your family";
//                } else {
//                    int availableSpots = room.getCapacity() - room.getOccupied();
//                    roomText += "\n   👥 Current students: " + room.getOccupied() + "/" + room.getCapacity();
//                    roomText += "\n   📊 Available spots: " + availableSpots;
//                }
//            }
//
//            // Show what happens after booking
//            if (tenantType.equals("Family")) {
//                roomText += "\n   🔒 After booking: Room becomes family-exclusive";
//                roomText += "\n   🚫 Students will not see this room";
//            } else {
//                int availableAfter = room.getCapacity() - room.getOccupied() - 1;
//                roomText += "\n   📈 After booking: " + availableAfter + " spots for other students";
//                roomText += "\n   🚫 Families cannot book this room";
//            }
//
//            // Show rent information
//            if (room.getRent() > 0) {
//                if (tenantType.equals("Family")) {
//                    roomText += "\n   💰 Rent: ₹" + room.getRent() + " (Full room)";
//                } else {
//                    double sharedRent = room.getRent() / (double) room.getCapacity();
//                    roomText += "\n   💰 Rent: ₹" + Math.round(sharedRent) + " (Per person)";
//                }
//            }
//
//            radioButton.setText(roomText);
//            radioButton.setPadding(15, 20, 15, 20);
//            radioButton.setId(i);
//
//            if (room.getRoomId().equals(selectedRoomKey)) {
//                radioButton.setChecked(true);
//            }
//
//            radioGroup.addView(radioButton);
//
//            // Add separator line
//            if (i < availableRooms.size() - 1) {
//                View separator = new View(this);
//                separator.setLayoutParams(new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT, 2));
//                separator.setBackgroundColor(0xFFE0E0E0);
//                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) separator.getLayoutParams();
//                params.setMargins(0, 15, 0, 15);
//                radioGroup.addView(separator);
//            }
//        }
//
//        layout.addView(radioGroup);
//        builder.setView(layout);
//
//        builder.setPositiveButton("Select", (dialog, which) -> {
//            int selectedId = radioGroup.getCheckedRadioButtonId();
//
//            if (selectedId == -1) {
//                tvSelectedRoom.setText("No room selected");
//                selectedRoomKey = "";
//                selectedRoomName = "";
//                selectedRooms.clear();
//            } else {
//                RoomModel selectedRoom = availableRooms.get(selectedId);
//                selectedRoomKey = selectedRoom.getRoomId();
//                selectedRoomName = selectedRoom.getName();
//
//                // Clear and add to selectedRooms for compatibility
//                selectedRooms.clear();
//                selectedRooms.add(selectedRoom);
//
//                updateSelectedRoomDisplay(selectedRoom);
//                autoFillRentAndDeposit(selectedRoom);
//            }
//        });
//
//        builder.setNegativeButton("Cancel", null);
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
//
//    private void updateSelectedRoomDisplay(RoomModel selectedRoom) {
//        String displayText;
//        if (tenantType.equals("Family")) {
//            displayText = "✅ Selected: " + selectedRoom.getName() +
//                    " (" + selectedRoom.getType() + ")\n" +
//                    "🏠 Family room - Will become exclusive\n";
//
//            if (selectedRoom.getOccupied() == 0) {
//                displayText += "✨ Empty room - You'll be the first occupant\n";
//            }
//            displayText += "🔒 Room will be fully occupied after booking\n" +
//                    "🚫 Students will not be able to book this room";
//        } else {
//            int availableSpots = selectedRoom.getCapacity() - selectedRoom.getOccupied();
//            displayText = "✅ Selected: " + selectedRoom.getName() +
//                    " (" + selectedRoom.getType() + ")\n" +
//                    "👥 Student sharing room\n";
//
//            if (selectedRoom.getOccupied() == 0) {
//                displayText += "✨ Empty room - You'll be the first student\n";
//            } else {
//                displayText += "🤝 " + selectedRoom.getOccupied() + " students already here\n";
//            }
//            displayText += "📊 After booking: " + (availableSpots - 1) + " spots remaining\n" +
//                    "🚫 Families cannot book this room";
//        }
//
//        tvSelectedRoom.setText(displayText);
//    }
//
//    private void autoFillRentAndDeposit(RoomModel selectedRoom) {
//        if (selectedRoom.getRent() > 0) {
//            if (tenantType.equals("Family")) {
//                etRent.setText(String.valueOf(selectedRoom.getRent()));
//            } else {
//                double sharedRent = selectedRoom.getRent() / (double) selectedRoom.getCapacity();
//                etRent.setText(String.valueOf(Math.round(sharedRent)));
//            }
//        }
//
//        if (selectedRoom.getDeposit() > 0) {
//            if (tenantType.equals("Family")) {
//                etDeposit.setText(String.valueOf(selectedRoom.getDeposit()));
//            } else {
//                double sharedDeposit = selectedRoom.getDeposit() / (double) selectedRoom.getCapacity();
//                etDeposit.setText(String.valueOf(Math.round(sharedDeposit)));
//            }
//        }
//    }
//
//    private void toggleMoreInfo() {
//        if (isMoreInfoExpanded) {
//            // Collapse
//            layoutAdditionalFields.setVisibility(View.GONE);
//            tvMoreInfo.setText("More Info ▼");
//            isMoreInfoExpanded = false;
//        } else {
//            // Expand
//            layoutAdditionalFields.setVisibility(View.VISIBLE);
//            tvMoreInfo.setText("Less Info ▲");
//            isMoreInfoExpanded = true;
//        }
//    }
//
//    private void showDatePicker(EditText target) {
//        Calendar c = Calendar.getInstance();
//        DatePickerDialog picker = new DatePickerDialog(this,
//                (view, year, month, dayOfMonth) -> {
//                    month++;
//                    target.setText(dayOfMonth + "/" + month + "/" + year);
//                },
//                c.get(Calendar.YEAR),
//                c.get(Calendar.MONTH),
//                c.get(Calendar.DAY_OF_MONTH));
//        picker.show();
//    }
//
//    private void saveTenant() {
//        // Validate essential fields first
//        if (!validateEssentialFields()) {
//            return;
//        }
//
//        // If additional fields are visible, validate them too
//        if (isMoreInfoExpanded && !validateAdditionalFields()) {
//            return;
//        }
//
//        String name = etName.getText().toString().trim();
//        String tenantMobile = etMobile.getText().toString().trim();
//        String email = etEmail.getText().toString().trim();
//        String leaseStart = etLeaseStart.getText().toString().trim();
//        String leaseEnd = etLeaseEnd.getText().toString().trim();
//        String rentStr = etRent.getText().toString().trim();
//        String depositStr = etDeposit.getText().toString().trim();
//        String idProofType = etIdProofType.getText().toString().trim();
//        String idProofNumber = etIdProofNumber.getText().toString().trim();
//
//        // === Create Tenant object ===
//        String tenantId = UUID.randomUUID().toString();
//
//        // Set default lease end date if not provided (1 year from start)
//        if (TextUtils.isEmpty(leaseEnd)) {
//            leaseEnd = calculateDefaultLeaseEnd(leaseStart);
//        }
//
//        Tenant tenant = new Tenant(
//                tenantId,
//                name,
//                tenantMobile,
//                email,
//                selectedRoomName,
//                selectedRoomKey,
//                selectedRoomName,
//                leaseStart,
//                leaseEnd,
//                parseDouble(rentStr),
//                parseDouble(depositStr),
//                "Active",
//                etEmergencyName.getText().toString().trim(),
//                etEmergencyPhone.getText().toString().trim(),
//                idProofType,
//                idProofNumber,
//                etNotes.getText().toString().trim(),
//                ""
//        );
//
//        // Save tenant
//        usersRef.child(adminMobile).child("tenants").child(tenantMobile)
//                .setValue(tenant)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        // Update room occupancy based on tenant type
//                        if (tenantType.equals("Family")) {
//                            // Family takes the whole room - sets occupancy to full capacity
//                            updateRoomOccupancyForFamily(selectedRoomKey);
//                        } else {
//                            // Student takes one spot - increments occupancy by 1
//                            updateRoomOccupancy(selectedRoomKey, 1);
//                        }
//
//                        String successMessage = "Tenant added successfully!\n\n";
//                        if (tenantType.equals("Family")) {
//                            successMessage += "🏠 Room " + selectedRoomName + " is now family-exclusive.\n" +
//                                    "🚫 Students will not be able to book this room.";
//                        } else {
//                            successMessage += "👥 Room " + selectedRoomName + " is now student-shared.\n" +
//                                    "🚫 Families will not be able to book this room.";
//                        }
//
//                        Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show();
//                        finish();
//                    } else {
//                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    private void updateRoomOccupancyForFamily(String roomKey) {
//        if (TextUtils.isEmpty(roomKey)) return;
//
//        DatabaseReference roomRef = usersRef.child(adminMobile).child("rooms").child(roomKey);
//
//        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                RoomModel room = snapshot.getValue(RoomModel.class);
//                if (room != null) {
//                    // Family takes the entire room capacity - marks room as fully occupied
//                    room.setOccupied(room.getCapacity());
//                    roomRef.setValue(room).addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            // Room is now family-exclusive and will not appear for students
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(AddTenantActivity.this, "Error updating room occupancy", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private boolean validateEssentialFields() {
//        String name = etName.getText().toString().trim();
//        String tenantMobile = etMobile.getText().toString().trim();
//        String leaseStart = etLeaseStart.getText().toString().trim();
//        String rentStr = etRent.getText().toString().trim();
//
//        if (TextUtils.isEmpty(name)) {
//            etName.setError("Enter tenant name");
//            etName.requestFocus();
//            return false;
//        }
//
//        if (TextUtils.isEmpty(tenantMobile) || !tenantMobile.matches("\\d{10}")) {
//            etMobile.setError("Enter valid 10-digit mobile number");
//            etMobile.requestFocus();
//            return false;
//        }
//
//        if (selectedRooms.isEmpty() || TextUtils.isEmpty(selectedRoomKey)) {
//            Toast.makeText(this, "Please select a room", Toast.LENGTH_SHORT).show();
//            btnSelectRoom.requestFocus();
//            return false;
//        }
//
//        if (TextUtils.isEmpty(leaseStart)) {
//            etLeaseStart.setError("Select lease start date");
//            etLeaseStart.requestFocus();
//            return false;
//        }
//
//        if (TextUtils.isEmpty(rentStr) || parseDouble(rentStr) <= 0) {
//            etRent.setError("Enter valid rent amount");
//            etRent.requestFocus();
//            return false;
//        }
//
//        return true;
//    }
//
//    private boolean validateAdditionalFields() {
//        String email = etEmail.getText().toString().trim();
//        String depositStr = etDeposit.getText().toString().trim();
//        String idProofType = etIdProofType.getText().toString().trim();
//        String idProofNumber = etIdProofNumber.getText().toString().trim();
//        String emergencyPhone = etEmergencyPhone.getText().toString().trim();
//
//        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            etEmail.setError("Enter valid email");
//            etEmail.requestFocus();
//            return false;
//        }
//
//        if (!TextUtils.isEmpty(depositStr) && parseDouble(depositStr) < 0) {
//            etDeposit.setError("Enter valid deposit amount");
//            etDeposit.requestFocus();
//            return false;
//        }
//
//        if (!TextUtils.isEmpty(idProofType) && idProofType.equalsIgnoreCase("Aadhaar")) {
//            if (TextUtils.isEmpty(idProofNumber) || !idProofNumber.matches("\\d{12}")) {
//                etIdProofNumber.setError("Enter valid 12-digit Aadhaar number");
//                etIdProofNumber.requestFocus();
//                return false;
//            }
//        }
//
//        if (!TextUtils.isEmpty(emergencyPhone) && !emergencyPhone.matches("\\d{10}")) {
//            etEmergencyPhone.setError("Enter valid 10-digit phone");
//            etEmergencyPhone.requestFocus();
//            return false;
//        }
//
//        return true;
//    }
//
//    private String calculateDefaultLeaseEnd(String leaseStart) {
//        if (!TextUtils.isEmpty(leaseStart)) {
//            String[] parts = leaseStart.split("/");
//            if (parts.length == 3) {
//                int day = Integer.parseInt(parts[0]);
//                int month = Integer.parseInt(parts[1]);
//                int year = Integer.parseInt(parts[2]) + 1; // Add 1 year
//                return day + "/" + month + "/" + year;
//            }
//        }
//        return "";
//    }
//
//    private void updateRoomOccupancy(String roomKey, int change) {
//        if (TextUtils.isEmpty(roomKey)) return;
//
//        DatabaseReference roomRef = usersRef.child(adminMobile).child("rooms").child(roomKey);
//
//        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                RoomModel room = snapshot.getValue(RoomModel.class);
//                if (room != null) {
//                    int newOccupied = Math.max(0, room.getOccupied() + change);
//                    room.setOccupied(newOccupied);
//                    roomRef.setValue(room);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {}
//        });
//    }
//
//    private double parseDouble(String s) {
//        if (TextUtils.isEmpty(s)) return 0.0;
//        try {
//            return Double.parseDouble(s);
//        } catch (NumberFormatException e) {
//            return 0.0;
//        }
//    }
//}


package com.ss.rentmangment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class AddTenantActivity extends AppCompatActivity {

    // Essential Fields (Always Visible)
    private EditText etName, etMobile, etRent, etLeaseStart;
    private Spinner spinnerRoomSelection;
    private MaterialButton btnSelectRoom;
    private RadioGroup rgTenantType;
    private RadioButton rbFamily, rbStudent;

    // Additional Fields (Initially Hidden)
    private EditText etEmail, etLeaseEnd, etDeposit, etEmergencyName,
            etEmergencyPhone, etIdProofType, etIdProofNumber, etNotes;

    // UI Components
    private MaterialButton btnSave;
    private TextView tvMoreInfo, tvSelectedRoom;
    private MaterialCardView cardMoreInfo;
    private LinearLayout layoutAdditionalFields;

    private DatabaseReference usersRef;
    private SharedPreferences sharedPreferences;
    private String adminMobile;

    private List<RoomModel> availableRooms = new ArrayList<>();
    private List<RoomModel> selectedRooms = new ArrayList<>();
    private String selectedRoomKey = "";
    private String selectedRoomName = "";
    private boolean isMoreInfoExpanded = false;
    private String tenantType = "Family"; // Default to Family

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tenant);

        initViews();
        setupClickListeners();

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        adminMobile = sharedPreferences.getString("mobile", "");

        loadAvailableRooms();
    }

    private void initViews() {
        // Essential Fields
        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etRent = findViewById(R.id.etRentAmount);
        etLeaseStart = findViewById(R.id.etLeaseStart);
        spinnerRoomSelection = findViewById(R.id.spinnerRoomSelection);
        btnSelectRoom = findViewById(R.id.btnSelectRoom);
        tvSelectedRoom = findViewById(R.id.tvSelectedRoom);

        // Tenant Type Selection
        rgTenantType = findViewById(R.id.rgTenantType);
        rbFamily = findViewById(R.id.rbFamily);
        rbStudent = findViewById(R.id.rbStudent);

        // Additional Fields
        etEmail = findViewById(R.id.etEmail);
        etLeaseEnd = findViewById(R.id.etLeaseEnd);
        etDeposit = findViewById(R.id.etSecurityDeposit);
        etEmergencyName = findViewById(R.id.etEmergencyName);
        etEmergencyPhone = findViewById(R.id.etEmergencyPhone);
        etIdProofType = findViewById(R.id.etIdProofType);
        etIdProofNumber = findViewById(R.id.etIdProofNumber);
        etNotes = findViewById(R.id.etNotes);

        // UI Components
        btnSave = findViewById(R.id.btnSaveTenant);
        tvMoreInfo = findViewById(R.id.tvMoreInfo);
        cardMoreInfo = findViewById(R.id.cardMoreInfo);
        layoutAdditionalFields = findViewById(R.id.layoutAdditionalFields);

        // Initially hide additional fields
        layoutAdditionalFields.setVisibility(View.GONE);

        // Hide spinner initially (we'll use checkbox selection)
        spinnerRoomSelection.setVisibility(View.GONE);

        // Show initial selection text
        tvSelectedRoom.setText("No room selected");

        // Set default tenant type
        rbFamily.setChecked(true);
        tenantType = "Family";
    }

    private void setupClickListeners() {
        etLeaseStart.setOnClickListener(v -> showDatePicker(etLeaseStart));
        etLeaseEnd.setOnClickListener(v -> showDatePicker(etLeaseEnd));
        btnSave.setOnClickListener(v -> saveTenant());
        btnSelectRoom.setOnClickListener(v -> showRoomSelectionDialog());

        // Tenant type selection listener
        rgTenantType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbFamily) {
                tenantType = "Family";
            } else if (checkedId == R.id.rbStudent) {
                tenantType = "Students";
            }

            // Clear previous selection when tenant type changes
            selectedRoomKey = "";
            selectedRoomName = "";
            selectedRooms.clear();
            tvSelectedRoom.setText("No room selected");

            // Reload available rooms based on new tenant type
            loadAvailableRooms();
        });

        // More Info toggle
        tvMoreInfo.setOnClickListener(v -> toggleMoreInfo());
        cardMoreInfo.setOnClickListener(v -> toggleMoreInfo());
    }

    private void loadAvailableRooms() {
        DatabaseReference roomsRef = usersRef.child(adminMobile).child("rooms");
        DatabaseReference tenantsRef = usersRef.child(adminMobile).child("tenants");

        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot roomSnapshot) {
                // First get all tenants to understand room occupancy types
                tenantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot tenantSnapshot) {
                        availableRooms.clear();

                        // Maps to track current room occupancy and tenant types
                        Map<String, Boolean> roomHasFamily = new HashMap<>();
                        Map<String, Integer> roomStudentCount = new HashMap<>();
                        Map<String, String> roomOccupancyType = new HashMap<>();

                        // Analyze current tenants to determine room occupancy patterns
                        for (DataSnapshot ds : tenantSnapshot.getChildren()) {
                            Tenant tenant = ds.getValue(Tenant.class);
                            if (tenant != null && tenant.assignedRoomKey != null && !tenant.assignedRoomKey.isEmpty()) {
                                String roomId = tenant.assignedRoomKey;

                                // FIXED: Use a more reliable method to determine tenant type
                                // Check if tenant has emergency contact (indicates family)
                                // OR if they have deposit > rent (families typically pay more deposit)
                                boolean isFamily = (tenant.emergencyContactName != null &&
                                        !tenant.emergencyContactName.isEmpty()) ||
                                        (tenant.securityDeposit >= tenant.rentAmount * 2);

                                if (isFamily) {
                                    roomHasFamily.put(roomId, true);
                                    roomOccupancyType.put(roomId, "Family");
                                } else {
                                    roomStudentCount.put(roomId, roomStudentCount.getOrDefault(roomId, 0) + 1);
                                    // Only set as "Students" if no family is already assigned
                                    if (!roomHasFamily.getOrDefault(roomId, false)) {
                                        roomOccupancyType.put(roomId, "Students");
                                    }
                                }
                            }
                        }

                        // Filter rooms based on tenant type and current occupancy
                        for (DataSnapshot ds : roomSnapshot.getChildren()) {
                            RoomModel room = ds.getValue(RoomModel.class);
                            if (room != null && room.getAllowedFor() != null) {

                                String roomId = room.getRoomId();
                                boolean hasFamily = roomHasFamily.getOrDefault(roomId, false);
                                int studentCount = roomStudentCount.getOrDefault(roomId, 0);

                                // Check if room supports the current tenant type
                                boolean roomSupportsCurrentType = room.getAllowedFor().contains(tenantType);

                                if (!roomSupportsCurrentType) {
                                    continue; // Skip if room doesn't support this tenant type
                                }

                                if (tenantType.equals("Family")) {
                                    // Family selection logic: Room must be completely empty
                                    if (room.getOccupied() == 0) {
                                        availableRooms.add(room);
                                    }
                                } else { // Students selection logic
                                    // Students can book if: No family is present AND room has available capacity
                                    if (!hasFamily && studentCount < room.getCapacity()) {
                                        availableRooms.add(room);
                                    }
                                    // Additional check: if room is empty and supports students
                                    else if (room.getOccupied() == 0 &&
                                            room.getAllowedFor().contains("Students")) {
                                        availableRooms.add(room);
                                    }
                                }
                            }
                        }

                        updateButtonText();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddTenantActivity.this, "Failed to check tenant data", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddTenantActivity.this, "Failed to load rooms", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateButtonText() {
        String buttonText;
        if (tenantType.equals("Family")) {
            buttonText = "Select Room (" + availableRooms.size() + " available for family)";
        } else {
            buttonText = "Select Room (" + availableRooms.size() + " available for students)";
        }
        btnSelectRoom.setText(buttonText);

        if (availableRooms.isEmpty()) {
            btnSelectRoom.setEnabled(false);
            if (tenantType.equals("Family")) {
                btnSelectRoom.setText("No available rooms for family");
            } else {
                btnSelectRoom.setText("No available rooms for students");
            }
        } else {
            btnSelectRoom.setEnabled(true);
        }
    }

    private void showRoomSelectionDialog() {
        if (availableRooms.isEmpty()) {
            String message;
            if (tenantType.equals("Family")) {
                message = "No rooms available for family.\n\n" +
                        "Possible reasons:\n" +
                        "• All rooms are occupied\n" +
                        "• No rooms allow family occupancy";
            } else {
                message = "No rooms available for students.\n\n" +
                        "Possible reasons:\n" +
                        "• All rooms are occupied by families\n" +
                        "• Student-allowed rooms are at capacity";
            }

            new AlertDialog.Builder(this)
                    .setTitle("No Available Rooms")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Room for " + tenantType);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Add information header
        TextView infoText = new TextView(this);
        if (tenantType.equals("Family")) {
            infoText.setText("🏠 Family Rooms\n" +
                    "✅ Full room exclusive access\n" +
                    "🚫 Students cannot book once family is assigned\n\n");
        } else {
            infoText.setText("🎓 Student Rooms\n" +
                    "✅ Sharing allowed with other students\n" +
                    "🚫 Cannot book if family is present\n\n");
        }
        infoText.setTextSize(12);
        infoText.setPadding(10, 10, 10, 20);
        layout.addView(infoText);

        RadioGroup radioGroup = new RadioGroup(this);

        for (int i = 0; i < availableRooms.size(); i++) {
            RoomModel room = availableRooms.get(i);
            RadioButton radioButton = new RadioButton(this);

            String roomText = "🏠 " + room.getName() + " (" + room.getType() + ")";

            // Show current occupancy status
            if (room.getOccupied() == 0) {
                roomText += "\n   ✨ Empty room - You'll be the first occupant";
            } else {
                if (tenantType.equals("Students")) {
                    int availableSpots = room.getCapacity() - room.getOccupied();
                    roomText += "\n   👥 Current students: " + room.getOccupied() + "/" + room.getCapacity();
                    roomText += "\n   📊 Available spots: " + availableSpots;
                }
            }

            // Show what happens after booking
            if (tenantType.equals("Family")) {
                roomText += "\n   🔒 After booking: Room becomes family-exclusive";
                roomText += "\n   🚫 Students will not see this room";
            } else {
                int availableAfter = room.getCapacity() - room.getOccupied() - 1;
                roomText += "\n   📈 After booking: " + availableAfter + " spots for other students";
            }

            // Show rent information
            if (room.getRent() > 0) {
                if (tenantType.equals("Family")) {
                    roomText += "\n   💰 Rent: ₹" + room.getRent() + " (Full room)";
                } else {
                    double sharedRent = room.getRent() / (double) room.getCapacity();
                    roomText += "\n   💰 Rent: ₹" + Math.round(sharedRent) + " (Per person)";
                }
            }

            radioButton.setText(roomText);
            radioButton.setPadding(15, 20, 15, 20);
            radioButton.setId(i);

            if (room.getRoomId().equals(selectedRoomKey)) {
                radioButton.setChecked(true);
            }

            radioGroup.addView(radioButton);

            // Add separator line
            if (i < availableRooms.size() - 1) {
                View separator = new View(this);
                separator.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 2));
                separator.setBackgroundColor(0xFFE0E0E0);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) separator.getLayoutParams();
                params.setMargins(0, 15, 0, 15);
                radioGroup.addView(separator);
            }
        }

        layout.addView(radioGroup);
        builder.setView(layout);

        builder.setPositiveButton("Select", (dialog, which) -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId == -1) {
                tvSelectedRoom.setText("No room selected");
                selectedRoomKey = "";
                selectedRoomName = "";
                selectedRooms.clear();
            } else {
                RoomModel selectedRoom = availableRooms.get(selectedId);
                selectedRoomKey = selectedRoom.getRoomId();
                selectedRoomName = selectedRoom.getName();

                // Clear and add to selectedRooms for compatibility
                selectedRooms.clear();
                selectedRooms.add(selectedRoom);

                updateSelectedRoomDisplay(selectedRoom);
                autoFillRentAndDeposit(selectedRoom);
            }
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateSelectedRoomDisplay(RoomModel selectedRoom) {
        String displayText;
        if (tenantType.equals("Family")) {
            displayText = "✅ Selected: " + selectedRoom.getName() +
                    " (" + selectedRoom.getType() + ")\n" +
                    "🏠 Family room - Will become exclusive\n";

            if (selectedRoom.getOccupied() == 0) {
                displayText += "✨ Empty room - You'll be the first occupant\n";
            }
            displayText += "🔒 Room will be fully occupied after booking\n" +
                    "🚫 Students will not be able to book this room";
        } else {
            int availableSpots = selectedRoom.getCapacity() - selectedRoom.getOccupied();
            displayText = "✅ Selected: " + selectedRoom.getName() +
                    " (" + selectedRoom.getType() + ")\n" +
                    "👥 Student sharing room\n";

            if (selectedRoom.getOccupied() == 0) {
                displayText += "✨ Empty room - You'll be the first student\n";
            } else {
                displayText += "🤝 " + selectedRoom.getOccupied() + " students already here\n";
            }
            displayText += "📊 After booking: " + (availableSpots - 1) + " spots remaining\n" +
                    "🚫 Families cannot book this room";
        }

        tvSelectedRoom.setText(displayText);
    }

    private void autoFillRentAndDeposit(RoomModel selectedRoom) {
        if (selectedRoom.getRent() > 0) {
            if (tenantType.equals("Family")) {
                etRent.setText(String.valueOf(selectedRoom.getRent()));
            } else {
                double sharedRent = selectedRoom.getRent() / (double) selectedRoom.getCapacity();
                etRent.setText(String.valueOf(Math.round(sharedRent)));
            }
        }

        if (selectedRoom.getDeposit() > 0) {
            if (tenantType.equals("Family")) {
                etDeposit.setText(String.valueOf(selectedRoom.getDeposit()));
            } else {
                double sharedDeposit = selectedRoom.getDeposit() / (double) selectedRoom.getCapacity();
                etDeposit.setText(String.valueOf(Math.round(sharedDeposit)));
            }
        }
    }

    private void toggleMoreInfo() {
        if (isMoreInfoExpanded) {
            // Collapse
            layoutAdditionalFields.setVisibility(View.GONE);
            tvMoreInfo.setText("More Info ▼");
            isMoreInfoExpanded = false;
        } else {
            // Expand
            layoutAdditionalFields.setVisibility(View.VISIBLE);
            tvMoreInfo.setText("Less Info ▲");
            isMoreInfoExpanded = true;
        }
    }

    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    month++;
                    target.setText(dayOfMonth + "/" + month + "/" + year);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    private void saveTenant() {
        // Validate essential fields first
        if (!validateEssentialFields()) {
            return;
        }

        // If additional fields are visible, validate them too
        if (isMoreInfoExpanded && !validateAdditionalFields()) {
            return;
        }

        String name = etName.getText().toString().trim();
        String tenantMobile = etMobile.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String leaseStart = etLeaseStart.getText().toString().trim();
        String leaseEnd = etLeaseEnd.getText().toString().trim();
        String rentStr = etRent.getText().toString().trim();
        String depositStr = etDeposit.getText().toString().trim();
        String idProofType = etIdProofType.getText().toString().trim();
        String idProofNumber = etIdProofNumber.getText().toString().trim();

        // === Create Tenant object ===
        String tenantId = UUID.randomUUID().toString();

        // Set default lease end date if not provided (1 year from start)
        if (TextUtils.isEmpty(leaseEnd)) {
            leaseEnd = calculateDefaultLeaseEnd(leaseStart);
        }

        // FIXED: Use the constructor that matches your Tenant class
        Tenant tenant = new Tenant(
                tenantId,
                name,
                tenantMobile,
                email,
                selectedRoomName,        // roomNumber
                selectedRoomKey,         // assignedRoomKey
                selectedRoomName,        // assignedRoomName
                leaseStart,
                leaseEnd,
                parseDouble(rentStr),
                parseDouble(depositStr),
                "Active",
                etEmergencyName.getText().toString().trim(),
                etEmergencyPhone.getText().toString().trim(),
                idProofType,
                idProofNumber,
                etNotes.getText().toString().trim(),
                ""
        );

        // Save tenant
        usersRef.child(adminMobile).child("tenants").child(tenantMobile)
                .setValue(tenant)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update room occupancy based on tenant type
                        if (tenantType.equals("Family")) {
                            // Family takes the whole room - sets occupancy to full capacity
                            updateRoomOccupancyForFamily(selectedRoomKey);
                        } else {
                            // Student takes one spot - increments occupancy by 1
                            updateRoomOccupancy(selectedRoomKey, 1);
                        }

                        String successMessage = "Tenant added successfully!\n\n";
                        if (tenantType.equals("Family")) {
                            successMessage += "🏠 Room " + selectedRoomName + " is now family-exclusive.\n" +
                                    "🚫 Students will not be able to book this room.";
                        } else {
                            successMessage += "👥 Room " + selectedRoomName + " is now student-shared.\n" +
                                    "🚫 Families will not be able to book this room.";
                        }

                        Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateRoomOccupancyForFamily(String roomKey) {
        if (TextUtils.isEmpty(roomKey)) return;

        DatabaseReference roomRef = usersRef.child(adminMobile).child("rooms").child(roomKey);

        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                RoomModel room = snapshot.getValue(RoomModel.class);
                if (room != null) {
                    // Family takes the entire room capacity - marks room as fully occupied
                    room.setOccupied(room.getCapacity());
                    roomRef.setValue(room).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Room is now family-exclusive and will not appear for students
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddTenantActivity.this, "Error updating room occupancy", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateEssentialFields() {
        String name = etName.getText().toString().trim();
        String tenantMobile = etMobile.getText().toString().trim();
        String leaseStart = etLeaseStart.getText().toString().trim();
        String rentStr = etRent.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Enter tenant name");
            etName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(tenantMobile) || !tenantMobile.matches("\\d{10}")) {
            etMobile.setError("Enter valid 10-digit mobile number");
            etMobile.requestFocus();
            return false;
        }

        if (selectedRooms.isEmpty() || TextUtils.isEmpty(selectedRoomKey)) {
            Toast.makeText(this, "Please select a room", Toast.LENGTH_SHORT).show();
            btnSelectRoom.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(leaseStart)) {
            etLeaseStart.setError("Select lease start date");
            etLeaseStart.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(rentStr) || parseDouble(rentStr) <= 0) {
            etRent.setError("Enter valid rent amount");
            etRent.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateAdditionalFields() {
        String email = etEmail.getText().toString().trim();
        String depositStr = etDeposit.getText().toString().trim();
        String idProofType = etIdProofType.getText().toString().trim();
        String idProofNumber = etIdProofNumber.getText().toString().trim();
        String emergencyPhone = etEmergencyPhone.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter valid email");
            etEmail.requestFocus();
            return false;
        }

        if (!TextUtils.isEmpty(depositStr) && parseDouble(depositStr) < 0) {
            etDeposit.setError("Enter valid deposit amount");
            etDeposit.requestFocus();
            return false;
        }

        if (!TextUtils.isEmpty(idProofType) && idProofType.equalsIgnoreCase("Aadhaar")) {
            if (TextUtils.isEmpty(idProofNumber) || !idProofNumber.matches("\\d{12}")) {
                etIdProofNumber.setError("Enter valid 12-digit Aadhaar number");
                etIdProofNumber.requestFocus();
                return false;
            }
        }

        if (!TextUtils.isEmpty(emergencyPhone) && !emergencyPhone.matches("\\d{10}")) {
            etEmergencyPhone.setError("Enter valid 10-digit phone");
            etEmergencyPhone.requestFocus();
            return false;
        }

        return true;
    }

    private String calculateDefaultLeaseEnd(String leaseStart) {
        if (!TextUtils.isEmpty(leaseStart)) {
            String[] parts = leaseStart.split("/");
            if (parts.length == 3) {
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]) + 1; // Add 1 year
                return day + "/" + month + "/" + year;
            }
        }
        return "";
    }

    private void updateRoomOccupancy(String roomKey, int change) {
        if (TextUtils.isEmpty(roomKey)) return;

        DatabaseReference roomRef = usersRef.child(adminMobile).child("rooms").child(roomKey);

        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                RoomModel room = snapshot.getValue(RoomModel.class);
                if (room != null) {
                    int newOccupied = Math.max(0, room.getOccupied() + change);
                    room.setOccupied(newOccupied);
                    roomRef.setValue(room);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private double parseDouble(String s) {
        if (TextUtils.isEmpty(s)) return 0.0;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
