package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.DoctorDto;
import com.lankamed.health.backend.dto.DoctorProfileDto;
import com.lankamed.health.backend.repository.DoctorRepository;
import com.lankamed.health.backend.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        System.out.println("DoctorService: Searching doctors with name=" + name + ", specialization=" + specialization);
        
        List<com.lankamed.health.backend.model.StaffDetails> doctors;
        
        if (name != null || specialization != null) {
            doctors = doctorRepository.findDoctorsByNameAndSpecialization(name, specialization);
        } else {
            doctors = doctorRepository.findAllDoctors();
        }
        
        System.out.println("DoctorService: Found " + doctors.size() + " doctors");

        return doctors.stream()
                .map(doctor -> {
                    System.out.println("DoctorService: Processing doctor " + doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName());
                    DoctorDto dto = DoctorDto.fromStaffDetails(doctor);
                    if (dto == null) {
                        System.out.println("DoctorService: DTO is null for doctor " + doctor.getStaffId());
                        return null;
                    }
                    // Add rating information
                    Double averageRating = reviewRepository.findAverageRatingByDoctorId(doctor.getStaffId());
                    Long reviewCount = reviewRepository.countReviewsByDoctorId(doctor.getStaffId());
                    dto.setAverageRating(averageRating);
                    dto.setReviewCount(reviewCount);
                    return dto;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    public List<DoctorProfileDto> searchDoctorProfiles(String name, String specialization) {
        System.out.println("DoctorService: Searching doctor profiles with name=" + name + ", specialization=" + specialization);
        
        List<Object[]> results;
        
        if (name != null || specialization != null) {
            results = doctorRepository.findDoctorProfilesWithRatings(name, specialization);
        } else {
            results = doctorRepository.findAllDoctorProfilesWithRatings();
        }
        
        System.out.println("DoctorService: Found " + results.size() + " doctor profiles");

        return results.stream()
                .map(this::mapToDoctorProfileDto)
                .collect(Collectors.toList());
    }

    private DoctorProfileDto mapToDoctorProfileDto(Object[] row) {
        try {
            return DoctorProfileDto.builder()
                    .doctorId(((Number) row[0]).longValue())
                    .firstName((String) row[1])
                    .lastName((String) row[2])
                    .fullName((String) row[3])
                    .specialization((String) row[4])
                    .hospitalName((String) row[5])
                    .categoryName((String) row[6])
                    .averageRating(((BigDecimal) row[7]).doubleValue())
                    .reviewCount(((Number) row[8]).longValue())
                    .email((String) row[9])
                    .hospitalAddress((String) row[10])
                    .hospitalContact((String) row[11])
                    .build();
        } catch (Exception e) {
            System.err.println("Error mapping doctor profile: " + e.getMessage());
            return null;
        }
    }
}
