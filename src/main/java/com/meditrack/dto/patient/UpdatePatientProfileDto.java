package com.meditrack.dto.patient;

import lombok.Data;

import java.util.List;

@Data
public class UpdatePatientProfileDto {

    private String name;
    private String phoneNumber;
    private Integer age;
    private String curp;
    private List<String> chronicDiseases;

}
