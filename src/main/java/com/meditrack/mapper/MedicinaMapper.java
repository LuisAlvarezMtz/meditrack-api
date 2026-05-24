package com.meditrack.mapper;

import com.meditrack.dto.medicina.RequestMedicinaDto;
import com.meditrack.dto.medicina.ResponseMedicinaDto;
import com.meditrack.model.*;

public class MedicinaMapper {

    public static Medicine toEntity(RequestMedicinaDto dto, Patient patient, User registradoPor) {
        if (dto == null) return null;

        Medicine medicine = new Medicine();
        medicine.setNombre(dto.getNombre());
        medicine.setDosageForm(dto.getDosageForm());
        medicine.setExpirationDate(dto.getExpirationDate());
        medicine.setPatient(patient);
        medicine.setRegistradoPor(registradoPor);

        return medicine;
    }

    public static ResponseMedicinaDto toResponse(Medicine medicine) {
        if (medicine == null) return null;

        String registradoPor;

        if (medicine.getRegistradoPor() != null &&
                medicine.getRegistradoPor().getRole() == Role.CUIDADOR) {
            registradoPor = "Caregiver";
        } else {
            registradoPor = "Patient";
        }

        ResponseMedicinaDto dto = new ResponseMedicinaDto();
        dto.setId(medicine.getId());
        dto.setNombre(medicine.getNombre());
        dto.setDosageForm(medicine.getDosageForm());
        dto.setExpirationDate(medicine.getExpirationDate());
        dto.setRegistradoPorNombre(registradoPor);


        if (medicine.getPatient() != null &&
                medicine.getPatient().getUser() != null) {
            dto.setPacienteNombre(
                    medicine.getPatient().getUser().getName()
            );
        }

        return dto;
    }

}
