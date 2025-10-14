package com.lankamed.health.backend.repository;

import com.lankamed.health.backend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByDoctorStaffIdOrderByCreatedAtDesc(Long doctorId);
    
    Optional<Review> findByAppointmentAppointmentId(Long appointmentId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.doctor.staffId = :doctorId")
    Double findAverageRatingByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.doctor.staffId = :doctorId")
    Long countReviewsByDoctorId(@Param("doctorId") Long doctorId);
}
