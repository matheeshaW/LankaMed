package com.lankamed.health.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.lankamed.health.backend.model.Visit;

public interface VisitRepository extends JpaRepository<Visit, Long>, JpaSpecificationExecutor<Visit> {
    // Add queries for visit analytics as needed
}