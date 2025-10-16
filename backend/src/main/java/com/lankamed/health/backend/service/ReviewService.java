package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.CreateReviewDto;
import com.lankamed.health.backend.dto.ReviewDto;
import com.lankamed.health.backend.model.Appointment;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.model.Review;
import com.lankamed.health.backend.model.StaffDetails;
import com.lankamed.health.backend.repository.AppointmentRepository;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import com.lankamed.health.backend.repository.ReviewRepository;
import com.lankamed.health.backend.repository.StaffDetailsRepository;
import com.lankamed.health.backend.repository.UserRepository;
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
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, 
                        AppointmentRepository appointmentRepository,
                        PatientRepository patientRepository,
                        StaffDetailsRepository staffDetailsRepository,
                        UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.staffDetailsRepository = staffDetailsRepository;
        this.userRepository = userRepository;
    }

    public ReviewDto createReview(CreateReviewDto createReviewDto) {
        String email = getCurrentUserEmail();
        if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
            email = "john.doe@example.com"; // demo fallback user created in DataInitializer
        }
        final String finalEmail = email;
        Patient patient = patientRepository.findByUserEmail(finalEmail)
                .orElseGet(() -> {
                    // Create a minimal patient record for the fallback/demo user if missing
                    User user = userRepository.findByEmail(finalEmail).orElse(null);
                    if (user == null) {
                        throw new RuntimeException("User not found for email: " + finalEmail);
                    }
                    Patient newPatient = Patient.builder()
                            .user(user)
                            .dateOfBirth(java.time.LocalDate.of(1990, 1, 1))
                            .gender(Patient.Gender.OTHER)
                            .contactNumber("Not Provided")
                            .address("Not Provided")
                            .build();
                    return patientRepository.save(newPatient);
                });

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

    public java.util.Map<String, Object> getDoctorReviewStats(Long doctorId) {
        Double avg = reviewRepository.findAverageRatingByDoctorId(doctorId);
        Long count = reviewRepository.countReviewsByDoctorId(doctorId);
        double average = avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0;
        return java.util.Map.of(
                "doctorId", doctorId,
                "averageRating", average,
                "reviewCount", count == null ? 0 : count
        );
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;
        String name = authentication.getName();
        if (name == null || name.isBlank() || "anonymousUser".equals(name)) return null;
        return name;
    }
}
