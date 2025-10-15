package com.lankamed.health.backend.service.patient;

import com.lankamed.health.backend.dto.patient.PrescriptionDto;
import com.lankamed.health.backend.dto.patient.AllergyDto;
import com.lankamed.health.backend.dto.patient.CreateAllergyDto;
import com.lankamed.health.backend.dto.patient.CreateMedicalConditionDto;
import com.lankamed.health.backend.dto.patient.MedicalConditionDto;
import com.lankamed.health.backend.repository.patient.AllergyRepository;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import com.lankamed.health.backend.repository.patient.MedicalConditionRepository;
import com.lankamed.health.backend.repository.patient.PrescriptionRepository;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.model.patient.Allergy;
import com.lankamed.health.backend.model.patient.MedicalCondition;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicalHistoryService {

    private final PatientRepository patientRepository;
    private final MedicalConditionRepository medicalConditionRepository;
    private final AllergyRepository allergyRepository;
    private final PrescriptionRepository prescriptionRepository;

    public MedicalHistoryService(PatientRepository patientRepository,
                                 MedicalConditionRepository medicalConditionRepository,
                                 AllergyRepository allergyRepository,
                                 PrescriptionRepository prescriptionRepository) {
        this.patientRepository = patientRepository;
        this.medicalConditionRepository = medicalConditionRepository;
        this.allergyRepository = allergyRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    public List<MedicalConditionDto> getMedicalConditions() {
        String email = getCurrentUserEmail();
        return medicalConditionRepository.findByPatientUserEmail(email)
                .stream()
                .map(MedicalConditionDto::fromMedicalCondition)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicalConditionDto createMedicalCondition(CreateMedicalConditionDto createDto) {
        String email = getCurrentUserEmail();
        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        MedicalCondition condition = MedicalCondition.builder()
                .patient(patient)
                .conditionName(createDto.getConditionName())
                .diagnosedDate(createDto.getDiagnosedDate())
                .notes(createDto.getNotes())
                .build();

        MedicalCondition saved = medicalConditionRepository.save(condition);
        return MedicalConditionDto.fromMedicalCondition(saved);
    }

    @Transactional
    public MedicalConditionDto updateMedicalCondition(Long conditionId, CreateMedicalConditionDto updateDto) {
        MedicalCondition condition = medicalConditionRepository.findById(conditionId)
                .orElseThrow(() -> new RuntimeException("Medical condition not found"));

        String email = getCurrentUserEmail();
        if (!condition.getPatient().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access to medical condition");
        }

        condition.setConditionName(updateDto.getConditionName());
        condition.setDiagnosedDate(updateDto.getDiagnosedDate());
        condition.setNotes(updateDto.getNotes());

        MedicalCondition saved = medicalConditionRepository.save(condition);
        return MedicalConditionDto.fromMedicalCondition(saved);
    }

    @Transactional
    public void deleteMedicalCondition(Long conditionId) {
        MedicalCondition condition = medicalConditionRepository.findById(conditionId)
                .orElseThrow(() -> new RuntimeException("Medical condition not found"));

        String email = getCurrentUserEmail();
        if (!condition.getPatient().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access to medical condition");
        }

        medicalConditionRepository.delete(condition);
    }

    public List<AllergyDto> getAllergies() {
        String email = getCurrentUserEmail();
        return allergyRepository.findByPatientUserEmail(email)
                .stream()
                .map(AllergyDto::fromAllergy)
                .collect(Collectors.toList());
    }

    @Transactional
    public AllergyDto createAllergy(CreateAllergyDto createDto) {
        String email = getCurrentUserEmail();
        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Allergy allergy = Allergy.builder()
                .patient(patient)
                .allergyName(createDto.getAllergyName())
                .severity(createDto.getSeverity())
                .notes(createDto.getNotes())
                .build();

        Allergy saved = allergyRepository.save(allergy);
        return AllergyDto.fromAllergy(saved);
    }

    @Transactional
    public AllergyDto updateAllergy(Long allergyId, CreateAllergyDto updateDto) {
        Allergy allergy = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new RuntimeException("Allergy not found"));

        String email = getCurrentUserEmail();
        if (!allergy.getPatient().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access to allergy");
        }

        allergy.setAllergyName(updateDto.getAllergyName());
        allergy.setSeverity(updateDto.getSeverity());
        allergy.setNotes(updateDto.getNotes());

        Allergy saved = allergyRepository.save(allergy);
        return AllergyDto.fromAllergy(saved);
    }

    @Transactional
    public void deleteAllergy(Long allergyId) {
        Allergy allergy = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new RuntimeException("Allergy not found"));

        String email = getCurrentUserEmail();
        if (!allergy.getPatient().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access to allergy");
        }

        allergyRepository.delete(allergy);
    }

    public List<PrescriptionDto> getPrescriptions() {
        String email = getCurrentUserEmail();
        return prescriptionRepository.findByPatientUserEmail(email)
                .stream()
                .map(PrescriptionDto::fromPrescription)
                .collect(Collectors.toList());
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}


