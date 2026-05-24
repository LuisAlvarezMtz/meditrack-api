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
import com.meditrack.service.CuidadorService;
import com.meditrack.service.JWTService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cuidadores")
@RequiredArgsConstructor
public class CuidadorController {

    private final CuidadorService cuidadorSrv;
    private final JWTService jwtService;

    @PostMapping("/registro")
    public ResponseEntity<AuthResponseDto> registrar(
            @Valid @RequestBody RequestCaregiverDto dto
    ) {
        AuthResponseDto response = cuidadorSrv.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/actualizar")
    public ResponseEntity<UpdateCaregiverResponseDto> actualizarCuidador(
            @RequestBody UpdateCaregiverDto dto,
            @RequestHeader("Authorization") String token
    ) {

        String jwt = token.replace("Bearer ", "");
        String phoneNumber = jwtService.extractPhoneNumber(jwt);

        UpdateCaregiverResponseDto response =
                cuidadorSrv.actualizarCuidador(phoneNumber, dto);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/pacientes-del-cuidador")
    public ResponseEntity<List<ResponsePatientDto>> obtenerPacientesDelCuidador(
            @RequestHeader("Authorization") String token) {

        String jwt = token.replace("Bearer ", "");
        String phoneNumber = jwtService.extractPhoneNumber(jwt);
        List<ResponsePatientDto> pacientes =
                cuidadorSrv.obtenerPacientesDeCuidador(phoneNumber);

        return ResponseEntity.ok(pacientes);
    }

    @GetMapping("/mis-datos")
    public ResponseEntity<ResponseCaregiverDto> obtenerMisDatosCuidador(
            @RequestHeader("Authorization") String token) {

        String jwt = token.replace("Bearer ", "");
        String phoneNumber = jwtService.extractPhoneNumber(jwt);

        ResponseCaregiverDto dto = cuidadorSrv.obtenerMisDatos(phoneNumber);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/registrar-paciente")
    public ResponseEntity<ResponsePatientDto> registrarPacienteDesdeCuidador(
            @RequestHeader("Authorization") String token,
            @RequestBody RequestPatientDto dto
    ) {
        String jwt = token.replace("Bearer ", "");
        String phoneNumber = jwtService.extractPhoneNumber(jwt);

        ResponsePatientDto paciente =
                cuidadorSrv.registrarPacienteDesdeCuidador(phoneNumber, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(paciente);
    }

    @DeleteMapping("/{id}/desvincular")
    public ResponseEntity<Void> desvincularPaciente(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        String jwt = token.replace("Bearer ", "");
        String phoneNumber = jwtService.extractPhoneNumber(jwt);

        cuidadorSrv.desvincularPaciente(id, phoneNumber);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/pacientes/{pacienteId}")
    public ResponseEntity<ResponsePatientProfileDto> actualizarPaciente(
            @RequestHeader("Authorization") String token,
            @PathVariable Long pacienteId,
            @RequestBody UpdatePatientProfileDto dto
    ) {

        String jwt = token.replace("Bearer ", "");
        String phoneNumber = jwtService.extractPhoneNumber(jwt);

        ResponsePatientProfileDto response =
                cuidadorSrv.actualizarPacienteDesdeCuidador(
                        pacienteId,
                        phoneNumber,
                        dto
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pacientes/{pacienteId}")
    public ResponseEntity<ResponsePatientProfileDto> obtenerPaciente(
            @RequestHeader("Authorization") String token,
            @PathVariable Long pacienteId
    ) {

        String jwt = token.replace("Bearer ", "");
        String phoneNumber = jwtService.extractPhoneNumber(jwt);

        ResponsePatientProfileDto response =
                cuidadorSrv.obtenerPacienteDesdeCuidador(
                        pacienteId,
                        phoneNumber
                );

        return ResponseEntity.ok(response);
    }

}
