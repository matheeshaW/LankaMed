package com.lankamed.health.backend.repository;

import com.lankamed.health.backend.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientPatientIdOrderByAppointmentDateTimeDesc(Long patientId);
    List<Appointment> findByPatientUserEmailOrderByAppointmentDateTimeDesc(String email);
    List<Appointment> findByDoctorStaffIdOrderByAppointmentDateTimeDesc(Long doctorId);
    List<Appointment> findByHospitalHospitalIdOrderByAppointmentDateTimeDesc(Long hospitalId);
    
    @Query("SELECT a FROM Appointment a WHERE a.patient.user.email = :email AND a.appointmentDateTime >= :startDate ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findUpcomingAppointmentsByPatientEmail(@Param("email") String email, @Param("startDate") LocalDateTime startDate);

    // Fetch-join all associations to avoid LazyInitializationException when building admin views
    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.patient p " +
            "JOIN FETCH p.user " +
            "JOIN FETCH a.doctor d " +
            "JOIN FETCH d.user " +
            "JOIN FETCH a.hospital h " +
            "JOIN FETCH a.serviceCategory s " +
            "ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findAllWithDetails();

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.patient p " +
            "JOIN FETCH p.user " +
            "JOIN FETCH a.doctor d " +
            "JOIN FETCH d.user " +
            "JOIN FETCH a.hospital h " +
            "JOIN FETCH a.serviceCategory s " +
            "WHERE a.appointmentId = :id")
    Optional<Appointment> findByIdWithDetails(@Param("id") Long id);

    long countByDoctorStaffIdAndAppointmentDateTimeBetween(Long doctorId, LocalDateTime startInclusive, LocalDateTime endInclusive);
}
