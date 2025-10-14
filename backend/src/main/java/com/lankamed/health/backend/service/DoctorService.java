package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.DoctorDto;
import com.lankamed.health.backend.repository.DoctorRepository;
import com.lankamed.health.backend.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final ReviewRepository reviewRepository;

    public DoctorService(DoctorRepository doctorRepository, ReviewRepository reviewRepository) {
        this.doctorRepository = doctorRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<DoctorDto> searchDoctors(String name, String specialization) {
        List<com.lankamed.health.backend.model.StaffDetails> doctors;
        
        if (name != null || specialization != null) {
            doctors = doctorRepository.findDoctorsByNameAndSpecialization(name, specialization);
        } else {
            doctors = doctorRepository.findAllDoctors();
        }

        return doctors.stream()
                .map(doctor -> {
                    DoctorDto dto = DoctorDto.fromStaffDetails(doctor);
                    // Add rating information
                    Double averageRating = reviewRepository.findAverageRatingByDoctorId(doctor.getStaffId());
                    Long reviewCount = reviewRepository.countReviewsByDoctorId(doctor.getStaffId());
                    dto.setAverageRating(averageRating);
                    dto.setReviewCount(reviewCount);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
