package com.lankamed.health.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "report_audit")
public class ReportAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "report_type", nullable = false, length = 100)
    private String reportType;

    @Column(name = "criteria_json", columnDefinition = "TEXT", nullable = false)
    private String criteriaJson;

    @Column(name = "generated_on", nullable = false)
    private LocalDateTime generatedOn;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getCriteriaJson() { return criteriaJson; }
    public void setCriteriaJson(String criteriaJson) { this.criteriaJson = criteriaJson; }
    public LocalDateTime getGeneratedOn() { return generatedOn; }
    public void setGeneratedOn(LocalDateTime generatedOn) { this.generatedOn = generatedOn; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}