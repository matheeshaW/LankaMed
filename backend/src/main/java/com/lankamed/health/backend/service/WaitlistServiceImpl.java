package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.CreateWaitlistDto;
import com.lankamed.health.backend.dto.WaitlistEntryDto;
import com.lankamed.health.backend.model.*;
import com.lankamed.health.backend.repository.*;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import com.lankamed.health.backend.model.patient.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class WaitlistServiceImpl implements WaitlistService {
    private static final Logger logger = LoggerFactory.getLogger(WaitlistServiceImpl.class);

    private final WaitlistRepository waitlistRepository;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final StaffDetailsRepository staffDetailsRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final CurrentUserEmailProvider currentUserEmailProvider;
    private final boolean waitlistEnabled;

    public WaitlistServiceImpl(
            WaitlistRepository waitlistRepository,
            PatientRepository patientRepository,
            HospitalRepository hospitalRepository,
            ServiceCategoryRepository serviceCategoryRepository,
            StaffDetailsRepository staffDetailsRepository,
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            @Value("${feature.waitlist.enabled:false}") boolean waitlistEnabled) {
        this.waitlistRepository = waitlistRepository;
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.staffDetailsRepository = staffDetailsRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.currentUserEmailProvider = new SecurityContextCurrentUserEmailProvider();
        this.waitlistEnabled = waitlistEnabled;
    }

    @Override
    public WaitlistEntryDto addToWaitlist(CreateWaitlistDto dto) {
        if (!waitlistEnabled) {
            throw new UnsupportedOperationException("Waitlist feature is disabled");
        }

        String email = dto.getPatientEmail() != null && !dto.getPatientEmail().isBlank()
                ? dto.getPatientEmail()
                : currentUserEmailProvider.getCurrentUserEmail();
        if (email == null || email.isEmpty() || "anonymousUser".equals(email)) {
            email = "john.doe@example.com"; // Fallback for demo
        }
        final String finalEmail = email;
        logger.info("WaitlistService: Adding to waitlist for email: {}", finalEmail);

        Patient patient = patientRepository.findByUserEmail(finalEmail)
                .orElseGet(() -> {
                    logger.info("WaitlistService: No patient record found, creating one for email: {}", finalEmail);
                    User user = userRepository.findByEmail(finalEmail)
                            .orElseThrow(() -> new RuntimeException("User not found for email: " + finalEmail));
                    Patient newPatient = Patient.builder()
                            .user(user)
                            .dateOfBirth(LocalDate.of(1990, 1, 1))
                            .gender(Patient.Gender.OTHER)
                            .contactNumber("Not Provided")
                            .address("Not Provided")
                            .build();
                    return patientRepository.save(newPatient);
                });

        Hospital hospital = dto.getHospitalId() != null
                ? hospitalRepository.findById(dto.getHospitalId()).orElseGet(() -> hospitalRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No hospitals configured. Please add a hospital.")))
                : hospitalRepository.findAll().stream().findFirst().orElseThrow(() -> new RuntimeException("No hospitals configured. Please add a hospital."));

        ServiceCategory serviceCategory = dto.getServiceCategoryId() != null
                ? serviceCategoryRepository.findById(dto.getServiceCategoryId()).orElseGet(() -> serviceCategoryRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No service categories configured. Please add a category.")))
                : serviceCategoryRepository.findAll().stream().findFirst().orElseThrow(() -> new RuntimeException("No service categories configured. Please add a category."));

        StaffDetails doctor = null;
        if (dto.getDoctorId() != null) {
            doctor = staffDetailsRepository.findById(dto.getDoctorId()).orElse(null);
        }
        if (doctor == null) {
            doctor = staffDetailsRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No doctors configured. Please add a doctor."));
        }

        WaitlistEntry entry = WaitlistEntry.builder()
                .patient(patient)
                .doctor(doctor)
                .hospital(hospital)
                .serviceCategory(serviceCategory)
                .desiredDateTime(dto.getDesiredDateTime())
                .priority(dto.isPriority())
                .build();

        WaitlistEntry saved = waitlistRepository.save(entry);
        return WaitlistEntryDto.fromWaitlistEntry(saved);
    }

    @Override
    public List<WaitlistEntryDto> getMyWaitlist() {
        if (!waitlistEnabled) {
            return List.of();
        }

        String email = currentUserEmailProvider.getCurrentUserEmail();
        if (email == null || email.isEmpty() || "anonymousUser".equals(email)) {
            email = "john.doe@example.com"; // Fallback to demo email to match waitlist creation
        }

        return waitlistRepository.findByPatientUserEmailAndStatusNotOrderByCreatedAtDesc(email, WaitlistEntry.Status.PROMOTED)
                .stream()
                .map(WaitlistEntryDto::fromWaitlistEntry)
                .collect(Collectors.toList());
    }

    @Override
    public List<WaitlistEntryDto> listAllQueued() {
        if (!waitlistEnabled) return List.of();
        return waitlistRepository.findByStatusOrderByCreatedAtAsc(WaitlistEntry.Status.QUEUED)
                .stream().map(WaitlistEntryDto::fromWaitlistEntry).collect(Collectors.toList());
    }

    @Override
    public List<WaitlistEntryDto> listQueuedByDoctor(Long doctorId) {
        if (!waitlistEnabled) return List.of();
        return waitlistRepository.findByDoctorStaffIdAndStatusOrderByCreatedAtAsc(doctorId, WaitlistEntry.Status.QUEUED)
                .stream().map(WaitlistEntryDto::fromWaitlistEntry).collect(Collectors.toList());
    }

    @Override
    public WaitlistEntryDto promoteToAppointment(Long waitlistId) {
        if (!waitlistEnabled) {
            throw new UnsupportedOperationException("Waitlist feature is disabled");
        }

        WaitlistEntry entry = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new RuntimeException("Waitlist entry not found: " + waitlistId));

        if (entry.getStatus() != WaitlistEntry.Status.QUEUED) {
            throw new IllegalStateException("Waitlist entry already processed: " + entry.getStatus());
        }

        LocalDateTime start = entry.getDesiredDateTime().minusMinutes(15);
        LocalDateTime end = entry.getDesiredDateTime().plusMinutes(15);
        List<Appointment> conflicts = appointmentRepository.findByDoctorStaffIdOrderByAppointmentDateTimeDesc(entry.getDoctor().getStaffId())
                .stream()
                .filter(a -> !a.getAppointmentDateTime().isBefore(start) && !a.getAppointmentDateTime().isAfter(end))
                .collect(Collectors.toList());

        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("No slot available at requested time for doctor: " + entry.getDoctor().getStaffId());
        }

        Appointment appointment = Appointment.builder()
                .patient(entry.getPatient())
                .doctor(entry.getDoctor())
                .hospital(entry.getHospital())
                .serviceCategory(entry.getServiceCategory())
                .appointmentDateTime(entry.getDesiredDateTime())
                .status(entry.isPriority() ? Appointment.Status.CONFIRMED : Appointment.Status.PENDING)
                .priority(entry.isPriority())
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        entry.setStatus(WaitlistEntry.Status.PROMOTED);
        waitlistRepository.save(entry);

        logger.info("Waitlist entry {} promoted to appointment {}", waitlistId, savedAppointment.getAppointmentId());
        return WaitlistEntryDto.fromWaitlistEntry(entry);
    }

    @Override
    public WaitlistEntryDto updateWaitlistStatus(Long waitlistId, String newStatus) {
        if (!waitlistEnabled) {
            throw new UnsupportedOperationException("Waitlist feature is disabled");
        }

        WaitlistEntry entry = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new RuntimeException("Waitlist entry not found: " + waitlistId));

        try {
            WaitlistEntry.Status status = WaitlistEntry.Status.valueOf(newStatus.toUpperCase());
            entry.setStatus(status);
            WaitlistEntry saved = waitlistRepository.save(entry);
            
            logger.info("Waitlist entry {} status updated to {}", waitlistId, newStatus);
            return WaitlistEntryDto.fromWaitlistEntry(saved);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + newStatus);
        }
    }

    @Override
    public List<WaitlistEntryDto> getAllQueuedWaitlistEntries() {
        if (!waitlistEnabled) {
            return List.of();
        }
        return waitlistRepository.findByStatusOrderByCreatedAtAsc(WaitlistEntry.Status.QUEUED)
                .stream()
                .map(WaitlistEntryDto::fromWaitlistEntry)
                .collect(Collectors.toList());
    }

    @Override
    public List<WaitlistEntryDto> getQueuedWaitlistEntriesByDoctor(Long doctorId) {
        if (!waitlistEnabled) {
            return List.of();
        }
        return waitlistRepository.findByDoctorStaffIdAndStatusOrderByCreatedAtAsc(doctorId, WaitlistEntry.Status.QUEUED)
                .stream()
                .map(WaitlistEntryDto::fromWaitlistEntry)
                .collect(Collectors.toList());
    }

    @Override
    public List<WaitlistEntryDto> getAllActiveWaitlistEntries() {
        if (!waitlistEnabled) {
            return List.of();
        }
        return waitlistRepository.findByStatusNotOrderByCreatedAtAsc(WaitlistEntry.Status.PROMOTED)
                .stream()
                .map(WaitlistEntryDto::fromWaitlistEntry)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WaitlistEntryDto> promoteNextToAppointment(Long doctorId) {
        if (!waitlistEnabled) {
            throw new UnsupportedOperationException("Waitlist feature is disabled");
        }

        return waitlistRepository.findByDoctorStaffIdAndStatusOrderByCreatedAtAsc(doctorId, WaitlistEntry.Status.QUEUED)
                .stream()
                .findFirst()
                .map(entry -> {
                    try {
                        return promoteToAppointment(entry.getId());
                    } catch (IllegalStateException e) {
                        logger.warn("Failed to promote waitlist entry {} for doctor {}: {}", entry.getId(), doctorId, e.getMessage());
                        return null; // Or handle differently, e.g., mark as NOTIFIED and try next
                    }
                });
    }

}
