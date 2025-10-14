package com.lankamed.health.backend.repository;

import com.lankamed.health.backend.model.StaffDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<StaffDetails, Long> {
    
    @Query("SELECT s FROM StaffDetails s WHERE s.user.role = 'DOCTOR' " +
           "AND (:name IS NULL OR LOWER(CONCAT(s.user.firstName, ' ', s.user.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:specialization IS NULL OR LOWER(s.specialization) LIKE LOWER(CONCAT('%', :specialization, '%')))")
    List<StaffDetails> findDoctorsByNameAndSpecialization(@Param("name") String name, @Param("specialization") String specialization);
    
    @Query("SELECT s FROM StaffDetails s WHERE s.user.role = 'DOCTOR'")
    List<StaffDetails> findAllDoctors();
}
