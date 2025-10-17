package com.lankamed.health.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lankamed.health.backend.dto.PaymentDTO;
import com.lankamed.health.backend.model.Appointment;
import com.lankamed.health.backend.model.Payment;
import com.lankamed.health.backend.model.PaymentStatus;
import com.lankamed.health.backend.model.PaymentType;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.AppointmentRepository;
import com.lankamed.health.backend.repository.PaymentRepository;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import com.lankamed.health.backend.strategy.PaymentStrategy;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final Map<String, PaymentStrategy> paymentStrategies;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, 
                            PatientRepository patientRepository,
                            AppointmentRepository appointmentRepository,
                            List<PaymentStrategy> strategies) {
        this.paymentRepository = paymentRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.paymentStrategies = strategies.stream()
                .collect(Collectors.toMap(
                        s -> s.getClass().getSimpleName().replace("Payment", ""),
                        Function.identity()));
    }

    @Override
    public PaymentDTO makePayment(PaymentDTO dto) {
        // Validate payment method
        PaymentStrategy strategy = paymentStrategies.get(dto.getPaymentMethod());
        if (strategy == null) {
            throw new IllegalArgumentException("Invalid payment method: " + dto.getPaymentMethod());
        }

        // Fetch patient from database
        Patient patient = patientRepository.findByPatientId(dto.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + dto.getPatientId()));

        // Fetch appointment from database if provided
        Appointment appointment = null;
        if (dto.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(dto.getAppointmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + dto.getAppointmentId()));
        }

        // Process payment using strategy pattern
        PaymentStatus status = strategy.processPayment(dto);

        // Create new payment entity
        Payment payment = new Payment();
        payment.setPatient(patient);
        payment.setAppointment(appointment);
        payment.setAmount(dto.getAmount());
        payment.setPaymentType(PaymentType.valueOf(dto.getPaymentMethod()));
        payment.setStatus(status);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setPaymentTimestamp(LocalDateTime.now());

        // Save payment to database
        payment = paymentRepository.save(payment);

        // Update DTO with generated values
        dto.setTransactionId(payment.getTransactionId());
        dto.setStatus(status.name());

        return dto;
    }

    @Override
    public List<PaymentDTO> getPendingPayments(Long patientId) {
        try {
            System.out.println("PaymentServiceImpl: Getting pending payments for patient ID: " + patientId);

            // First verify the patient exists - use alternative method if primary fails
            Optional<Patient> patientOpt = Optional.empty();

            try {
                patientOpt = patientRepository.findById(patientId);
            } catch (Exception e) {
                System.out.println("PaymentServiceImpl: Primary patient lookup failed: " + e.getMessage());
            }

            // If primary method fails, try alternative
            if (patientOpt.isEmpty()) {
                try {
                    System.out.println("PaymentServiceImpl: Trying alternative patient lookup...");
                    patientOpt = patientRepository.findByPatientId(patientId);
                } catch (Exception e) {
                    System.out.println("PaymentServiceImpl: Alternative patient lookup also failed: " + e.getMessage());
                }
            }

            if (patientOpt.isEmpty()) {
                System.out.println("PaymentServiceImpl: Patient not found with ID: " + patientId);
                // Instead of throwing an error, return empty list for non-existent patient
                return new ArrayList<>();
            }

            System.out.println("PaymentServiceImpl: Patient found, querying payments...");

            // Try multiple methods to find payments
            List<Payment> payments = new ArrayList<>();

            try {
                // Try the first method name
                payments = paymentRepository.findByPatient_PatientIdAndStatus(patientId, PaymentStatus.Pending);
                System.out.println("PaymentServiceImpl: First method found " + payments.size() + " payments");
            } catch (Exception e) {
                System.out.println("PaymentServiceImpl: First method failed: " + e.getMessage());
            }

            // If no results, try alternative method
            if (payments.isEmpty()) {
                try {
                    System.out.println("PaymentServiceImpl: Trying alternative method...");
                    payments = paymentRepository.findByPatientPatientIdAndStatus(patientId, PaymentStatus.Pending);
                    System.out.println("PaymentServiceImpl: Alternative method found " + payments.size() + " payments");
                } catch (Exception e) {
                    System.out.println("PaymentServiceImpl: Alternative method also failed: " + e.getMessage());
                }
            }

            // If still no results, try a more general query
            if (payments.isEmpty()) {
                try {
                    System.out.println("PaymentServiceImpl: Trying general query...");
                    List<Payment> allPayments = paymentRepository.findAll();
                    payments = allPayments.stream()
                        .filter(p -> patientId.equals(p.getPatient().getPatientId()) &&
                                     PaymentStatus.Pending.equals(p.getStatus()))
                        .collect(Collectors.toList());
                    System.out.println("PaymentServiceImpl: General query found " + payments.size() + " payments");
                } catch (Exception e) {
                    System.out.println("PaymentServiceImpl: General query also failed: " + e.getMessage());
                }
            }

            System.out.println("PaymentServiceImpl: Found " + payments.size() + " pending payments");

            // If no payments exist, return empty list instead of error
            if (payments.isEmpty()) {
                System.out.println("PaymentServiceImpl: No pending payments found, returning empty list");
                return new ArrayList<>();
            }

            return payments.stream()
                    .map(p -> {
                        try {
                            System.out.println("PaymentServiceImpl: Processing payment ID: " + p.getPaymentId());

                            PaymentDTO dto = new PaymentDTO(
                                    p.getPatient().getPatientId(),
                                    p.getAppointment() != null ? p.getAppointment().getAppointmentId() : null,
                                    p.getAmount(),
                                    p.getPaymentType() != null ? p.getPaymentType().name() : "Cash",
                                    p.getTransactionId(),
                                    p.getStatus() != null ? p.getStatus().name() : "Pending"
                            );

                            // Add appointment details if appointment exists
                            if (p.getAppointment() != null) {
                                try {
                                    Appointment appointment = p.getAppointment();
                                    System.out.println("PaymentServiceImpl: Processing appointment ID: " + appointment.getAppointmentId());
                                    System.out.println("PaymentServiceImpl: Appointment payment amount: " + appointment.getPaymentAmount());

                                    // Set appointment date and time
                                    if (appointment.getAppointmentDateTime() != null) {
                                        dto.setAppointmentDateTime(
                                            appointment.getAppointmentDateTime().toString()
                                        );
                                    }

                                    // Set doctor name
                                    if (appointment.getDoctor() != null &&
                                        appointment.getDoctor().getUser() != null) {
                                        String doctorName = appointment.getDoctor().getUser().getFirstName() + " " +
                                                           appointment.getDoctor().getUser().getLastName();
                                        dto.setDoctorName(doctorName);
                                    }

                                    // Set service name
                                    if (appointment.getServiceCategory() != null) {
                                        dto.setServiceName(appointment.getServiceCategory().getName());
                                    }

                                    // Set hospital name
                                    if (appointment.getHospital() != null) {
                                        dto.setHospitalName(appointment.getHospital().getName());
                                    }

                                    // Set appointment description
                                    if (appointment.getServiceCategory() != null &&
                                        appointment.getDoctor() != null &&
                                        appointment.getDoctor().getUser() != null) {
                                        String description = appointment.getServiceCategory().getName() + " with Dr. " +
                                                           appointment.getDoctor().getUser().getFirstName() + " " +
                                                           appointment.getDoctor().getUser().getLastName();
                                        dto.setAppointmentDescription(description);
                                    }

                                    // Ensure the payment amount from appointment is properly set in DTO
                                    if (appointment.getPaymentAmount() != null) {
                                        dto.setAmount(appointment.getPaymentAmount());
                                        System.out.println("PaymentServiceImpl: Updated DTO amount from appointment: " + appointment.getPaymentAmount());
                                    }
                                } catch (Exception e) {
                                    // Log the error but don't fail the entire operation
                                    System.err.println("Error processing appointment details for payment " + p.getPaymentId() + ": " + e.getMessage());
                                    // Continue without appointment details
                                }
                            }

                            System.out.println("PaymentServiceImpl: Successfully processed payment DTO");
                            return dto;
                        } catch (Exception e) {
                            System.err.println("Error processing individual payment " + p.getPaymentId() + ": " + e.getMessage());
                            // Return a basic DTO even if there are issues
                            return new PaymentDTO(
                                p.getPatient().getPatientId(),
                                p.getAppointment() != null ? p.getAppointment().getAppointmentId() : null,
                                p.getAmount(),
                                p.getPaymentType() != null ? p.getPaymentType().name() : "Cash",
                                p.getTransactionId(),
                                p.getStatus() != null ? p.getStatus().name() : "Pending"
                            );
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("PaymentServiceImpl: Error in getPendingPayments: " + e.getMessage());
            e.printStackTrace();
            // Instead of throwing an error, return empty list as fallback
            System.err.println("PaymentServiceImpl: Returning empty list due to error");
            return new ArrayList<>();
        }
    }

    // Helper method to create sample payment data for testing
    public void createSamplePaymentData(Long patientId) {
        try {
            System.out.println("PaymentServiceImpl: Creating sample payment data for patient ID: " + patientId);

            // Verify patient exists - try multiple methods
            Optional<Patient> patientOpt = Optional.empty();

            try {
                patientOpt = patientRepository.findById(patientId);
            } catch (Exception e) {
                System.out.println("PaymentServiceImpl: Primary patient lookup failed: " + e.getMessage());
            }

            if (patientOpt.isEmpty()) {
                try {
                    patientOpt = patientRepository.findByPatientId(patientId);
                } catch (Exception e) {
                    System.out.println("PaymentServiceImpl: Alternative patient lookup failed: " + e.getMessage());
                }
            }

            if (patientOpt.isEmpty()) {
                System.out.println("PaymentServiceImpl: Patient not found, cannot create sample data");
                return;
            }

            Patient patient = patientOpt.get();

            // Create a sample payment
            Payment samplePayment = new Payment();
            samplePayment.setPatient(patient);
            samplePayment.setAmount(1500.00);
            samplePayment.setPaymentType(PaymentType.Card);
            samplePayment.setStatus(PaymentStatus.Pending);
            samplePayment.setTransactionId("SAMPLE-" + UUID.randomUUID().toString().substring(0, 8));
            samplePayment.setPaymentTimestamp(LocalDateTime.now());

            paymentRepository.save(samplePayment);
            System.out.println("PaymentServiceImpl: Created sample payment with ID: " + samplePayment.getPaymentId());

        } catch (Exception e) {
            System.err.println("PaymentServiceImpl: Error creating sample payment data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to get all payments for debugging
    public List<PaymentDTO> getAllPaymentsForDebugging() {
        try {
            System.out.println("PaymentServiceImpl: Getting all payments for debugging...");

            List<Payment> allPayments = paymentRepository.findAll();
            System.out.println("PaymentServiceImpl: Found " + allPayments.size() + " total payments in database");

            return allPayments.stream()
                    .map(p -> {
                        try {
                            return new PaymentDTO(
                                    p.getPatient().getPatientId(),
                                    p.getAppointment() != null ? p.getAppointment().getAppointmentId() : null,
                                    p.getAmount(),
                                    p.getPaymentType() != null ? p.getPaymentType().name() : "Cash",
                                    p.getTransactionId(),
                                    p.getStatus() != null ? p.getStatus().name() : "Pending"
                            );
                        } catch (Exception e) {
                            System.err.println("Error processing payment for debugging: " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("PaymentServiceImpl: Error getting all payments for debugging: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Helper method to force create sample data for testing
    public void initializeSampleData() {
        try {
            System.out.println("PaymentServiceImpl: Initializing sample payment data...");

            // Get all patients
            List<Patient> patients = patientRepository.findAll();
            System.out.println("PaymentServiceImpl: Found " + patients.size() + " patients in database");

            if (patients.isEmpty()) {
                System.out.println("PaymentServiceImpl: No patients found, cannot create sample payments");
                return;
            }

            // Create sample payments for each patient
            for (Patient patient : patients) {
                try {
                    createSamplePaymentData(patient.getPatientId());
                    System.out.println("PaymentServiceImpl: Created sample payment for patient: " + patient.getPatientId());
                } catch (Exception e) {
                    System.err.println("PaymentServiceImpl: Failed to create sample payment for patient " + patient.getPatientId() + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("PaymentServiceImpl: Error initializing sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to check database health
    public String checkDatabaseHealth() {
        try {
            System.out.println("PaymentServiceImpl: Checking database health...");

            // Check if we can access the payment repository
            List<Payment> allPayments = paymentRepository.findAll();
            System.out.println("PaymentServiceImpl: Database contains " + allPayments.size() + " payments");

            // Check if we can access patients
            List<Patient> allPatients = patientRepository.findAll();
            System.out.println("PaymentServiceImpl: Database contains " + allPatients.size() + " patients");

            return String.format("Database Health: OK (Payments: %d, Patients: %d)", allPayments.size(), allPatients.size());

        } catch (Exception e) {
            System.err.println("PaymentServiceImpl: Database health check failed: " + e.getMessage());
            e.printStackTrace();
            return "Database Health: ERROR - " + e.getMessage();
        }
    }

    // Helper method to create a simple test payment without appointment dependencies
    public PaymentDTO createSimpleTestPayment(Long patientId) {
        try {
            System.out.println("PaymentServiceImpl: Creating simple test payment for patient ID: " + patientId);

            // Verify patient exists
            Optional<Patient> patientOpt = patientRepository.findById(patientId);
            if (patientOpt.isEmpty()) {
                System.out.println("PaymentServiceImpl: Patient not found, cannot create test payment");
                return null;
            }

            Patient patient = patientOpt.get();

            // Create a simple payment without appointment
            Payment testPayment = new Payment();
            testPayment.setPatient(patient);
            testPayment.setAmount(2500.00);
            testPayment.setPaymentType(PaymentType.Card);
            testPayment.setStatus(PaymentStatus.Pending);
            testPayment.setTransactionId("TEST-" + UUID.randomUUID().toString().substring(0, 8));
            testPayment.setPaymentTimestamp(LocalDateTime.now());

            testPayment = paymentRepository.save(testPayment);
            System.out.println("PaymentServiceImpl: Created test payment with ID: " + testPayment.getPaymentId());

            // Return DTO representation
            return new PaymentDTO(
                testPayment.getPatient().getPatientId(),
                null, // No appointment
                testPayment.getAmount(),
                testPayment.getPaymentType().name(),
                testPayment.getTransactionId(),
                testPayment.getStatus().name()
            );

        } catch (Exception e) {
            System.err.println("PaymentServiceImpl: Error creating test payment: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Helper method to get system status for debugging
    public String getSystemStatus() {
        try {
            System.out.println("PaymentServiceImpl: Getting system status...");

            StringBuilder status = new StringBuilder();
            status.append("=== PAYMENT SYSTEM STATUS ===\n");

            // Check database connectivity
            try {
                List<Payment> payments = paymentRepository.findAll();
                List<Patient> patients = patientRepository.findAll();
                status.append(String.format("Database: OK (Payments: %d, Patients: %d)\n", payments.size(), patients.size()));
            } catch (Exception e) {
                status.append("Database: ERROR - " + e.getMessage() + "\n");
            }

            // Check payment strategies
            try {
                status.append(String.format("Payment Strategies: %d registered\n", paymentStrategies.size()));
                paymentStrategies.keySet().forEach(strategy -> status.append("  - " + strategy + "\n"));
            } catch (Exception e) {
                status.append("Payment Strategies: ERROR - " + e.getMessage() + "\n");
            }

            // Check current time
            status.append("Current Time: " + LocalDateTime.now() + "\n");

            return status.toString();

        } catch (Exception e) {
            return "System Status: ERROR - " + e.getMessage();
        }
    }

    // Helper method to reset payment data for testing
    public void resetPaymentData() {
        try {
            System.out.println("PaymentServiceImpl: Resetting payment data...");

            // Delete all payments
            paymentRepository.deleteAll();
            System.out.println("PaymentServiceImpl: Deleted all payments");

            // Create fresh sample data
            initializeSampleData();
            System.out.println("PaymentServiceImpl: Reset complete");

        } catch (Exception e) {
            System.err.println("PaymentServiceImpl: Error resetting payment data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to get payment statistics for debugging
    public String getPaymentStatistics() {
        try {
            System.out.println("PaymentServiceImpl: Getting payment statistics...");

            List<Payment> allPayments = paymentRepository.findAll();
            List<Patient> allPatients = patientRepository.findAll();

            long pendingPayments = allPayments.stream()
                .filter(p -> PaymentStatus.Pending.equals(p.getStatus()))
                .count();

            long paidPayments = allPayments.stream()
                .filter(p -> PaymentStatus.Paid.equals(p.getStatus()))
                .count();

            long failedPayments = allPayments.stream()
                .filter(p -> PaymentStatus.Failed.equals(p.getStatus()))
                .count();

            return String.format(
                "Payment Stats: Total=%d (Pending=%d, Paid=%d, Failed=%d), Patients=%d",
                allPayments.size(), pendingPayments, paidPayments, failedPayments, allPatients.size()
            );

        } catch (Exception e) {
            System.err.println("PaymentServiceImpl: Error getting payment statistics: " + e.getMessage());
            e.printStackTrace();
            return "Payment Stats: ERROR - " + e.getMessage();
        }
    }

    // Helper method to force refresh and ensure data consistency
    public void refreshAndValidateData() {
        try {
            System.out.println("PaymentServiceImpl: Refreshing and validating data...");

            // Clear any cached data and reload
            List<Payment> payments = paymentRepository.findAll();
            List<Patient> patients = patientRepository.findAll();

            System.out.println("PaymentServiceImpl: Validated - Payments: " + payments.size() + ", Patients: " + patients.size());

            // Ensure each patient has at least one payment for testing
            for (Patient patient : patients) {
                boolean hasPayment = payments.stream()
                    .anyMatch(p -> patient.getPatientId().equals(p.getPatient().getPatientId()));

                if (!hasPayment) {
                    System.out.println("PaymentServiceImpl: Creating missing payment for patient: " + patient.getPatientId());
                    createSamplePaymentData(patient.getPatientId());
                }
            }

        } catch (Exception e) {
            System.err.println("PaymentServiceImpl: Error refreshing data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to create a test payment and return it as DTO
    public PaymentDTO createAndReturnTestPayment(Long patientId) {
        try {
            System.out.println("PaymentServiceImpl: Creating and returning test payment for patient ID: " + patientId);

            // First ensure patient exists
            Optional<Patient> patientOpt = patientRepository.findById(patientId);
            if (patientOpt.isEmpty()) {
                System.out.println("PaymentServiceImpl: Patient not found, trying alternative lookup...");
                patientOpt = patientRepository.findByPatientId(patientId);
            }

            if (patientOpt.isEmpty()) {
                System.out.println("PaymentServiceImpl: Patient not found, cannot create test payment");
                return null;
            }

            Patient patient = patientOpt.get();

            // Create test payment
            Payment testPayment = new Payment();
            testPayment.setPatient(patient);
            testPayment.setAmount(3000.00);
            testPayment.setPaymentType(PaymentType.Card);
            testPayment.setStatus(PaymentStatus.Pending);
            testPayment.setTransactionId("TEST-PAYMENT-" + System.currentTimeMillis());
            testPayment.setPaymentTimestamp(LocalDateTime.now());

            testPayment = paymentRepository.save(testPayment);
            System.out.println("PaymentServiceImpl: Created test payment with ID: " + testPayment.getPaymentId());

            // Return as DTO
            return new PaymentDTO(
                testPayment.getPatient().getPatientId(),
                testPayment.getAppointment() != null ? testPayment.getAppointment().getAppointmentId() : null,
                testPayment.getAmount(),
                testPayment.getPaymentType() != null ? testPayment.getPaymentType().name() : "Card",
                testPayment.getTransactionId(),
                testPayment.getStatus() != null ? testPayment.getStatus().name() : "Pending"
            );

        } catch (Exception e) {
            System.err.println("PaymentServiceImpl: Error creating test payment: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
