package com.lankamed.health.backend.controller;

import com.lankamed.health.backend.dto.CreateReviewDto;
import com.lankamed.health.backend.dto.ReviewDto;
import com.lankamed.health.backend.service.ReviewService;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/me")
@CrossOrigin(origins = "http://localhost:3000")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews")
    public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody CreateReviewDto createReviewDto) {
        ReviewDto review = reviewService.createReview(createReviewDto);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/doctors/{doctorId}/reviews")
    public ResponseEntity<List<ReviewDto>> getDoctorReviews(@PathVariable Long doctorId) {
        List<ReviewDto> reviews = reviewService.getDoctorReviews(doctorId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/doctors/{doctorId}/review-stats")
    public ResponseEntity<?> getDoctorReviewStats(@PathVariable Long doctorId) {
        var stats = reviewService.getDoctorReviewStats(doctorId);
        return ResponseEntity.ok(stats);
    }
}
