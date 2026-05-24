package com.meditrack.validation;

import com.meditrack.exception.BadRequestException;
import com.meditrack.exception.ForbiddenException;
import com.meditrack.exception.NotFoundException;
import com.meditrack.model.*;
import com.meditrack.repository.AlarmaConfigRepository;
import com.meditrack.repository.MedicinaRepository;
import com.meditrack.repository.PacienteRepository;
import com.meditrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EntidadValidator {

    private final UserRepository userRepository;
    private final MedicinaRepository medicinaRepository;
    private final AlarmaConfigRepository alarmaConfigRepository;
    private final PacienteRepository pacienteRepository;

    public User usuario(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    public Paciente paciente(String phoneNumber) {
        User user = usuario(phoneNumber);

        if (user.getRol() != Rol.PACIENTE) {
            throw new ForbiddenException("Solo pacientes pueden realizar esta acción");
        }
        return user.getPaciente();
    }

    public Medicina medicinaValida(Long medicinaId, Paciente paciente) {
        Medicina medicina = medicinaRepository.findById(medicinaId)
                .orElseThrow(() -> new NotFoundException("Medicina no encontrada"));

        if (!medicina.getPaciente().getId().equals(paciente.getId())) {
            throw new ForbiddenException("No tienes acceso a esta medicina");
        }

        return medicina;
    }

    public AlarmaConfig configValida(Long configId, User user) {

        AlarmaConfig config = alarmaConfigRepository.findById(configId)
                .orElseThrow(() -> new NotFoundException("Config no encontrada"));

        Paciente paciente = config.getPaciente();

        if (user.getRol() == Rol.PACIENTE) {
            if (!paciente.getUser().getId().equals(user.getId())) {
                throw new ForbiddenException("No tienes acceso a esta configuración");
            }
        }

        if (user.getRol() == Rol.CUIDADOR) {
            boolean vinculado = user.getCuidador().getPacientes().stream()
                    .anyMatch(p -> p.getId().equals(paciente.getId()));

            if (!vinculado) {
                throw new ForbiddenException("Paciente no vinculado");
            }
        }

        return config;
    }

    public Paciente pacienteValidoParaUser(Long pacienteId, User user) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new NotFoundException("Paciente no encontrado"));

        validarAcceso(paciente, user);

        return paciente;
    }


    public Paciente resolverPaciente(User user, Long pacienteId) {
        if (user.getRol() == Rol.PACIENTE) {
            return user.getPaciente();
        }

        if (pacienteId == null) {
            throw new BadRequestException("Debe especificar pacienteId");
        }

        return pacienteValidoParaUser(pacienteId, user);
    }

    public void validarAcceso(Paciente paciente, User user) {
        if (user.getRol() == Rol.PACIENTE) {
            if (!Objects.equals(paciente.getUser().getId(), user.getId()))
                throw new ForbiddenException("No puedes gestionar alarmas de otro paciente");
        }
        if (user.getRol() == Rol.CUIDADOR) {
            boolean vinculado = user.getCuidador().getPacientes().stream()
                    .anyMatch(p -> p.getId().equals(paciente.getId()));
            if (!vinculado)
                throw new ForbiddenException("Paciente no vinculado al cuidador");
        }
    }
}
