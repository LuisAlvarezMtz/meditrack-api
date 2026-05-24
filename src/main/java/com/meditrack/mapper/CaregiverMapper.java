package com.meditrack.mapper;

import com.meditrack.dto.caregiver.RequestCaregiverDto;
import com.meditrack.dto.caregiver.ResponseCaregiverDto;
import com.meditrack.dto.caregiver.UpdateCaregiverDto;
import com.meditrack.model.Caregiver;
import com.meditrack.model.Role;
import com.meditrack.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;
import java.util.stream.Collectors;

public class CaregiverMapper {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public static Caregiver toEntity(RequestCaregiverDto dto) {
        if (dto == null) return null;

        User user = new User();
        user.setName(dto.getName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRole(Role.CAREGIVER);

        Caregiver caregiver = new Caregiver();
        caregiver.setOccupation(dto.getOccupation());
        caregiver.setLinkCode(generateCode());
        caregiver.setUser(user);

        user.setCaregiver(caregiver);

        return caregiver;
    }

    public static ResponseCaregiverDto toResponse(Caregiver caregiver) {
        if (caregiver == null) return null;

        ResponseCaregiverDto dto = new ResponseCaregiverDto();
        dto.setId(caregiver.getId());
        dto.setName(caregiver.getUser().getName());
        dto.setPhoneNumber(caregiver.getUser().getPhoneNumber());
        dto.setOccupation(caregiver.getOccupation());
        dto.setLinkCode(caregiver.getLinkCode());

        if (caregiver.getPatients() != null) {
            dto.setPatients(
                    caregiver.getPatients()
                            .stream()
                            .map(p -> p.getUser().getName())
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    public static boolean updateEntity(Caregiver caregiver, UpdateCaregiverDto dto) {
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

        if (dto.getOccupation() != null) {
            caregiver.setOccupation(dto.getOccupation());
        }

        return requiresReauth;
    }

    private static String generateCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

}