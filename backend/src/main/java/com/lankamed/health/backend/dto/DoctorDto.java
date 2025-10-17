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
        if (staffDetails == null) {
            System.out.println("DoctorDto: StaffDetails is null");
            return null;
        }
        
        if (staffDetails.getUser() == null) {
            System.out.println("DoctorDto: User is null for staffId: " + staffDetails.getStaffId());
            return null;
        }
        
        if (staffDetails.getHospital() == null) {
            System.out.println("DoctorDto: Hospital is null for staffId: " + staffDetails.getStaffId());
            return null;
        }
        
        System.out.println("DoctorDto: Creating DTO for " + staffDetails.getUser().getFirstName() + " " + staffDetails.getUser().getLastName());
        
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
