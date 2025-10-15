package com.lankamed.health.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "staff_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDetails {

    @Id
    @Column(name = "staff_id")
    private Long staffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_category_id", referencedColumnName = "category_id")
    private ServiceCategory serviceCategory;

    @Size(max = 100)
    private String specialization;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "staff_id")
    private User user;
}
