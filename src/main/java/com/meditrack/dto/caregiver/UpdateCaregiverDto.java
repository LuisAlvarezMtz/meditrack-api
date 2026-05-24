package com.meditrack.dto.caregiver;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCaregiverDto {
    private String name;

    @Pattern(regexp = "\\d{10}", message = "Phone number must have 10 digits")
    private String phoneNumber;

    private String occupation;
}