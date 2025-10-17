package com.lankamed.health.backend.model.patient;

import com.lankamed.health.backend.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "patients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Convert(converter = GenderConverter.class)
    private Gender gender;

    @Size(max = 20)
    @Column(name = "contact_number")
    private String contactNumber;

    @Size(max = 255)
    private String address;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "patient_id")
    private User user;

    public enum Gender {
        MALE, FEMALE, OTHER
    }
}
