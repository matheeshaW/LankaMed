package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.EmergencyContactDto;
import com.lankamed.health.backend.dto.CreateEmergencyContactDto;
import com.lankamed.health.backend.model.EmergencyContact;
import com.lankamed.health.backend.model.Patient;
import com.lankamed.health.backend.repository.EmergencyContactRepository;
import com.lankamed.health.backend.repository.PatientRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmergencyContactService {

    private final EmergencyContactRepository emergencyContactRepository;
    private final PatientRepository patientRepository;

    public EmergencyContactService(EmergencyContactRepository emergencyContactRepository,
                                   PatientRepository patientRepository) {
        this.emergencyContactRepository = emergencyContactRepository;
        this.patientRepository = patientRepository;
    }

    public List<EmergencyContactDto> getEmergencyContacts() {
        String email = getCurrentUserEmail();
        return emergencyContactRepository.findByPatientUserEmail(email)
                .stream()
                .map(EmergencyContactDto::fromEmergencyContact)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmergencyContactDto createEmergencyContact(CreateEmergencyContactDto dto) {
        String email = getCurrentUserEmail();
        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        EmergencyContact ec = EmergencyContact.builder()
                .patient(patient)
                .fullName(dto.getFullName())
                .relationship(dto.getRelationship())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .build();

        EmergencyContact saved = emergencyContactRepository.save(ec);
        return EmergencyContactDto.fromEmergencyContact(saved);
    }

    @Transactional
    public EmergencyContactDto updateEmergencyContact(Long emergencyContactId, CreateEmergencyContactDto dto) {
        EmergencyContact ec = emergencyContactRepository.findById(emergencyContactId)
                .orElseThrow(() -> new RuntimeException("Emergency contact not found"));

        // verify ownership
        String email = getCurrentUserEmail();
        if (!ec.getPatient().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access to emergency contact");
        }

        ec.setFullName(dto.getFullName());
        ec.setRelationship(dto.getRelationship());
        ec.setPhone(dto.getPhone());
        ec.setEmail(dto.getEmail());
        ec.setAddress(dto.getAddress());

        EmergencyContact saved = emergencyContactRepository.save(ec);
        return EmergencyContactDto.fromEmergencyContact(saved);
    }

    @Transactional
    public void deleteEmergencyContact(Long emergencyContactId) {
        EmergencyContact ec = emergencyContactRepository.findById(emergencyContactId)
                .orElseThrow(() -> new RuntimeException("Emergency contact not found"));

        // verify ownership
        String email = getCurrentUserEmail();
        if (!ec.getPatient().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access to emergency contact");
        }

        emergencyContactRepository.delete(ec);
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}


