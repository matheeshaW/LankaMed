package com.lankamed.health.backend.config;

import com.lankamed.health.backend.model.*;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.*;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Value("${app.data.initialize:true}")
    private boolean shouldInitializeData;

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final StaffDetailsRepository staffDetailsRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, 
                          HospitalRepository hospitalRepository,
                          ServiceCategoryRepository serviceCategoryRepository,
                          StaffDetailsRepository staffDetailsRepository,
                          PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.staffDetailsRepository = staffDetailsRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            System.out.println("DataInitializer: Starting initialization check...");
            System.out.println("DataInitializer: Data initialization enabled: " + shouldInitializeData);
            
            if (!shouldInitializeData) {
                System.out.println("DataInitializer: Data initialization is disabled, skipping...");
                ensureCoreEntities();
                return;
            }
            
            // Add a small delay to ensure database is fully ready
            Thread.sleep(2000);
            
            long userCount = userRepository.count();
            System.out.println("DataInitializer: Current user count: " + userCount);
            
            // Check if any doctors exist
            long doctorCount = staffDetailsRepository.count();
            System.out.println("DataInitializer: Current doctor count: " + doctorCount);
            
            // Only create sample data if database is empty (first time setup)
            if (userCount == 0 && doctorCount == 0) {
                System.out.println("DataInitializer: Database is empty, creating initial sample data...");
                createSampleData();
            } else if (doctorCount == 0) {
                System.out.println("DataInitializer: No doctors found, creating sample doctors...");
                createSampleDoctors();
            } else {
                System.out.println("DataInitializer: Database already contains data, skipping initialization.");
                System.out.println("DataInitializer: Existing users: " + userCount + ", doctors: " + doctorCount);
            }
            // Always ensure core entities exist even if we skipped creating full sample data
            ensureCoreEntities();
            
        } catch (Exception e) {
            System.err.println("DataInitializer: Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ensureCoreEntities() {
        try {
            if (hospitalRepository.count() == 0) {
                System.out.println("DataInitializer: No hospitals found. Creating a default hospital...");
                Hospital defaultHospital = Hospital.builder()
                        .name("City General Hospital")
                        .address("123 Main Street, Colombo 03")
                        .contactNumber("+94-11-234-5678")
                        .createdAt(Instant.now())
                        .build();
                hospitalRepository.save(defaultHospital);
            }

            if (serviceCategoryRepository.count() == 0) {
                System.out.println("DataInitializer: No service categories found. Creating a default category...");
                ServiceCategory defaultCategory = ServiceCategory.builder()
                        .name("General Medicine")
                        .description("General medical consultation")
                        .build();
                serviceCategoryRepository.save(defaultCategory);
            }
        } catch (Exception e) {
            System.err.println("DataInitializer: Failed ensuring core entities: " + e.getMessage());
        }
    }
    
    private void createSampleData() {
        try {
            System.out.println("DataInitializer: Creating comprehensive dummy data...");
            
            // Create hospitals
            Hospital hospital1 = Hospital.builder()
                    .name("City General Hospital")
                    .address("123 Main Street, Colombo 03")
                    .contactNumber("+94-11-234-5678")
                    .createdAt(Instant.now())
                    .build();
            hospital1 = hospitalRepository.save(hospital1);

            Hospital hospital2 = Hospital.builder()
                    .name("Central Medical Center")
                    .address("456 Health Avenue, Kandy")
                    .contactNumber("+94-81-234-5678")
                    .createdAt(Instant.now())
                    .build();
            hospital2 = hospitalRepository.save(hospital2);

            Hospital hospital3 = Hospital.builder()
                    .name("LankaMed Specialized Clinic")
                    .address("789 Medical Complex, Galle")
                    .contactNumber("+94-91-234-5678")
                    .createdAt(Instant.now())
                    .build();
            hospital3 = hospitalRepository.save(hospital3);

            // Create service categories
            ServiceCategory cardiology = ServiceCategory.builder()
                    .name("Cardiology")
                    .description("Heart and cardiovascular system treatment")
                    .build();
            cardiology = serviceCategoryRepository.save(cardiology);

            ServiceCategory dermatology = ServiceCategory.builder()
                    .name("Dermatology")
                    .description("Skin, hair, and nail treatment")
                    .build();
            dermatology = serviceCategoryRepository.save(dermatology);

            ServiceCategory pediatrics = ServiceCategory.builder()
                    .name("Pediatrics")
                    .description("Medical care for infants, children, and adolescents")
                    .build();
            pediatrics = serviceCategoryRepository.save(pediatrics);

            ServiceCategory neurology = ServiceCategory.builder()
                    .name("Neurology")
                    .description("Nervous system disorders treatment")
                    .build();
            neurology = serviceCategoryRepository.save(neurology);

            ServiceCategory orthopedics = ServiceCategory.builder()
                    .name("Orthopedics")
                    .description("Bone, joint, and muscle treatment")
                    .build();
            orthopedics = serviceCategoryRepository.save(orthopedics);

            // Create admin user
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("User")
                    .email("admin@lankamed.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .createdAt(Instant.now())
                    .build();
            userRepository.save(admin);

            // Create multiple doctors
            createDoctor("Dr. Sarah", "Johnson", "sarah.johnson@lankamed.com", "Cardiologist", hospital1, cardiology);
            createDoctor("Dr. Michael", "Chen", "michael.chen@lankamed.com", "Dermatologist", hospital1, dermatology);
            createDoctor("Dr. Priya", "Fernando", "priya.fernando@lankamed.com", "Pediatrician", hospital2, pediatrics);
            createDoctor("Dr. David", "Rodrigo", "david.rodrigo@lankamed.com", "Neurologist", hospital2, neurology);
            createDoctor("Dr. James", "Wilson", "james.wilson@lankamed.com", "Orthopedic Surgeon", hospital3, orthopedics);
            createDoctor("Dr. Maria", "Silva", "maria.silva@lankamed.com", "Cardiologist", hospital3, cardiology);
            createDoctor("Dr. Ahmed", "Hassan", "ahmed.hassan@lankamed.com", "Dermatologist", hospital2, dermatology);
            createDoctor("Dr. Lisa", "Perera", "lisa.perera@lankamed.com", "Pediatrician", hospital1, pediatrics);

            // Create sample patients
            User patient1 = createPatient("John", "Doe", "john.doe@example.com", "patient123");
            User patient2 = createPatient("Jane", "Smith", "jane.smith@example.com", "patient123");
            User patient3 = createPatient("Robert", "Brown", "robert.brown@example.com", "patient123");
            User patient4 = createPatient("Emily", "Davis", "emily.davis@example.com", "patient123");

            // Create test user for development
            User testUser = createPatient("Test", "User", "test@example.com", "test123");

            // Create sample appointments
            createSampleAppointments();

            System.out.println("DataInitializer: Dummy data created successfully!");
            System.out.println("DataInitializer: Created 8 doctors, 4 patients, 3 hospitals, 5 categories, and sample appointments");
        } catch (Exception e) {
            System.err.println("DataInitializer: Error creating sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createSampleDoctors() {
        try {
            System.out.println("DataInitializer: Creating sample doctors...");

            // Get existing hospitals and categories
            List<Hospital> hospitals = hospitalRepository.findAll();
            List<ServiceCategory> categories = serviceCategoryRepository.findAll();

            if (hospitals.isEmpty() || categories.isEmpty()) {
                System.out.println("DataInitializer: Cannot create doctors - missing hospitals or categories");
                return;
            }

            // Create multiple doctors with different consultation fees
            createDoctor("Dr. Sarah", "Johnson", "sarah.johnson@lankamed.com", "Cardiologist", hospitals.get(0), categories.get(0)); // 2500
            createDoctor("Dr. Michael", "Chen", "michael.chen@lankamed.com", "Dermatologist", hospitals.get(0), categories.get(1)); // 1800
            createDoctor("Dr. Priya", "Fernando", "priya.fernando@lankamed.com", "Pediatrician", hospitals.get(1), categories.get(2)); // 2000
            createDoctor("Dr. David", "Rodrigo", "david.rodrigo@lankamed.com", "Neurologist", hospitals.get(1), categories.get(3)); // 3000
            createDoctor("Dr. James", "Wilson", "james.wilson@lankamed.com", "Orthopedic Surgeon", hospitals.get(2), categories.get(4)); // 3500

            System.out.println("DataInitializer: Created 5 sample doctors with different consultation fees");
        } catch (Exception e) {
            System.err.println("DataInitializer: Error creating sample doctors: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createDoctor(String firstName, String lastName, String email, String specialization, Hospital hospital, ServiceCategory category) {
        try {
            System.out.println("DataInitializer: Creating doctor: " + firstName + " " + lastName);

            // Check if doctor already exists
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                System.out.println("DataInitializer: Doctor " + firstName + " " + lastName + " already exists, skipping...");
                return;
            }

            User doctor = User.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .passwordHash(passwordEncoder.encode("doctor123"))
                    .role(Role.DOCTOR)
                    .createdAt(Instant.now())
                    .build();
            doctor = userRepository.save(doctor);
            System.out.println("DataInitializer: Saved user with ID: " + doctor.getUserId());

            // Set consultation fee based on specialization
            Double consultationFee = getConsultationFeeForSpecialization(specialization);

            StaffDetails doctorDetails = StaffDetails.builder()
                    .staffId(doctor.getUserId())
                    .specialization(specialization)
                    .hospital(hospital)
                    .serviceCategory(category)
                    .consultationFee(consultationFee)
                    .user(doctor)
                    .build();
            staffDetailsRepository.save(doctorDetails);
            System.out.println("DataInitializer: Saved staff details for: " + firstName + " " + lastName + " with consultation fee: " + consultationFee);

        } catch (Exception e) {
            System.err.println("DataInitializer: Error creating doctor " + firstName + " " + lastName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Double getConsultationFeeForSpecialization(String specialization) {
        // Set different consultation fees based on specialization
        switch (specialization.toLowerCase()) {
            case "cardiologist":
                return 2500.00;
            case "dermatologist":
                return 1800.00;
            case "pediatrician":
                return 2000.00;
            case "neurologist":
                return 3000.00;
            case "orthopedic surgeon":
                return 3500.00;
            default:
                return 1500.00; // Default consultation fee
        }
    }

    private User createPatient(String firstName, String lastName, String email, String password) {
        User patient = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.PATIENT)
                .createdAt(Instant.now())
                .build();
        patient = userRepository.save(patient);

        Patient patientDetails = Patient.builder()
                .patientId(patient.getUserId())
                .dateOfBirth(java.time.LocalDate.of(1985 + (int)(Math.random() * 20), 
                    (int)(Math.random() * 12) + 1, (int)(Math.random() * 28) + 1))
                .gender(Math.random() > 0.5 ? Patient.Gender.MALE : Patient.Gender.FEMALE)
                .address("Sample Address " + (int)(Math.random() * 1000) + ", Colombo")
                .contactNumber("+94-77-" + String.format("%03d", (int)(Math.random() * 1000)) + "-" + String.format("%04d", (int)(Math.random() * 10000)))
                .user(patient)
                .build();
        patientRepository.save(patientDetails);
        
        System.out.println("DataInitializer: Created patient: " + firstName + " " + lastName);
        return patient;
    }

    private void createSampleAppointments() {
        try {
            // Get all patients and doctors
            List<Patient> patients = patientRepository.findAll();
            List<StaffDetails> doctors = staffDetailsRepository.findAll();
            List<Hospital> hospitals = hospitalRepository.findAll();
            List<ServiceCategory> categories = serviceCategoryRepository.findAll();

            if (patients.isEmpty() || doctors.isEmpty() || hospitals.isEmpty() || categories.isEmpty()) {
                System.out.println("DataInitializer: Cannot create appointments - missing required data");
                return;
            }

            // Create sample appointments with different statuses
            LocalDateTime now = LocalDateTime.now();
            
            // Past appointments (completed)
            createAppointment(patients.get(0), doctors.get(0), hospitals.get(0), categories.get(0), 
                now.minusDays(5).withHour(10).withMinute(0), Appointment.Status.COMPLETED);
            createAppointment(patients.get(1), doctors.get(1), hospitals.get(1), categories.get(1), 
                now.minusDays(3).withHour(14).withMinute(30), Appointment.Status.COMPLETED);

            // Upcoming appointments (pending/approved)
            createAppointment(patients.get(0), doctors.get(2), hospitals.get(2), categories.get(2), 
                now.plusDays(2).withHour(9).withMinute(0), Appointment.Status.APPROVED);
            createAppointment(patients.get(2), doctors.get(3), hospitals.get(0), categories.get(3), 
                now.plusDays(3).withHour(11).withMinute(30), Appointment.Status.PENDING);

            // More upcoming
            createAppointment(patients.get(1), doctors.get(4), hospitals.get(1), categories.get(4), 
                now.plusDays(1).withHour(15).withMinute(0), Appointment.Status.APPROVED);
            createAppointment(patients.get(3), doctors.get(5), hospitals.get(2), categories.get(0), 
                now.plusDays(4).withHour(16).withMinute(30), Appointment.Status.PENDING);

            // More upcoming
            createAppointment(patients.get(2), doctors.get(6), hospitals.get(0), categories.get(1), 
                now.plusDays(5).withHour(10).withMinute(0), Appointment.Status.PENDING);
            createAppointment(patients.get(3), doctors.get(7), hospitals.get(1), categories.get(2), 
                now.plusDays(6).withHour(13).withMinute(0), Appointment.Status.APPROVED);

            System.out.println("DataInitializer: Created 8 sample appointments with various statuses");
        } catch (Exception e) {
            System.err.println("DataInitializer: Error creating sample appointments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createAppointment(Patient patient, StaffDetails doctor, Hospital hospital,
                                 ServiceCategory category, LocalDateTime dateTime, Appointment.Status status) {
        // Calculate payment amount based on doctor's consultation fee
        Double paymentAmount = doctor.getConsultationFee();
        if (paymentAmount == null || paymentAmount <= 0) {
            paymentAmount = 1500.00; // Default consultation fee
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .hospital(hospital)
                .serviceCategory(category)
                .appointmentDateTime(dateTime)
                .status(status)
                .paymentAmount(paymentAmount)
                .build();
        appointmentRepository.save(appointment);

        System.out.println("DataInitializer: Created appointment - " + patient.getUser().getFirstName() +
            " with " + doctor.getUser().getFirstName() + " on " + dateTime.toLocalDate() + " (" + status + ") - Amount: " + paymentAmount);
    }
}
