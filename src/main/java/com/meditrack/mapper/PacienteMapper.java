package com.meditrack.mapper;


import com.meditrack.dto.paciente.RequestPacienteDto;
import com.meditrack.dto.paciente.ResponsePacienteDto;
import com.meditrack.dto.paciente.ResponsePacientePerfilDto;
import com.meditrack.dto.paciente.UpdatePacientePerfilDto;
import com.meditrack.model.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PacienteMapper {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public static Patient toEntity(RequestPacienteDto dto, Caregiver caregiver) {
        if (dto == null) return null;

        User user = new User();
        user.setName(dto.getName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRole(Role.PATIENT);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setEdad(dto.getEdad());
        patient.setCaregiver(caregiver);

        user.setPatient(patient);

        if (caregiver != null) {
            caregiver.getPatients().add(patient);
        }

        return patient;
    }

    public static ResponsePacienteDto toResponse(Patient patient) {
        if (patient == null) return null;

        ResponsePacienteDto dto = new ResponsePacienteDto();
        dto.setId(patient.getId());
        dto.setName(patient.getUser().getName());
        dto.setPhoneNumber(patient.getUser().getPhoneNumber());
        dto.setEdad(patient.getEdad());
        dto.setCurp(patient.getCurp());
        dto.setEnfermedadesCronicas(patient.getEnfermedadesCronicas());

        if (patient.getCaregiver() != null && patient.getCaregiver().getUser() != null) {
            dto.setCuidadorName(patient.getCaregiver().getUser().getName());
            dto.setCodigoCuidador(patient.getCaregiver().getCodigoVinculacion());
        } else {
            dto.setCuidadorName(null);
            dto.setCodigoCuidador(null);
        }

        return dto;
    }

    public static boolean updatePacienteFromDto(UpdatePacientePerfilDto dto, Patient patient) {
        if (dto == null || patient == null) return false;

        boolean requiresReauth = false;

        if (dto.getNombre() != null) {
            patient.getUser().setName(dto.getNombre());
        }

        if (dto.getPhoneNumber() != null &&
                !dto.getPhoneNumber().equals(patient.getUser().getPhoneNumber())) {

            patient.getUser().setPhoneNumber(dto.getPhoneNumber());
            requiresReauth = true;
        }

        if (dto.getEdad() != null) {
            patient.setEdad(dto.getEdad());
        }

        if (dto.getCurp() != null) {
            patient.setCurp(dto.getCurp());
        }

        if (dto.getEnfermedadesCronicas() != null) {
            patient.setEnfermedadesCronicas(dto.getEnfermedadesCronicas());
        }

        return requiresReauth;
    }

    public static ResponsePacientePerfilDto toResponsePerfil(Patient patient) {
        if (patient == null) return null;

        ResponsePacientePerfilDto dto = new ResponsePacientePerfilDto();
        dto.setId(patient.getId());
        dto.setName(patient.getUser().getName());
        dto.setPhoneNumber(patient.getUser().getPhoneNumber());
        dto.setEdad(patient.getEdad());
        dto.setCurp(patient.getCurp());
        dto.setEnfermedadesCronicas(patient.getEnfermedadesCronicas());

        return dto;
    }
}
