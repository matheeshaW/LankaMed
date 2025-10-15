package com.lankamed.health.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lankamed.health.backend.model.ReportAudit;

public interface ReportAuditRepository extends JpaRepository<ReportAudit, Long> {
    // Add custom queries for audit if needed
}