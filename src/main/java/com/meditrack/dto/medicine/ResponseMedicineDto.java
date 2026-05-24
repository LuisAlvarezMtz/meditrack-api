package com.meditrack.dto.medicine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMedicineDto {
    private Long id;
    private String name;
    private String dosageForm;
    private LocalDate expirationDate;
    private String patientName;
    private String registeredByName;
}
