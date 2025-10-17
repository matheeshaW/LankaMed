package com.lankamed.health.backend.dto;

public class PaymentDTO {
    private Long patientId;
    private Long appointmentId;
    private double amount;
    private String paymentMethod;
    private String cardNumber;
    private String insuranceNumber;
    private String transactionId;
    private String status;

    // Appointment details
    private String appointmentDateTime;
    private String doctorName;
    private String serviceName;
    private String hospitalName;
    private String appointmentDescription;

    // Constructors
    public PaymentDTO() {
    }

    public PaymentDTO(Long patientId, Long appointmentId, double amount, String paymentMethod,
                      String transactionId, String status) {
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.status = status;
    }

    public PaymentDTO(Long patientId, Long appointmentId, double amount, String paymentMethod,
                      String transactionId, String status, String appointmentDateTime,
                      String doctorName, String serviceName, String hospitalName,
                      String appointmentDescription) {
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.status = status;
        this.appointmentDateTime = appointmentDateTime;
        this.doctorName = doctorName;
        this.serviceName = serviceName;
        this.hospitalName = hospitalName;
        this.appointmentDescription = appointmentDescription;
    }

    // Getters and Setters
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Appointment detail getters and setters
    public String getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(String appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getAppointmentDescription() {
        return appointmentDescription;
    }

    public void setAppointmentDescription(String appointmentDescription) {
        this.appointmentDescription = appointmentDescription;
    }
}
