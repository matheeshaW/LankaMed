package com.lankamed.health.backend.dto;

import com.lankamed.health.backend.model.EmergencyContact;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmergencyContactDto {
    private Long emergencyContactId;
    private String fullName;
    private String relationship;
    private String phone;
    private String email;
    private String address;

    public static EmergencyContactDto fromEmergencyContact(EmergencyContact ec) {
        return EmergencyContactDto.builder()
                .emergencyContactId(ec.getEmergencyContactId())
                .fullName(ec.getFullName())
                .relationship(ec.getRelationship())
                .phone(ec.getPhone())
                .email(ec.getEmail())
                .address(ec.getAddress())
                .build();
    }
}


