package com.lankamed.health.backend.repository;

import com.lankamed.health.backend.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientPatientIdOrderByAppointmentDateTimeDesc(Long patientId);
    List<Appointment> findByPatientUserEmailOrderByAppointmentDateTimeDesc(String email);
    List<Appointment> findByDoctorStaffIdOrderByAppointmentDateTimeDesc(Long doctorId);
    List<Appointment> findByHospitalHospitalIdOrderByAppointmentDateTimeDesc(Long hospitalId);
    
    @Query("SELECT a FROM Appointment a WHERE a.patient.user.email = :email AND a.appointmentDateTime >= :startDate ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findUpcomingAppointmentsByPatientEmail(@Param("email") String email, @Param("startDate") LocalDateTime startDate);
}
