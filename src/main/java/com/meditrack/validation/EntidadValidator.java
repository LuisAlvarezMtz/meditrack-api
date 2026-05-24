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

    public Patient paciente(String phoneNumber) {
        User user = usuario(phoneNumber);

        if (user.getRole() != Role.PATIENT) {
            throw new ForbiddenException("Solo pacientes pueden realizar esta acción");
        }
        return user.getPatient();
    }

    public Medicine medicinaValida(Long medicinaId, Patient patient) {
        Medicine medicine = medicinaRepository.findById(medicinaId)
                .orElseThrow(() -> new NotFoundException("Medicine no encontrada"));

        if (!medicine.getPatient().getId().equals(patient.getId())) {
            throw new ForbiddenException("No tienes acceso a esta medicine");
        }

        return medicine;
    }

    public AlarmConfig configValida(Long configId, User user) {

        AlarmConfig config = alarmaConfigRepository.findById(configId)
                .orElseThrow(() -> new NotFoundException("Config no encontrada"));

        Patient patient = config.getPatient();

        if (user.getRole() == Role.PATIENT) {
            if (!patient.getUser().getId().equals(user.getId())) {
                throw new ForbiddenException("No tienes acceso a esta configuración");
            }
        }

        if (user.getRole() == Role.CAREGIVER) {
            boolean vinculado = user.getCaregiver().getPatients().stream()
                    .anyMatch(p -> p.getId().equals(patient.getId()));

            if (!vinculado) {
                throw new ForbiddenException("Patient no vinculado");
            }
        }

        return config;
    }

    public Patient pacienteValidoParaUser(Long pacienteId, User user) {
        Patient patient = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new NotFoundException("Patient no encontrado"));

        validarAcceso(patient, user);

        return patient;
    }


    public Patient resolverPaciente(User user, Long pacienteId) {
        if (user.getRole() == Role.PATIENT) {
            return user.getPatient();
        }

        if (pacienteId == null) {
            throw new BadRequestException("Debe especificar pacienteId");
        }

        return pacienteValidoParaUser(pacienteId, user);
    }

    public void validarAcceso(Patient patient, User user) {
        if (user.getRole() == Role.PATIENT) {
            if (!Objects.equals(patient.getUser().getId(), user.getId()))
                throw new ForbiddenException("No puedes gestionar alarmas de otro patient");
        }
        if (user.getRole() == Role.CAREGIVER) {
            boolean vinculado = user.getCaregiver().getPatients().stream()
                    .anyMatch(p -> p.getId().equals(patient.getId()));
            if (!vinculado)
                throw new ForbiddenException("Patient no vinculado al cuidador");
        }
    }
}
