package com.meditrack.mapper;

import com.meditrack.dto.cuidador.RequestCuidadorDto;
import com.meditrack.dto.cuidador.ResponseCuidadorDto;
import com.meditrack.dto.cuidador.UpdateCuidadorDto;
import com.meditrack.model.Caregiver;
import com.meditrack.model.Role;
import com.meditrack.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;
import java.util.stream.Collectors;

public class CuidadorMapper {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public static Caregiver toEntity(RequestCuidadorDto dto) {
        if (dto == null) return null;

        User user = new User();
        user.setName(dto.getName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRole(Role.CUIDADOR);

        Caregiver caregiver = new Caregiver();
        caregiver.setOcupacion(dto.getOcupacion());
        caregiver.setCodigoVinculacion(generarCodigo());
        caregiver.setUser(user);

        user.setCaregiver(caregiver);

        return caregiver;
    }

    public static ResponseCuidadorDto toResponse(Caregiver caregiver) {
        if (caregiver == null) return null;

        ResponseCuidadorDto dto = new ResponseCuidadorDto();
        dto.setId(caregiver.getId());
        dto.setName(caregiver.getUser().getName());
        dto.setPhoneNumber(caregiver.getUser().getPhoneNumber());
        dto.setOcupacion(caregiver.getOcupacion());
        dto.setCodigoVinculacion(caregiver.getCodigoVinculacion());

        if (caregiver.getPatients() != null) {
            dto.setPacientes(
                    caregiver.getPatients()
                            .stream()
                            .map(p -> p.getUser().getName())
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    public static boolean updateEntity(Caregiver caregiver, UpdateCuidadorDto dto) {
        if (caregiver == null || dto == null) return false;

        boolean requiresReauth = false;

        User user = caregiver.getUser();

        if (dto.getPhoneNumber() != null &&
                !dto.getPhoneNumber().equals(user.getPhoneNumber())) {

            user.setPhoneNumber(dto.getPhoneNumber());
            requiresReauth = true;
        }

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }

        if (dto.getOcupacion() != null) {
            caregiver.setOcupacion(dto.getOcupacion());
        }

        return requiresReauth;
    }

    private static String generarCodigo() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

}
