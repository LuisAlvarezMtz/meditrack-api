package com.meditrack.service;

import com.meditrack.dto.medicine.RequestMedicineDto;
import com.meditrack.dto.medicine.ResponseMedicineDto;
import com.meditrack.exception.ForbiddenException;
import com.meditrack.exception.NotFoundException;
import com.meditrack.mapper.MedicineMapper;
import com.meditrack.model.*;
import com.meditrack.repository.MedicineRepository;
import com.meditrack.repository.UserRepository;
import com.meditrack.validation.EntityValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;
    private final EntityValidator entityValidator;

    @Transactional
    public ResponseMedicineDto registerMedicine
            (RequestMedicineDto dto, String phoneNumber) {

        User registeredBy = entityValidator.getUser(phoneNumber);

        Patient patient = entityValidator.resolvePatient(registeredBy, dto.getPatientId());

        Medicine medicine = MedicineMapper.toEntity(dto, patient, registeredBy);
        Medicine saved = medicineRepository.save(medicine);

        return MedicineMapper.toResponse(saved);
    }

    public List<ResponseMedicineDto> getPatientMedicines(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getRole() != Role.PATIENT) {
            throw new ForbiddenException("Only patients can use this method");
        }

        Patient patient = user.getPatient();
        List<Medicine> medicines = medicineRepository.findByPatientAndStatus(patient, com.meditrack.model.Status.ACTIVE);

        return medicines.stream()
                .map(MedicineMapper::toResponse)
                .toList();
    }


    public List<ResponseMedicineDto> getMedicinesOfPatient(Long patientId, String caregiverPhoneNumber) {
        User user = userRepository.findByPhoneNumber(caregiverPhoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getRole() != Role.CAREGIVER) {
            throw new ForbiddenException("Only caregivers can view patient medicines");
        }

        Caregiver caregiver = user.getCaregiver();

        Patient patient = caregiver.getPatients().stream()
                .filter(p -> p.getId().equals(patientId))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException(
                        "You cannot view medicines of an unlinked patient"));

        List<Medicine> medicines = medicineRepository.findByPatientAndStatus(patient, com.meditrack.model.Status.ACTIVE);

        return medicines.stream()
                .map(MedicineMapper::toResponse)
                .toList();
    }

    public ResponseMedicineDto getById(Long id, String phoneNumber) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Medicine not found"));

        validateAccess(medicine.getPatient(), phoneNumber);

        return MedicineMapper.toResponse(medicine);
    }

    public ResponseMedicineDto updateMedicine(Long id, RequestMedicineDto dto, String phoneNumber) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Medicine not found"));

        validateAccess(medicine.getPatient(), phoneNumber);

        medicine.setName(dto.getName());
        medicine.setDosageForm(dto.getDosageForm());
        medicine.setExpirationDate(dto.getExpirationDate());

        Medicine saved = medicineRepository.save(medicine);
        return MedicineMapper.toResponse(saved);
    }

    @Transactional
    public void deactivateMedicine(Long id, String phoneNumber) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Medicine not found"));

        validateAccess(medicine.getPatient(), phoneNumber);

        medicine.setStatus(com.meditrack.model.Status.INACTIVE);
        medicine.getAlarmConfigs().clear();

    }

    private void validateAccess(Patient patient, String phoneNumber) {
        User user = entityValidator.getUser(phoneNumber);
        entityValidator.validateAccess(patient, user);
    }

}