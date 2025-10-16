package com.lankamed.health.backend.model;

import com.lankamed.health.backend.model.patient.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "waitlist_entries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Patient patient;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private StaffDetails doctor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Hospital hospital;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ServiceCategory serviceCategory;

    @Column(nullable = false)
    private LocalDateTime desiredDateTime;

    @Column(nullable = false)
    @Builder.Default
    private boolean priority = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.QUEUED;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public enum Status { QUEUED, NOTIFIED, PROMOTED, EXPIRED, APPROVED }
}
