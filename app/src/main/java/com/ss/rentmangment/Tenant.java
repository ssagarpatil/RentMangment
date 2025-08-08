package com.ss.rentmangment;

public class Tenant {
    public String tenantId;
    public String name;
    public String mobile;
    public String email;
    public String roomNumber;
    public String leaseStart;
    public String leaseEnd;
    public double rentAmount;
    public double securityDeposit;
    public String paymentStatus;
    public String emergencyContactName;
    public String emergencyContactPhone;
    public String idProofType;
    public String idProofNumber;
    public String notes;
    public String photoUrl;

    public Tenant() {}

    public Tenant(String tenantId, String name, String mobile, String email,
                  String roomNumber, String leaseStart, String leaseEnd,
                  double rentAmount, double securityDeposit, String paymentStatus,
                  String emergencyContactName, String emergencyContactPhone,
                  String idProofType, String idProofNumber, String notes, String photoUrl) {
        this.tenantId = tenantId;
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.roomNumber = roomNumber;
        this.leaseStart = leaseStart;
        this.leaseEnd = leaseEnd;
        this.rentAmount = rentAmount;
        this.securityDeposit = securityDeposit;
        this.paymentStatus = paymentStatus;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.idProofType = idProofType;
        this.idProofNumber = idProofNumber;
        this.notes = notes;
        this.photoUrl = photoUrl;
    }
}
