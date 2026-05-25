package com.meditrack.validation;

import com.meditrack.exception.BadRequestException;
import com.meditrack.exception.ForbiddenException;
import com.meditrack.exception.NotFoundException;
import com.meditrack.model.*;
import com.meditrack.repository.AlarmConfigRepository;
import com.meditrack.repository.MedicineRepository;
import com.meditrack.repository.PatientRepository;
import com.meditrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EntityValidator {

    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;
    private final AlarmConfigRepository alarmConfigRepository;
    private final PatientRepository patientRepository;

    public User getUser(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public Medicine validateMedicine(Long medicineId, Patient patient) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new NotFoundException("Medicine not found"));

        if (!medicine.getPatient().getId().equals(patient.getId())) {
            throw new ForbiddenException("You do not have access to this medicine");
        }

        return medicine;
    }

    public AlarmConfig validateConfig(Long configId, User user) {

        AlarmConfig config = alarmConfigRepository.findById(configId)
                .orElseThrow(() -> new NotFoundException("Config not found"));

        Patient patient = config.getPatient();

        if (user.getRole() == Role.PATIENT) {
            if (!patient.getUser().getId().equals(user.getId())) {
                throw new ForbiddenException("You do not have access to this configuration");
            }
        }

        if (user.getRole() == Role.CAREGIVER) {
            boolean linked = user.getCaregiver().getPatients().stream()
                    .anyMatch(p -> p.getId().equals(patient.getId()));

            if (!linked) {
                throw new ForbiddenException("Patient not linked");
            }
        }

        return config;
    }

    public Patient validatePatientForUser(Long patientId, User user) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found"));

        validateAccess(patient, user);

        return patient;
    }


    public Patient resolvePatient(User user, Long patientId) {
        if (user.getRole() == Role.PATIENT) {
            return user.getPatient();
        }

        if (patientId == null) {
            throw new BadRequestException("Must specify patientId");
        }

        return validatePatientForUser(patientId, user);
    }

    public void validateAccess(Patient patient, User user) {
        if (user.getRole() == Role.PATIENT) {
            if (!Objects.equals(patient.getUser().getId(), user.getId()))
                throw new ForbiddenException("You cannot manage alarms for another patient");
        }
        if (user.getRole() == Role.CAREGIVER) {
            boolean linked = user.getCaregiver().getPatients().stream()
                    .anyMatch(p -> p.getId().equals(patient.getId()));
            if (!linked)
                throw new ForbiddenException("Patient not linked to caregiver");
        }
    }
}