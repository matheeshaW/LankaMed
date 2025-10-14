package com.lankamed.health.backend.dto;

import com.lankamed.health.backend.model.StaffDetails;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorDto {
    private Long doctorId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String specialization;
    private String hospitalName;
    private String serviceCategoryName;
    private Double averageRating;
    private Long reviewCount;

    public static DoctorDto fromStaffDetails(StaffDetails staffDetails) {
        return DoctorDto.builder()
                .doctorId(staffDetails.getStaffId())
                .firstName(staffDetails.getUser().getFirstName())
                .lastName(staffDetails.getUser().getLastName())
                .fullName(staffDetails.getUser().getFirstName() + " " + staffDetails.getUser().getLastName())
                .specialization(staffDetails.getSpecialization())
                .hospitalName(staffDetails.getHospital().getName())
                .serviceCategoryName(staffDetails.getServiceCategory() != null ? 
                    staffDetails.getServiceCategory().getName() : null)
                .build();
    }
}
