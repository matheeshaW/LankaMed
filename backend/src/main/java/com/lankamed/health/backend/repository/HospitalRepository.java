package com.lankamed.health.backend.repository;

import com.lankamed.health.backend.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    Optional<Hospital> findByName(String name);
    boolean existsByName(String name);
}
