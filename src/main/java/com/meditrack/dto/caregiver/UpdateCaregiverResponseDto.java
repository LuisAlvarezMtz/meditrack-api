package com.meditrack.dto.caregiver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCaregiverResponseDto {
    private String message;
    private boolean requiresReauth;
    private ResponseCaregiverDto caregiver;
}