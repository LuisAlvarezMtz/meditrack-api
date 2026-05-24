package com.meditrack.service;

import com.meditrack.dto.auth.AuthResponseDto;
import com.meditrack.dto.cuidador.RequestCuidadorDto;
import com.meditrack.dto.cuidador.ResponseCuidadorDto;
import com.meditrack.dto.cuidador.UpdateCuidadorDto;
import com.meditrack.dto.cuidador.UpdateCuidadorResponseDto;
import com.meditrack.dto.paciente.RequestPacienteDto;
import com.meditrack.dto.paciente.ResponsePacienteDto;
import com.meditrack.dto.paciente.ResponsePacientePerfilDto;
import com.meditrack.dto.paciente.UpdatePacientePerfilDto;
import com.meditrack.exception.ConflictException;
import com.meditrack.exception.ForbiddenException;
import com.meditrack.exception.NotFoundException;
import com.meditrack.mapper.CuidadorMapper;
import com.meditrack.mapper.PacienteMapper;
import com.meditrack.model.Caregiver;
import com.meditrack.model.Patient;
import com.meditrack.model.User;
import com.meditrack.repository.CuidadorRepository;
import com.meditrack.repository.PacienteRepository;
import com.meditrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CuidadorService {

    private final CuidadorRepository cuidadorRepository;
    private final UserRepository userRepo;
    private final PacienteRepository pacienteRepository;
    private final PacienteService pacienteService;
    private final JWTService jwtService;


    @Transactional
    public AuthResponseDto registrar(RequestCuidadorDto dto) {

        Optional<User> existente = userRepo.findByPhoneNumber(dto.getPhoneNumber());
        if (existente.isPresent()) {
            throw new ConflictException("El Teléfono ya está registrado");
        }
        Caregiver caregiver = CuidadorMapper.toEntity(dto);

        User userGuardado = userRepo.save(caregiver.getUser());
        caregiver.setUser(userGuardado);

        cuidadorRepository.save(caregiver);

        String accessToken = jwtService.generateToken(userGuardado);
        String refreshToken = jwtService.generateRefreshToken(userGuardado);

        return new AuthResponseDto(accessToken, refreshToken);
    }

    public UpdateCuidadorResponseDto actualizarCuidador(String phoneNumber, UpdateCuidadorDto dto) {

        Caregiver caregiver = cuidadorRepository.findByUserPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("Caregiver no encontrado"));

        if (dto.getPhoneNumber() != null &&
                !dto.getPhoneNumber().equals(caregiver.getUser().getPhoneNumber())) {

            if (userRepo.existsByPhoneNumber(dto.getPhoneNumber())) {
                throw new ConflictException("El número ya está en uso");
            }
        }
        boolean requiresReauth = CuidadorMapper.updateEntity(caregiver, dto);

        cuidadorRepository.save(caregiver);

        return new UpdateCuidadorResponseDto(
                requiresReauth
                        ? "Teléfono actualizado. Se requiere iniciar sesión nuevamente."
                        : "Datos actualizados correctamente",
                requiresReauth,
                CuidadorMapper.toResponse(caregiver)
        );
    }


    public List<ResponsePacienteDto> obtenerPacientesDeCuidador(String phoneNumberCuidador) {
        Caregiver caregiver = obtenerCuidador(phoneNumberCuidador);
        return caregiver.getPatients().stream()
                .map(PacienteMapper::toResponse)
                .toList();
    }

    public ResponseCuidadorDto obtenerMisDatos(String phoneNumberCuidador) {
        Caregiver caregiver = cuidadorRepository.findByUserPhoneNumber(phoneNumberCuidador)
                .orElseThrow(() -> new NotFoundException("Caregiver no encontrado"));

        return CuidadorMapper.toResponse(caregiver);
    }

    @Transactional
    public ResponsePacienteDto registrarPacienteDesdeCuidador(
            String phoneNumberCuidador,
            RequestPacienteDto dto) {

        Caregiver caregiver = obtenerCuidador(phoneNumberCuidador);
        if (userRepo.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
            throw new ConflictException("El número ya está registrado");
        }
        Patient patient = PacienteMapper.toEntity(dto, caregiver);
        pacienteRepository.save(patient);

        return PacienteMapper.toResponse(patient);
    }


    @Transactional
    public void desvincularPaciente(Long pacienteId, String phoneNumber) {

        Patient patient = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new NotFoundException("Patient no encontrado"));

        if (patient.getCaregiver() == null ||
                !patient.getCaregiver().getUser().getPhoneNumber().equals(phoneNumber)) {
            throw new ForbiddenException("No puedes desvincular este patient");
        }

        patient.setCaregiver(null);
    }

    @Transactional
    public ResponsePacientePerfilDto actualizarPacienteDesdeCuidador(
            Long pacienteId,
            String phoneCuidador,
            UpdatePacientePerfilDto dto
    ) {

        Caregiver caregiver = obtenerCuidador(phoneCuidador);
        Patient patient = obtenerPaciente(pacienteId);

        if (patient.getCaregiver() == null ||
                !patient.getCaregiver().getId().equals(caregiver.getId())) {

            throw new ForbiddenException("Este patient no pertenece al caregiver");
        }

        return pacienteService.actualizarPerfilPacienteDesdeCuidador(patient, dto);
    }

    @Transactional(readOnly = true)
    public ResponsePacientePerfilDto obtenerPacienteDesdeCuidador(
            Long pacienteId,
            String phoneCuidador
    ) {

        Caregiver caregiver = obtenerCuidador(phoneCuidador);

        Patient patient = obtenerPaciente(pacienteId);

        if (!patient.getCaregiver().getId().equals(caregiver.getId())) {
            throw new ForbiddenException("Este patient no pertenece al caregiver");
        }

        return PacienteMapper.toResponsePerfil(patient);
    }

    private Patient obtenerPaciente(Long pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new NotFoundException("Patient no encontrado"));
    }

    private Caregiver obtenerCuidador(String phoneCuidador) {
        return cuidadorRepository
                .findByUserPhoneNumber(phoneCuidador)
                .orElseThrow(() -> new NotFoundException("Caregiver no encontrado"));
    }
}