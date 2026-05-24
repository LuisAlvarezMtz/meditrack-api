package com.meditrack.dto.caregiver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCaregiverDto {
    private Long id;
    private String name;
    private String phoneNumber;
    private String occupation;
    private String linkCode;
    private List<String> patients;
}
