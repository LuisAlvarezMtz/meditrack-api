package com.meditrack.dto.patient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponsePatientDto {
    private Long id;
    private String name;
    private String phoneNumber;
    private String caregiverName;
    private String caregiverCode;
    private Integer age;
    private String curp;
    private List<String> chronicDiseases;

}
