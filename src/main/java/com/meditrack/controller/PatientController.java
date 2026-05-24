package com.meditrack.controller;

import com.meditrack.dto.auth.AuthResponseDto;
import com.meditrack.dto.caregiver.CaregiverInfoDto;
import com.meditrack.dto.patient.RequestPatientDto;
import com.meditrack.dto.patient.ResponsePatientDto;
import com.meditrack.dto.patient.UpdatePatientProfileDto;
import com.meditrack.dto.patient.UpdatePatientProfileResponseDto;
import com.meditrack.service.JWTService;
import com.meditrack.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final JWTService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(
            @Valid @RequestBody RequestPatientDto dto
    ) {
        AuthResponseDto response = patientService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<UpdatePatientProfileResponseDto> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdatePatientProfileDto dto
    ) {

        String jwt = token.replace("Bearer ", "");
        String phoneNumber = jwtService.extractPhoneNumber(jwt);

        return ResponseEntity.ok(
                patientService.updateOwnProfile(phoneNumber, dto)
        );
    }

    @PostMapping("/caregiver")
    public ResponseEntity<Map<String, String>> linkCaregiver(
            @RequestParam String code,
            Authentication authentication) {

        String phoneNumber = authentication.getName();
        patientService.linkCaregiver(phoneNumber, code);

        return ResponseEntity.ok(
                Map.of("message",
                        "Patient linked to caregiver successfully"));
    }

    @GetMapping("/caregiver")
    public ResponseEntity<CaregiverInfoDto> getCaregiverByCode(
            @RequestParam String code) {

        return ResponseEntity.ok(
                patientService.findCaregiverByCode(code)
        );
    }

    @DeleteMapping("/caregiver")
    public ResponseEntity<Map<String, String>>
    unlinkCaregiver(Authentication authentication) {
        String phoneNumber = authentication.getName();
        patientService.unlinkCaregiver(phoneNumber);

        return ResponseEntity.ok
                (Map.of("message", "Caregiver unlinked successfully"));
    }

    @GetMapping("/profile")
    public ResponseEntity<ResponsePatientDto> getMyData
            (Authentication authentication) {
        String phoneNumber = authentication.getName();

        ResponsePatientDto response = patientService.getProfile(phoneNumber);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-caregiver")
    public ResponseEntity<Map<String, String>> changeCaregiver(
            @RequestParam String newCode,
            Authentication authentication) {

        String phoneNumber = authentication.getName();
        patientService.changeCaregiver(phoneNumber, newCode);

        return ResponseEntity.ok
                (Map.of("message", "Caregiver updated successfully"));
    }
}