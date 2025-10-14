package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.CreateReviewDto;
import com.lankamed.health.backend.dto.ReviewDto;
import com.lankamed.health.backend.model.Appointment;
import com.lankamed.health.backend.model.Patient;
import com.lankamed.health.backend.model.Review;
import com.lankamed.health.backend.model.StaffDetails;
import com.lankamed.health.backend.repository.AppointmentRepository;
import com.lankamed.health.backend.repository.PatientRepository;
import com.lankamed.health.backend.repository.ReviewRepository;
import com.lankamed.health.backend.repository.StaffDetailsRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final StaffDetailsRepository staffDetailsRepository;

    public ReviewService(ReviewRepository reviewRepository, 
                        AppointmentRepository appointmentRepository,
                        PatientRepository patientRepository,
                        StaffDetailsRepository staffDetailsRepository) {
        this.reviewRepository = reviewRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.staffDetailsRepository = staffDetailsRepository;
    }

    public ReviewDto createReview(CreateReviewDto createReviewDto) {
        String email = getCurrentUserEmail();
        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Appointment appointment = appointmentRepository.findById(createReviewDto.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Check if appointment belongs to the patient
        if (!appointment.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new RuntimeException("Appointment does not belong to the patient");
        }

        // Check if appointment is completed
        if (appointment.getStatus() != Appointment.Status.COMPLETED) {
            throw new RuntimeException("Can only review completed appointments");
        }

        // Check if review already exists
        if (reviewRepository.findByAppointmentAppointmentId(appointment.getAppointmentId()).isPresent()) {
            throw new RuntimeException("Review already exists for this appointment");
        }

        Review review = Review.builder()
                .appointment(appointment)
                .patient(patient)
                .doctor(appointment.getDoctor())
                .rating(createReviewDto.getRating())
                .comment(createReviewDto.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        return ReviewDto.fromReview(savedReview);
    }

    public List<ReviewDto> getDoctorReviews(Long doctorId) {
        return reviewRepository.findByDoctorStaffIdOrderByCreatedAtDesc(doctorId)
                .stream()
                .map(ReviewDto::fromReview)
                .collect(Collectors.toList());
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
