package com.lankamed.health.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileDto {
    private Long doctorId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String specialization;
    private String hospitalName;
    private String categoryName;
    private Double averageRating;
    private Long reviewCount;
    private String email;
    private String hospitalAddress;
    private String hospitalContact;
    
    // Additional fields for enhanced profile
    private String experience;
    private String education;
    private String bio;
    private String profileImage;
    private String consultationFee;
    private String availableDays;
    private String availableHours;
}


