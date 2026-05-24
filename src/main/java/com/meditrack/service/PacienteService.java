package com.meditrack.service;

import com.meditrack.dto.auth.AuthResponseDto;
import com.meditrack.dto.caregiver.CaregiverInfoDto;
import com.meditrack.dto.patient.*;
import com.meditrack.exception.BadRequestException;
import com.meditrack.exception.ConflictException;
import com.meditrack.exception.ForbiddenException;
import com.meditrack.exception.NotFoundException;
import com.meditrack.mapper.PacienteMapper;
import com.meditrack.model.Caregiver;
import com.meditrack.model.Patient;
import com.meditrack.model.User;
import com.meditrack.repository.CuidadorRepository;
import com.meditrack.repository.PacienteRepository;
import com.meditrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UserRepository userRepo;
    private final JWTService jwtService;
    private final CuidadorRepository cuidadorRepository;

    public PacienteService(PacienteRepository pacienteRepository,
                           UserRepository userRepo, JWTService jwtService,
                           CuidadorRepository cuidadorRepository) {
        this.pacienteRepository = pacienteRepository;
        this.userRepo = userRepo;
        this.jwtService = jwtService;
        this.cuidadorRepository = cuidadorRepository;
    }

    @Transactional
    public AuthResponseDto registrar(RequestPatientDto dto) {
        Optional<User> existente = userRepo.findByPhoneNumber(dto.getPhoneNumber());
        if (existente.isPresent()) {
            throw new ConflictException("El teléfono ya está registrado");
        }
        Caregiver caregiver = null;
        Patient patient = PacienteMapper.toEntity(dto, caregiver);
        Patient guardado = pacienteRepository.save(patient);

        User user = guardado.getUser();

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponseDto(accessToken, refreshToken);
    }


    @Transactional
    public void vincularCuidador(String phoneNumberPaciente, String codigo) {
        Patient patient = userRepo.findByPhoneNumber(phoneNumberPaciente)
                .map(User::getPatient)
                .orElseThrow(() ->
                        new NotFoundException("Patient no encontrado"));
        Caregiver caregiver = cuidadorRepository.findByCodigoVinculacion(codigo)
                .orElseThrow(() ->
                        new BadRequestException("Código de caregiver no válido"));

        if (patient.getCaregiver() != null) throw new ForbiddenException("El patient ya está vinculado a un caregiver");

        patient.setCaregiver(caregiver);
        pacienteRepository.save(patient);
    }

    @Transactional(readOnly = true)
    public CaregiverInfoDto buscarCuidadorPorCodigo(String codigo) {
        Caregiver caregiver = cuidadorRepository
                .findByCodigoVinculacion(codigo).orElseThrow(() ->
                        new BadRequestException("Código de caregiver no válido"));
        return new CaregiverInfoDto(
                caregiver.getUser().getName(),
                caregiver.getUser().getPhoneNumber());
    }

    public void desvincularCuidador(String phoneNumber) {
        User user = userRepo.findByPhoneNumber(phoneNumber)
                .orElseThrow(() ->
                        new NotFoundException("Usuario no encontrado"));
        Patient patient = user.getPatient();
        if (patient == null)
            throw new ForbiddenException("El usuario no es un patient");
        if (patient.getCaregiver() == null)
            throw new ForbiddenException("El patient no tiene cuidador vinculado");
        patient.setCaregiver(null);
        pacienteRepository.save(patient);
    }

    public ResponsePatientDto obtenerPerfil(String phoneNumberUsuarioActual) {
        User user = userRepo.findByPhoneNumber(phoneNumberUsuarioActual)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Patient patient = pacienteRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Patient no encontrado para este usuario"));

        return PacienteMapper.toResponse(patient);
    }


    public void cambiarCuidador(String phoneNumber, String codigoCuidador) {
        User user = userRepo.findByPhoneNumber(phoneNumber)
                .orElseThrow(() ->
                        new NotFoundException("Usuario no encontrado"));
        Patient patient = user.getPatient();
        if (patient == null)
            throw new ForbiddenException("El usuario no es un patient");
        Caregiver caregiver = cuidadorRepository
                .findByCodigoVinculacion(codigoCuidador)
                .orElseThrow(() -> new NotFoundException("Caregiver no encontrado"));
        patient.setCaregiver(caregiver);
        pacienteRepository.save(patient);
    }

    @Transactional
    public ResponsePatientProfileDto actualizarPerfilPacienteDesdeCuidador(
            Patient patient,
            UpdatePatientProfileDto dto
    ) {
        validarCambioTelefono(patient, dto.getPhoneNumber());

        PacienteMapper.updatePacienteFromDto(dto, patient);

        return PacienteMapper.toResponsePerfil(patient);
    }

    @Transactional
    public UpdatePatientProfileResponseDto actualizarPerfilPropio(
            String phoneNumber,
            UpdatePatientProfileDto dto
    ) {

        Patient patient = pacienteRepository
                .findByUserPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("Patient no encontrado"));

        validarCambioTelefono(patient, dto.getPhoneNumber());

        boolean requiresReauth = PacienteMapper.updatePacienteFromDto(dto, patient);

        userRepo.save(patient.getUser());

        return new UpdatePatientProfileResponseDto(
                requiresReauth
                        ? "Teléfono actualizado. Se requiere iniciar sesión nuevamente."
                        : "Perfil actualizado correctamente",
                requiresReauth,
                PacienteMapper.toResponsePerfil(patient)
        );
    }

    private void validarCambioTelefono(Patient patient, String nuevoTelefono) {

        if (nuevoTelefono == null || nuevoTelefono.isBlank()) return;

        String actual = patient.getUser().getPhoneNumber();

        if (!nuevoTelefono.equals(actual)) {

            User existente = userRepo.findByPhoneNumber(nuevoTelefono).orElse(null);

            if (existente != null &&
                    !existente.getId().equals(patient.getUser().getId())) {

                throw new ConflictException("El teléfono ya está registrado");
            }
        }
    }
}