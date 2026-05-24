package com.meditrack.service;

import com.meditrack.dto.alarma.AlarmaResponseDto;
import com.meditrack.exception.BadRequestException;
import com.meditrack.exception.NotFoundException;
import com.meditrack.validation.EntidadValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.meditrack.model.*;
import com.meditrack.repository.*;
import org.springframework.transaction.annotation.Transactional;
import com.meditrack.mapper.AlarmaMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmaService {

    private final AlarmaRepository alarmaRepository;
    private final EntidadValidator entidadValidator;

    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Mexico_City");

    @Transactional(readOnly = true)
    public List<AlarmaResponseDto> obtenerAlarmasDelDia(
            String phoneNumber,
            Long pacienteId
    ) {
        User user = entidadValidator.usuario(phoneNumber);
        Paciente paciente = entidadValidator.resolverPaciente(user, pacienteId);

        LocalDateTime inicio = LocalDate.now(ZONA_HORARIA).atStartOfDay();
        LocalDateTime fin = LocalDate.now(ZONA_HORARIA).atTime(23, 59, 59, 999999999);

        List<Alarma> alarmas = alarmaRepository.findAlarmasDelDia(
                paciente.getId(),
                inicio,
                fin
        );

        return alarmas.stream()
                .map(AlarmaMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public void actualizarEstado(Long alarmaId, EstadoAlarma estado, String phoneNumber) {
        User user = entidadValidator.usuario(phoneNumber);

        Alarma alarma = alarmaRepository.findById(alarmaId)
                .orElseThrow(() -> new NotFoundException("Alarma no encontrada"));

        entidadValidator.configValida(alarma.getAlarmaConfig().getId(), user);

        if (!alarma.getAlarmaConfig().isActivo()) {
            throw new BadRequestException("La alarma pertenece a una configuración inactiva");
        }

        alarma.setEstado(estado);
        alarma.setNotificada(true);
    }

    @Transactional(readOnly = true)
    public List<AlarmaResponseDto> obtenerHistorial(
            String phoneNumber,
            Long pacienteId,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {
        User user = entidadValidator.usuario(phoneNumber);
        Paciente paciente = entidadValidator.resolverPaciente(user, pacienteId);

        LocalDateTime inicio = (fechaInicio != null
                ? fechaInicio
                : LocalDate.now(ZONA_HORARIA).minusDays(6))
                .atStartOfDay();

        LocalDateTime fin = (fechaFin != null
                ? fechaFin
                : LocalDate.now(ZONA_HORARIA))
                .atTime(23, 59, 59, 999999999);

        List<Alarma> alarmas = alarmaRepository.findHistorial(
                paciente.getId(),
                inicio,
                fin
        );

        return alarmas.stream()
                .map(AlarmaMapper::toResponseDto)
                .toList();
    }
}