package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.AppointmentDto;
import com.lankamed.health.backend.dto.CreateAppointmentDto;
import com.lankamed.health.backend.dto.UpdateAppointmentStatusDto;
import com.lankamed.health.backend.model.Appointment;
import com.lankamed.health.backend.model.Hospital;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.model.ServiceCategory;
import com.lankamed.health.backend.model.StaffDetails;
import com.lankamed.health.backend.repository.AppointmentRepository;
import com.lankamed.health.backend.repository.HospitalRepository;
import com.lankamed.health.backend.repository.PaymentRepository;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import com.lankamed.health.backend.repository.ServiceCategoryRepository;
import com.lankamed.health.backend.repository.StaffDetailsRepository;
import com.lankamed.health.backend.repository.UserRepository;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.model.Role;
import com.lankamed.health.backend.model.Payment;
import com.lankamed.health.backend.model.PaymentType;
import com.lankamed.health.backend.model.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final StaffDetailsRepository staffDetailsRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    // Extracted collaborators to follow SRP/DIP while keeping behavior
    private final CurrentUserEmailProvider currentUserEmailProvider;
    private final DoctorSelectionPolicy doctorSelectionPolicy;
    private final AppointmentFactory appointmentFactory;

    public AppointmentService(AppointmentRepository appointmentRepository,
                            PatientRepository patientRepository,
                            HospitalRepository hospitalRepository,
                            ServiceCategoryRepository serviceCategoryRepository,
                            StaffDetailsRepository staffDetailsRepository,
                            UserRepository userRepository,
                            PaymentRepository paymentRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.staffDetailsRepository = staffDetailsRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;

        // Default implementations preserve existing behavior
        this.currentUserEmailProvider = new SecurityContextCurrentUserEmailProvider();
        this.doctorSelectionPolicy = new DefaultDoctorSelectionPolicy(staffDetailsRepository, userRepository);
        this.appointmentFactory = new DefaultAppointmentFactory();
    }

    public List<AppointmentDto> getPatientAppointments() {
        String email = currentUserEmailProvider.getCurrentUserEmail();
        if (email == null || email.isEmpty() || "anonymousUser".equals(email)) {
            // No auth context: return all so the UI shows newly created records during demos
            return appointmentRepository.findAll()
                    .stream()
                    .map(AppointmentDto::fromAppointment)
                    .collect(Collectors.toList());
        }
        return appointmentRepository.findByPatientUserEmailOrderByAppointmentDateTimeDesc(email)
                .stream()
                .map(AppointmentDto::fromAppointment)
                .collect(Collectors.toList());
    }

    public AppointmentDto createAppointment(CreateAppointmentDto createAppointmentDto) {
        String email = currentUserEmailProvider.getCurrentUserEmail();
        
        // If no authenticated user or anonymous user, return an error since we need a valid user
        if (email == null || email.isEmpty() || "anonymousUser".equals(email)) {
            // For development purposes, allow using a default test user if it exists
            email = "test@example.com";
        }
        
        // Make email final for lambda
        final String finalEmail = email;
        logger.info("AppointmentService: Creating appointment for email: {}", finalEmail);
        
        Patient patient = patientRepository.findByUserEmail(finalEmail)
                .orElseGet(() -> {
                    logger.info("AppointmentService: No patient record found, creating one for email: {}", finalEmail);
                    // Find the user first
                    User user = userRepository.findByEmail(finalEmail)
                            .orElseGet(() -> {
                                // Create user if doesn't exist (for development)
                                logger.info("AppointmentService: Creating user for email: {}", finalEmail);
                                User newUser = User.builder()
                                        .firstName(finalEmail.split("@")[0])
                                        .lastName("Patient")
                                        .email(finalEmail)
                                        .passwordHash("$2a$10$dummyHashedPasswordForDevelopment")
                                        .role(Role.PATIENT)
                                        .createdAt(Instant.now())
                                        .build();
                                return userRepository.save(newUser);
                            });

                    // Create a new patient record
                    Patient newPatient = Patient.builder()
                            .user(user)
                            .dateOfBirth(java.time.LocalDate.of(1990, 1, 1)) // Default date
                            .gender(Patient.Gender.OTHER) // Default gender
                            .contactNumber("Not Provided")
                            .address("Not Provided")
                            .build();

                    return patientRepository.save(newPatient);
                });

        // Get existing entities or create them
        Hospital hospital = hospitalRepository.findById(createAppointmentDto.getHospitalId())
                .orElseGet(() -> hospitalRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("No hospitals configured. Please add a hospital.")));

        ServiceCategory serviceCategory = serviceCategoryRepository.findById(createAppointmentDto.getServiceCategoryId())
                .orElseGet(() -> serviceCategoryRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("No service categories configured. Please add a category.")));

        // Resolve doctor according to existing selection and fallback policy
        StaffDetails doctor = doctorSelectionPolicy.resolveDoctor(createAppointmentDto, hospital, serviceCategory);

        // If any entity is missing, throw error
        // All entities guaranteed by above guards
        Appointment appointment = appointmentFactory.create(createAppointmentDto, patient, doctor, hospital, serviceCategory);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return AppointmentDto.fromAppointment(savedAppointment);
    }

    public List<AppointmentDto> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(AppointmentDto::fromAppointment)
                .collect(Collectors.toList());
    }

    public AppointmentDto updateAppointmentStatus(Long appointmentId, UpdateAppointmentStatusDto updateDto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Store the previous status to check if it's being confirmed
        Appointment.Status previousStatus = appointment.getStatus();

        appointment.setStatus(updateDto.getStatus());
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // If appointment is being confirmed and wasn't confirmed before, create a pending payment
        if (updateDto.getStatus() == Appointment.Status.CONFIRMED &&
            previousStatus != Appointment.Status.CONFIRMED) {
            createPendingPaymentForConfirmedAppointment(savedAppointment);
        }

        return AppointmentDto.fromAppointment(savedAppointment);
    }

    private String getCurrentUserEmail() {
        // Backward-compatible private method retained; now delegates to provider
        return currentUserEmailProvider.getCurrentUserEmail();
    }

    private void createPendingPaymentForConfirmedAppointment(Appointment appointment) {
        try {
            logger.info("Creating pending payment for confirmed appointment ID: {}", appointment.getAppointmentId());

            // Create a new payment record for the confirmed appointment
            Payment payment = new Payment();
            payment.setPatient(appointment.getPatient());
            payment.setAppointment(appointment);

            // Use the appointment's existing payment amount instead of recalculating
            Double paymentAmount = appointment.getPaymentAmount();
            if (paymentAmount == null || paymentAmount <= 0) {
                // Fallback to doctor's consultation fee if appointment payment amount is not set
                paymentAmount = appointment.getDoctor().getConsultationFee();
                if (paymentAmount == null || paymentAmount <= 0) {
                    paymentAmount = 1500.00; // Default consultation fee
                }
                // Update appointment with the calculated amount for consistency
                appointment.setPaymentAmount(paymentAmount);
                appointmentRepository.save(appointment);
                logger.warn("Appointment payment amount not set for appointment ID: {}, calculated from doctor fee: {}",
                           appointment.getAppointmentId(), paymentAmount);
            }

            payment.setAmount(paymentAmount);
            payment.setPaymentType(PaymentType.Card); // Default payment type
            payment.setStatus(PaymentStatus.Pending);
            payment.setTransactionId("APPT-" + appointment.getAppointmentId() + "-" + UUID.randomUUID().toString().substring(0, 8));
            payment.setPaymentTimestamp(LocalDateTime.now());

            paymentRepository.save(payment);
            logger.info("Successfully created pending payment for appointment ID: {} with amount: {}",
                       appointment.getAppointmentId(), paymentAmount);

        } catch (Exception e) {
            logger.error("Error creating pending payment for confirmed appointment ID: " + appointment.getAppointmentId(), e);
            // Don't throw exception to avoid breaking the appointment confirmation flow
        }
    }

    // ===== Collaborator abstractions and defaults (package-private for test visibility if needed) =====

    interface CurrentUserEmailProvider {
        String getCurrentUserEmail();
    }

    static class SecurityContextCurrentUserEmailProvider implements CurrentUserEmailProvider {
        @Override
        public String getCurrentUserEmail() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) return null;
            String name = authentication.getName();
            if (name == null || name.isBlank() || "anonymousUser".equals(name)) return null;
            return name;
        }
    }

    interface DoctorSelectionPolicy {
        StaffDetails resolveDoctor(CreateAppointmentDto createAppointmentDto, Hospital hospital, ServiceCategory serviceCategory);
    }

    static class DefaultDoctorSelectionPolicy implements DoctorSelectionPolicy {
        private final StaffDetailsRepository staffDetailsRepository;
        private final UserRepository userRepository;

        DefaultDoctorSelectionPolicy(StaffDetailsRepository staffDetailsRepository, UserRepository userRepository) {
            this.staffDetailsRepository = staffDetailsRepository;
            this.userRepository = userRepository;
        }

        @Override
        public StaffDetails resolveDoctor(CreateAppointmentDto createAppointmentDto, Hospital hospital, ServiceCategory serviceCategory) {
            return staffDetailsRepository.findById(createAppointmentDto.getDoctorId())
                    .orElseGet(() -> {
                        List<StaffDetails> byCat = staffDetailsRepository.findByServiceCategoryCategoryId(serviceCategory.getCategoryId());
                        if (!byCat.isEmpty()) return byCat.get(0);
                        List<StaffDetails> byHospital = staffDetailsRepository.findByHospitalHospitalId(hospital.getHospitalId());
                        if (!byHospital.isEmpty()) return byHospital.get(0);
                        List<StaffDetails> any = staffDetailsRepository.findAll();
                        if (!any.isEmpty()) return any.get(0);
                        // As a last resort, create a lightweight placeholder doctor bound to an existing user or a new one
                        User placeholderUser = userRepository.findByEmail("placeholder.doctor@lankamed.com")
                                .orElseGet(() -> userRepository.save(User.builder()
                                        .firstName("Placeholder")
                                        .lastName("Doctor")
                                        .email("placeholder.doctor@lankamed.com")
                                        .passwordHash("$2a$10$V3ryStr0ngHashForPlaceholderUserxxxxxxxxxxxxxx")
                                        .role(Role.DOCTOR)
                                        .build()));
                        StaffDetails placeholder = StaffDetails.builder()
                                .user(placeholderUser)
                                .hospital(hospital)
                                .serviceCategory(serviceCategory)
                                .specialization(serviceCategory.getName())
                                .build();
                        return staffDetailsRepository.save(placeholder);
                    });
        }
    }

    interface AppointmentFactory {
        Appointment create(CreateAppointmentDto dto, Patient patient, StaffDetails doctor, Hospital hospital, ServiceCategory serviceCategory);
    }

    static class DefaultAppointmentFactory implements AppointmentFactory {
        @Override
        public Appointment create(CreateAppointmentDto dto, Patient patient, StaffDetails doctor, Hospital hospital, ServiceCategory serviceCategory) {
            // Calculate payment amount based on doctor's consultation fee
            Double paymentAmount = doctor.getConsultationFee();
            if (paymentAmount == null || paymentAmount <= 0) {
                paymentAmount = 1500.00; // Default consultation fee
            }

            System.out.println("AppointmentFactory: Creating appointment with payment amount: " + paymentAmount);
            System.out.println("AppointmentFactory: Doctor consultation fee: " + doctor.getConsultationFee());
            System.out.println("AppointmentFactory: Doctor ID: " + doctor.getStaffId());

            return Appointment.builder()
                    .patient(patient)
                    .doctor(doctor)
                    .hospital(hospital)
                    .serviceCategory(serviceCategory)
                    .appointmentDateTime(dto.getAppointmentDateTime())
                    .status(dto.isPriority() ? Appointment.Status.CONFIRMED : Appointment.Status.PENDING)
                    .priority(dto.isPriority())
                    .paymentAmount(paymentAmount)
                    .build();
        }
    }
}