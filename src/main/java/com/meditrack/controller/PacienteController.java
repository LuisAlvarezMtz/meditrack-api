package com.meditrack.controller;

import com.meditrack.dto.auth.AuthResponseDto;
import com.meditrack.dto.cuidador.CuidadorInfoDto;
import com.meditrack.dto.paciente.RequestPacienteDto;
import com.meditrack.dto.paciente.ResponsePacienteDto;
import com.meditrack.dto.paciente.UpdatePacientePerfilDto;
import com.meditrack.dto.paciente.UpdatePacientePerfilResponseDto;
import com.meditrack.service.JWTService;
import com.meditrack.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteSrv;
    private final JWTService jwtService;

    @PostMapping("/registro")
    public ResponseEntity<AuthResponseDto> registrar(
            @Valid @RequestBody RequestPacienteDto dto
    ) {
        AuthResponseDto response = pacienteSrv.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/perfil")
    public ResponseEntity<UpdatePacientePerfilResponseDto> actualizarPerfil(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdatePacientePerfilDto dto
    ) {

        String jwt = token.replace("Bearer ", "");
        String phoneNumber = jwtService.extractPhoneNumber(jwt);

        return ResponseEntity.ok(
                pacienteSrv.actualizarPerfilPropio(phoneNumber, dto)
        );
    }

    @PostMapping("/cuidador")
    public ResponseEntity<Map<String, String>> vincularCuidador(
            @RequestParam String codigo,
            Authentication authentication) {

        String phoneNumber = authentication.getName();
        pacienteSrv.vincularCuidador(phoneNumber, codigo);

        return ResponseEntity.ok(
                Map.of("mensaje",
                        "Patient vinculado correctamente al cuidador"));
    }

    @GetMapping("/cuidador")
    public ResponseEntity<CuidadorInfoDto> obtenerCuidadorPorCodigo(
            @RequestParam String codigo) {

        return ResponseEntity.ok(
                pacienteSrv.buscarCuidadorPorCodigo(codigo)
        );
    }

    @DeleteMapping("/cuidador")
    public ResponseEntity<Map<String, String>>
    desvincularCuidador(Authentication authentication) {
        String phoneNumber = authentication.getName();
        pacienteSrv.desvincularCuidador(phoneNumber);

        return ResponseEntity.ok
                (Map.of("mensaje", "Caregiver " +
                        "desvinculado correctamente"));
    }

    @GetMapping("/perfil")
    public ResponseEntity<ResponsePacienteDto> obtenerMisDatos
            (Authentication authentication) {
        String phoneNumber = authentication.getName();

        ResponsePacienteDto response = pacienteSrv.obtenerPerfil(phoneNumber);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/cambiar-cuidador")
    public ResponseEntity<Map<String, String>> cambiarCuidador(
            @RequestParam String nuevoCodigo,
            Authentication authentication) {

        String phoneNumber = authentication.getName();
        pacienteSrv.cambiarCuidador(phoneNumber, nuevoCodigo);

        return ResponseEntity.ok
                (Map.of("mensaje", "Caregiver actualizado correctamente"));
    }
}
