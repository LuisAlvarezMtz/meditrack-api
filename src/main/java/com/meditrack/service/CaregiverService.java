package com.meditrack.service;

import com.meditrack.dto.auth.AuthResponseDto;
import com.meditrack.dto.caregiver.RequestCaregiverDto;
import com.meditrack.dto.caregiver.ResponseCaregiverDto;
import com.meditrack.dto.caregiver.UpdateCaregiverDto;
import com.meditrack.dto.caregiver.UpdateCaregiverResponseDto;
import com.meditrack.dto.patient.RequestPatientDto;
import com.meditrack.dto.patient.ResponsePatientDto;
import com.meditrack.dto.patient.ResponsePatientProfileDto;
import com.meditrack.dto.patient.UpdatePatientProfileDto;
import com.meditrack.exception.ConflictException;
import com.meditrack.exception.ForbiddenException;
import com.meditrack.exception.NotFoundException;
import com.meditrack.mapper.CaregiverMapper;
import com.meditrack.mapper.PatientMapper;
import com.meditrack.model.Caregiver;
import com.meditrack.model.Patient;
import com.meditrack.model.User;
import com.meditrack.repository.CaregiverRepository;
import com.meditrack.repository.PatientRepository;
import com.meditrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CaregiverService {

    private final CaregiverRepository caregiverRepository;
    private final UserRepository userRepo;
    private final PatientRepository patientRepository;
    private final PatientService patientService;
    private final JWTService jwtService;


    @Transactional
    public AuthResponseDto register(RequestCaregiverDto dto) {

        Optional<User> existing = userRepo.findByPhoneNumber(dto.getPhoneNumber());
        if (existing.isPresent()) {
            throw new ConflictException("The phone number is already registered");
        }
        Caregiver caregiver = CaregiverMapper.toEntity(dto);

        User savedUser = userRepo.save(caregiver.getUser());
        caregiver.setUser(savedUser);

        caregiverRepository.save(caregiver);

        String accessToken = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        return new AuthResponseDto(accessToken, refreshToken);
    }

    public UpdateCaregiverResponseDto updateCaregiver(String phoneNumber, UpdateCaregiverDto dto) {

        Caregiver caregiver = caregiverRepository.findByUserPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("Caregiver not found"));

        if (dto.getPhoneNumber() != null &&
                !dto.getPhoneNumber().equals(caregiver.getUser().getPhoneNumber())) {

            if (userRepo.existsByPhoneNumber(dto.getPhoneNumber())) {
                throw new ConflictException("The phone number is already in use");
            }
        }
        boolean requiresReauth = CaregiverMapper.updateEntity(caregiver, dto);

        caregiverRepository.save(caregiver);

        return new UpdateCaregiverResponseDto(
                requiresReauth
                        ? "Phone number updated. Re-login is required."
                        : "Data updated successfully",
                requiresReauth,
                CaregiverMapper.toResponse(caregiver)
        );
    }


    public List<ResponsePatientDto> getCaregiverPatients(String caregiverPhoneNumber) {
        Caregiver caregiver = getCaregiver(caregiverPhoneNumber);
        return caregiver.getPatients().stream()
                .map(PatientMapper::toResponse)
                .toList();
    }

    public ResponseCaregiverDto getMyData(String caregiverPhoneNumber) {
        Caregiver caregiver = caregiverRepository.findByUserPhoneNumber(caregiverPhoneNumber)
                .orElseThrow(() -> new NotFoundException("Caregiver not found"));

        return CaregiverMapper.toResponse(caregiver);
    }

    @Transactional
    public ResponsePatientDto registerPatientFromCaregiver(
            String caregiverPhoneNumber,
            RequestPatientDto dto) {

        Caregiver caregiver = getCaregiver(caregiverPhoneNumber);
        if (userRepo.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
            throw new ConflictException("The phone number is already registered");
        }
        Patient patient = PatientMapper.toEntity(dto, caregiver);
        patientRepository.save(patient);

        return PatientMapper.toResponse(patient);
    }


    @Transactional
    public void unlinkPatient(Long patientId, String phoneNumber) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found"));

        if (patient.getCaregiver() == null ||
                !patient.getCaregiver().getUser().getPhoneNumber().equals(phoneNumber)) {
            throw new ForbiddenException("You cannot unlink this patient");
        }

        patient.setCaregiver(null);
    }

    @Transactional
    public ResponsePatientProfileDto updatePatientFromCaregiver(
            Long patientId,
            String caregiverPhoneNumber,
            UpdatePatientProfileDto dto
    ) {

        Caregiver caregiver = getCaregiver(caregiverPhoneNumber);
        Patient patient = getPatient(patientId);

        if (patient.getCaregiver() == null ||
                !patient.getCaregiver().getId().equals(caregiver.getId())) {

            throw new ForbiddenException("This patient does not belong to the caregiver");
        }

        return patientService.updatePatientProfileFromCaregiver(patient, dto);
    }

    @Transactional(readOnly = true)
    public ResponsePatientProfileDto getPatientFromCaregiver(
            Long patientId,
            String caregiverPhoneNumber
    ) {

        Caregiver caregiver = getCaregiver(caregiverPhoneNumber);

        Patient patient = getPatient(patientId);

        if (!patient.getCaregiver().getId().equals(caregiver.getId())) {
            throw new ForbiddenException("This patient does not belong to the caregiver");
        }

        return PatientMapper.toResponseProfile(patient);
    }

    private Patient getPatient(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found"));
    }

    private Caregiver getCaregiver(String caregiverPhoneNumber) {
        return caregiverRepository
                .findByUserPhoneNumber(caregiverPhoneNumber)
                .orElseThrow(() -> new NotFoundException("Caregiver not found"));
    }
}