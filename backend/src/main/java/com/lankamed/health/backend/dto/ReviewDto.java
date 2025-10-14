package com.lankamed.health.backend.dto;

import com.lankamed.health.backend.model.Review;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ReviewDto {
    private Long reviewId;
    private Long appointmentId;
    private String doctorName;
    private String patientName;
    private Integer rating;
    private String comment;
    private Instant createdAt;

    public static ReviewDto fromReview(Review review) {
        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .appointmentId(review.getAppointment().getAppointmentId())
                .doctorName(review.getDoctor().getUser().getFirstName() + " " + 
                           review.getDoctor().getUser().getLastName())
                .patientName(review.getPatient().getUser().getFirstName() + " " + 
                            review.getPatient().getUser().getLastName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
