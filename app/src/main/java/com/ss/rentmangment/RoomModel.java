package com.ss.rentmangment;

public class RoomModel {
    private String roomId;
    private String name;           // Room No / Name
    private String type;           // 1BHK / 2BHK / 1RK / 1R
    private String allowedFor;     // "Family", "Students" or "Family,Students"
    private int capacity;          // total capacity
    private int occupied;          // current occupied (kept in DB, updated from tenants)
    private int rent;              // monthly rent
    private int deposit;           // security deposit
    private int maintenanceCharges;
    private String notes;
    private long lastUpdated;

    public RoomModel() {}

    public RoomModel(String roomId, String name, String type, String allowedFor,
                int capacity, int occupied, int rent, int deposit,
                int maintenanceCharges, String notes, long lastUpdated) {
        this.roomId = roomId;
        this.name = name;
        this.type = type;
        this.allowedFor = allowedFor;
        this.capacity = capacity;
        this.occupied = occupied;
        this.rent = rent;
        this.deposit = deposit;
        this.maintenanceCharges = maintenanceCharges;
        this.notes = notes;
        this.lastUpdated = lastUpdated;
    }

    // Getters & setters (only important ones shown; Android Studio can generate the rest)
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAllowedFor() { return allowedFor; }
    public void setAllowedFor(String allowedFor) { this.allowedFor = allowedFor; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getOccupied() { return occupied; }
    public void setOccupied(int occupied) { this.occupied = occupied; }

    public int getRent() { return rent; }
    public void setRent(int rent) { this.rent = rent; }

    public int getDeposit() { return deposit; }
    public void setDeposit(int deposit) { this.deposit = deposit; }

    public int getMaintenanceCharges() { return maintenanceCharges; }
    public void setMaintenanceCharges(int maintenanceCharges) { this.maintenanceCharges = maintenanceCharges; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}

