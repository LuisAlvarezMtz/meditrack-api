package com.meditrack.controller;

import com.meditrack.dto.auth.AuthResponseDto;
import com.meditrack.dto.caregiver.RequestCaregiverDto;
import com.meditrack.dto.caregiver.ResponseCaregiverDto;
import com.meditrack.dto.caregiver.UpdateCaregiverDto;
import com.meditrack.dto.caregiver.UpdateCaregiverResponseDto;
import com.meditrack.dto.patient.RequestPatientDto;
import com.meditrack.dto.patient.ResponsePatientDto;
import com.meditrack.dto.patient.ResponsePatientProfileDto;
import com.meditrack.dto.patient.UpdatePatientProfileDto;
import com.meditrack.service.CaregiverService;
import com.meditrack.service.JWTService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/caregivers")
@RequiredArgsConstructor
public class CaregiverController {

    private final CaregiverService caregiverService;
    private final JWTService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(
            @Valid @RequestBody RequestCaregiverDto dto
    ) {
        AuthResponseDto response = caregiverService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update")
    public ResponseEntity<UpdateCaregiverResponseDto> updateCaregiver(
            @RequestBody UpdateCaregiverDto dto,
            @RequestHeader("Authorization") String token
    ) {

        String jwt = token.replace("Bearer ", "");
        String phoneNumber = jwtService.extractPhoneNumber(jwt);

        UpdateCaregiverResponseDto response =
                caregiverService.updateCaregiver(phoneNumber, dto);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/patients")
    public ResponseEntity<List<ResponsePatientDto>> getCaregiverPatients(
            @RequestHeader("Authorization") String token) {

        String jwt = token.replace("Bearer ", "");
        String phoneNumber = jwtService.extractPhoneNumber(jwt);
        List<ResponsePatientDto> patients =
                caregiverService.getCaregiverPatients(phoneNumber);

        return ResponseEntity.ok(patients);
    }

    @GetMapping("/my-data")
    public ResponseEntity<ResponseCaregiverDto> getMyData(
            @RequestHeader("Authorization") String token) {

        String jwt = token.replace("Bearer ", "");
        String phoneNumber = jwtService.extractPhoneNumber(jwt);

        ResponseCaregiverDto dto = caregiverService.getMyData(phoneNumber);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/register-patient")
    public ResponseEntity<ResponsePatientDto> registerPatientFromCaregiver(
            @RequestHeader("Authorization") String token,
            @RequestBody RequestPatientDto dto
    ) {
        String jwt = token.replace("Bearer ", "");
        String phoneNumber = jwtService.extractPhoneNumber(jwt);

        ResponsePatientDto patient =
                caregiverService.registerPatientFromCaregiver(phoneNumber, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(patient);
    }

    @DeleteMapping("/{id}/unlink")
    public ResponseEntity<Void> unlinkPatient(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        String jwt = token.replace("Bearer ", "");
        String phoneNumber = jwtService.extractPhoneNumber(jwt);

        caregiverService.unlinkPatient(id, phoneNumber);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/patients/{patientId}")
    public ResponseEntity<ResponsePatientProfileDto> updatePatient(
            @RequestHeader("Authorization") String token,
            @PathVariable Long patientId,
            @RequestBody UpdatePatientProfileDto dto
    ) {

        String jwt = token.replace("Bearer ", "");
        String phoneNumber = jwtService.extractPhoneNumber(jwt);

        ResponsePatientProfileDto response =
                caregiverService.updatePatientFromCaregiver(
                        patientId,
                        phoneNumber,
                        dto
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/patients/{patientId}")
    public ResponseEntity<ResponsePatientProfileDto> getPatient(
            @RequestHeader("Authorization") String token,
            @PathVariable Long patientId
    ) {

        String jwt = token.replace("Bearer ", "");
        String phoneNumber = jwtService.extractPhoneNumber(jwt);

        ResponsePatientProfileDto response =
                caregiverService.getPatientFromCaregiver(
                        patientId,
                        phoneNumber
                );

        return ResponseEntity.ok(response);
    }

}