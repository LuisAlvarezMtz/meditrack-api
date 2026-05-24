package com.meditrack.dto.patient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePatientProfileResponseDto {
    private String message;
    private boolean requiresReauth;
    private ResponsePatientProfileDto patient;
}
