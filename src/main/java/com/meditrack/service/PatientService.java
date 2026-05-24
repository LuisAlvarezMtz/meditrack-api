package com.meditrack.service;

import com.meditrack.dto.auth.AuthResponseDto;
import com.meditrack.dto.caregiver.CaregiverInfoDto;
import com.meditrack.dto.patient.*;
import com.meditrack.exception.BadRequestException;
import com.meditrack.exception.ConflictException;
import com.meditrack.exception.ForbiddenException;
import com.meditrack.exception.NotFoundException;
import com.meditrack.mapper.PatientMapper;
import com.meditrack.model.Caregiver;
import com.meditrack.model.Patient;
import com.meditrack.model.User;
import com.meditrack.repository.CaregiverRepository;
import com.meditrack.repository.PatientRepository;
import com.meditrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepo;
    private final JWTService jwtService;
    private final CaregiverRepository caregiverRepository;

    public PatientService(PatientRepository patientRepository,
                           UserRepository userRepo, JWTService jwtService,
                           CaregiverRepository caregiverRepository) {
        this.patientRepository = patientRepository;
        this.userRepo = userRepo;
        this.jwtService = jwtService;
        this.caregiverRepository = caregiverRepository;
    }

    @Transactional
    public AuthResponseDto register(RequestPatientDto dto) {
        Optional<User> existing = userRepo.findByPhoneNumber(dto.getPhoneNumber());
        if (existing.isPresent()) {
            throw new ConflictException("The phone number is already registered");
        }
        Caregiver caregiver = null;
        Patient patient = PatientMapper.toEntity(dto, caregiver);
        Patient saved = patientRepository.save(patient);

        User user = saved.getUser();

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponseDto(accessToken, refreshToken);
    }


    @Transactional
    public void linkCaregiver(String patientPhoneNumber, String code) {
        Patient patient = userRepo.findByPhoneNumber(patientPhoneNumber)
                .map(User::getPatient)
                .orElseThrow(() ->
                        new NotFoundException("Patient not found"));
        Caregiver caregiver = caregiverRepository.findByLinkCode(code)
                .orElseThrow(() ->
                        new BadRequestException("Invalid caregiver code"));

        if (patient.getCaregiver() != null) throw new ForbiddenException("The patient is already linked to a caregiver");

        patient.setCaregiver(caregiver);
        patientRepository.save(patient);
    }

    @Transactional(readOnly = true)
    public CaregiverInfoDto findCaregiverByCode(String code) {
        Caregiver caregiver = caregiverRepository
                .findByLinkCode(code).orElseThrow(() ->
                        new BadRequestException("Invalid caregiver code"));
        return new CaregiverInfoDto(
                caregiver.getUser().getName(),
                caregiver.getUser().getPhoneNumber());
    }

    public void unlinkCaregiver(String phoneNumber) {
        User user = userRepo.findByPhoneNumber(phoneNumber)
                .orElseThrow(() ->
                        new NotFoundException("User not found"));
        Patient patient = user.getPatient();
        if (patient == null)
            throw new ForbiddenException("The user is not a patient");
        if (patient.getCaregiver() == null)
            throw new ForbiddenException("The patient has no linked caregiver");
        patient.setCaregiver(null);
        patientRepository.save(patient);
    }

    public ResponsePatientDto getProfile(String currentPhoneNumber) {
        User user = userRepo.findByPhoneNumber(currentPhoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Patient not found for this user"));

        return PatientMapper.toResponse(patient);
    }


    public void changeCaregiver(String phoneNumber, String caregiverCode) {
        User user = userRepo.findByPhoneNumber(phoneNumber)
                .orElseThrow(() ->
                        new NotFoundException("User not found"));
        Patient patient = user.getPatient();
        if (patient == null)
            throw new ForbiddenException("The user is not a patient");
        Caregiver caregiver = caregiverRepository
                .findByLinkCode(caregiverCode)
                .orElseThrow(() -> new NotFoundException("Caregiver not found"));
        patient.setCaregiver(caregiver);
        patientRepository.save(patient);
    }

    @Transactional
    public ResponsePatientProfileDto updatePatientProfileFromCaregiver(
            Patient patient,
            UpdatePatientProfileDto dto
    ) {
        validatePhoneChange(patient, dto.getPhoneNumber());

        PatientMapper.updatePacienteFromDto(dto, patient);

        return PatientMapper.toResponseProfile(patient);
    }

    @Transactional
    public UpdatePatientProfileResponseDto updateOwnProfile(
            String phoneNumber,
            UpdatePatientProfileDto dto
    ) {

        Patient patient = patientRepository
                .findByUserPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("Patient not found"));

        validatePhoneChange(patient, dto.getPhoneNumber());

        boolean requiresReauth = PatientMapper.updatePacienteFromDto(dto, patient);

        userRepo.save(patient.getUser());

        return new UpdatePatientProfileResponseDto(
                requiresReauth
                        ? "Phone number updated. Re-login is required."
                        : "Profile updated successfully",
                requiresReauth,
                PatientMapper.toResponseProfile(patient)
        );
    }

    private void validatePhoneChange(Patient patient, String newPhoneNumber) {

        if (newPhoneNumber == null || newPhoneNumber.isBlank()) return;

        String current = patient.getUser().getPhoneNumber();

        if (!newPhoneNumber.equals(current)) {

            User existing = userRepo.findByPhoneNumber(newPhoneNumber).orElse(null);

            if (existing != null &&
                    !existing.getId().equals(patient.getUser().getId())) {

                throw new ConflictException("The phone number is already registered");
            }
        }
    }
}