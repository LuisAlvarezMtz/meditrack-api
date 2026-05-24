package com.meditrack.mapper;

import com.meditrack.dto.medicine.RequestMedicineDto;
import com.meditrack.dto.medicine.ResponseMedicineDto;
import com.meditrack.model.*;

public class MedicineMapper {

    public static Medicine toEntity(RequestMedicineDto dto, Patient patient, User registeredBy) {
        if (dto == null) return null;

        Medicine medicine = new Medicine();
        medicine.setName(dto.getName());
        medicine.setDosageForm(dto.getDosageForm());
        medicine.setExpirationDate(dto.getExpirationDate());
        medicine.setPatient(patient);
        medicine.setRegisteredBy(registeredBy);

        return medicine;
    }

    public static ResponseMedicineDto toResponse(Medicine medicine) {
        if (medicine == null) return null;

        String registeredBy;

        if (medicine.getRegisteredBy() != null &&
                medicine.getRegisteredBy().getRole() == Role.CAREGIVER) {
            registeredBy = "Caregiver";
        } else {
            registeredBy = "Patient";
        }

        ResponseMedicineDto dto = new ResponseMedicineDto();
        dto.setId(medicine.getId());
        dto.setName(medicine.getName());
        dto.setDosageForm(medicine.getDosageForm());
        dto.setExpirationDate(medicine.getExpirationDate());
        dto.setRegisteredByName(registeredBy);


        if (medicine.getPatient() != null &&
                medicine.getPatient().getUser() != null) {
            dto.setPatientName(
                    medicine.getPatient().getUser().getName()
            );
        }

        return dto;
    }

}