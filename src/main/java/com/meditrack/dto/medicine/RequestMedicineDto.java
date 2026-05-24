package com.meditrack.dto.medicine;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestMedicineDto {
    @NotBlank(message = "Medicine name is required")
    private String name;

    @NotBlank(message = "Dosage form is required")
    private String dosageForm;

    @NotNull(message = "Expiration date is required")
    @FutureOrPresent(message = "Expiration date cannot be in the past")
    private LocalDate expirationDate;

    private Long patientId;
}
