package com.lankamed.health.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emergency_contacts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emergency_contact_id")
    private Long emergencyContactId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Size(max = 100)
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Size(max = 50)
    @Column(name = "relationship", nullable = false)
    private String relationship;

    @Size(max = 20)
    @Column(name = "phone", nullable = false)
    private String phone;

    @Size(max = 120)
    @Column(name = "email")
    private String email;

    @Size(max = 255)
    @Column(name = "address")
    private String address;
}


