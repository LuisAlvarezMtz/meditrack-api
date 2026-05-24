package com.meditrack.mapper;


import com.meditrack.dto.patient.RequestPatientDto;
import com.meditrack.dto.patient.ResponsePatientDto;
import com.meditrack.dto.patient.ResponsePatientProfileDto;
import com.meditrack.dto.patient.UpdatePatientProfileDto;
import com.meditrack.model.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PatientMapper {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public static Patient toEntity(RequestPatientDto dto, Caregiver caregiver) {
        if (dto == null) return null;

        User user = new User();
        user.setName(dto.getName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRole(Role.PATIENT);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setAge(dto.getAge());
        patient.setCaregiver(caregiver);

        user.setPatient(patient);

        if (caregiver != null) {
            caregiver.getPatients().add(patient);
        }

        return patient;
    }

    public static ResponsePatientDto toResponse(Patient patient) {
        if (patient == null) return null;

        ResponsePatientDto dto = new ResponsePatientDto();
        dto.setId(patient.getId());
        dto.setName(patient.getUser().getName());
        dto.setPhoneNumber(patient.getUser().getPhoneNumber());
        dto.setAge(patient.getAge());
        dto.setCurp(patient.getCurp());
        dto.setChronicDiseases(patient.getChronicDiseases());

        if (patient.getCaregiver() != null && patient.getCaregiver().getUser() != null) {
            dto.setCaregiverName(patient.getCaregiver().getUser().getName());
            dto.setCaregiverCode(patient.getCaregiver().getLinkCode());
        } else {
            dto.setCaregiverName(null);
            dto.setCaregiverCode(null);
        }

        return dto;
    }

    public static boolean updatePacienteFromDto(UpdatePatientProfileDto dto, Patient patient) {
        if (dto == null || patient == null) return false;

        boolean requiresReauth = false;

        if (dto.getName() != null) {
            patient.getUser().setName(dto.getName());
        }

        if (dto.getPhoneNumber() != null &&
                !dto.getPhoneNumber().equals(patient.getUser().getPhoneNumber())) {

            patient.getUser().setPhoneNumber(dto.getPhoneNumber());
            requiresReauth = true;
        }

        if (dto.getAge() != null) {
            patient.setAge(dto.getAge());
        }

        if (dto.getCurp() != null) {
            patient.setCurp(dto.getCurp());
        }

        if (dto.getChronicDiseases() != null) {
            patient.setChronicDiseases(dto.getChronicDiseases());
        }

        return requiresReauth;
    }

    public static ResponsePatientProfileDto toResponseProfile(Patient patient) {
        if (patient == null) return null;

        ResponsePatientProfileDto dto = new ResponsePatientProfileDto();
        dto.setId(patient.getId());
        dto.setName(patient.getUser().getName());
        dto.setPhoneNumber(patient.getUser().getPhoneNumber());
        dto.setAge(patient.getAge());
        dto.setCurp(patient.getCurp());
        dto.setChronicDiseases(patient.getChronicDiseases());

        return dto;
    }
}