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
import com.meditrack.model.Cuidador;
import com.meditrack.model.Paciente;
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
        Cuidador cuidador = CuidadorMapper.toEntity(dto);

        User userGuardado = userRepo.save(cuidador.getUser());
        cuidador.setUser(userGuardado);

        cuidadorRepository.save(cuidador);

        String accessToken = jwtService.generateToken(userGuardado);
        String refreshToken = jwtService.generateRefreshToken(userGuardado);

        return new AuthResponseDto(accessToken, refreshToken);
    }

    public UpdateCuidadorResponseDto actualizarCuidador(String phoneNumber, UpdateCuidadorDto dto) {

        Cuidador cuidador = cuidadorRepository.findByUserPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("Cuidador no encontrado"));

        if (dto.getPhoneNumber() != null &&
                !dto.getPhoneNumber().equals(cuidador.getUser().getPhoneNumber())) {

            if (userRepo.existsByPhoneNumber(dto.getPhoneNumber())) {
                throw new ConflictException("El número ya está en uso");
            }
        }
        boolean requiresReauth = CuidadorMapper.updateEntity(cuidador, dto);

        cuidadorRepository.save(cuidador);

        return new UpdateCuidadorResponseDto(
                requiresReauth
                        ? "Teléfono actualizado. Se requiere iniciar sesión nuevamente."
                        : "Datos actualizados correctamente",
                requiresReauth,
                CuidadorMapper.toResponse(cuidador)
        );
    }


    public List<ResponsePacienteDto> obtenerPacientesDeCuidador(String phoneNumberCuidador) {
        Cuidador cuidador = obtenerCuidador(phoneNumberCuidador);
        return cuidador.getPacientes().stream()
                .map(PacienteMapper::toResponse)
                .toList();
    }

    public ResponseCuidadorDto obtenerMisDatos(String phoneNumberCuidador) {
        Cuidador cuidador = cuidadorRepository.findByUserPhoneNumber(phoneNumberCuidador)
                .orElseThrow(() -> new NotFoundException("Cuidador no encontrado"));

        return CuidadorMapper.toResponse(cuidador);
    }

    @Transactional
    public ResponsePacienteDto registrarPacienteDesdeCuidador(
            String phoneNumberCuidador,
            RequestPacienteDto dto) {

        Cuidador cuidador = obtenerCuidador(phoneNumberCuidador);
        if (userRepo.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
            throw new ConflictException("El número ya está registrado");
        }
        Paciente paciente = PacienteMapper.toEntity(dto, cuidador);
        pacienteRepository.save(paciente);

        return PacienteMapper.toResponse(paciente);
    }


    @Transactional
    public void desvincularPaciente(Long pacienteId, String phoneNumber) {

        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new NotFoundException("Paciente no encontrado"));

        if (paciente.getCuidador() == null ||
                !paciente.getCuidador().getUser().getPhoneNumber().equals(phoneNumber)) {
            throw new ForbiddenException("No puedes desvincular este paciente");
        }

        paciente.setCuidador(null);
    }

    @Transactional
    public ResponsePacientePerfilDto actualizarPacienteDesdeCuidador(
            Long pacienteId,
            String phoneCuidador,
            UpdatePacientePerfilDto dto
    ) {

        Cuidador cuidador = obtenerCuidador(phoneCuidador);
        Paciente paciente = obtenerPaciente(pacienteId);

        if (paciente.getCuidador() == null ||
                !paciente.getCuidador().getId().equals(cuidador.getId())) {

            throw new ForbiddenException("Este paciente no pertenece al cuidador");
        }

        return pacienteService.actualizarPerfilPacienteDesdeCuidador(paciente, dto);
    }

    @Transactional(readOnly = true)
    public ResponsePacientePerfilDto obtenerPacienteDesdeCuidador(
            Long pacienteId,
            String phoneCuidador
    ) {

        Cuidador cuidador = obtenerCuidador(phoneCuidador);

        Paciente paciente = obtenerPaciente(pacienteId);

        if (!paciente.getCuidador().getId().equals(cuidador.getId())) {
            throw new ForbiddenException("Este paciente no pertenece al cuidador");
        }

        return PacienteMapper.toResponsePerfil(paciente);
    }

    private Paciente obtenerPaciente(Long pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new NotFoundException("Paciente no encontrado"));
    }

    private Cuidador obtenerCuidador(String phoneCuidador) {
        return cuidadorRepository
                .findByUserPhoneNumber(phoneCuidador)
                .orElseThrow(() -> new NotFoundException("Cuidador no encontrado"));
    }
}