package com.lankamed.health.backend.repository;

import com.lankamed.health.backend.model.StaffDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffDetailsRepository extends JpaRepository<StaffDetails, Long> {
    Optional<StaffDetails> findByStaffId(Long staffId);
    List<StaffDetails> findByHospitalHospitalId(Long hospitalId);
    List<StaffDetails> findByServiceCategoryCategoryId(Long categoryId);
}
