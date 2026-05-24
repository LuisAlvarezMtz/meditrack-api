package com.meditrack.service;

import com.meditrack.dto.alarm.AlarmResponseDto;
import com.meditrack.exception.BadRequestException;
import com.meditrack.exception.NotFoundException;
import com.meditrack.validation.EntidadValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.meditrack.model.*;
import com.meditrack.repository.*;
import org.springframework.transaction.annotation.Transactional;
import com.meditrack.mapper.AlarmMapper;

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
    public List<AlarmResponseDto> obtenerAlarmasDelDia(
            String phoneNumber,
            Long pacienteId
    ) {
        User user = entidadValidator.usuario(phoneNumber);
        Patient patient = entidadValidator.resolverPaciente(user, pacienteId);

        LocalDateTime inicio = LocalDate.now(ZONA_HORARIA).atStartOfDay();
        LocalDateTime fin = LocalDate.now(ZONA_HORARIA).atTime(23, 59, 59, 999999999);

        List<Alarm> alarms = alarmaRepository.findAlarmasDelDia(
                patient.getId(),
                inicio,
                fin
        );

        return alarms.stream()
                .map(AlarmMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public void actualizarEstado(Long alarmaId, AlarmStatus estado, String phoneNumber) {
        User user = entidadValidator.usuario(phoneNumber);

        Alarm alarm = alarmaRepository.findById(alarmaId)
                .orElseThrow(() -> new NotFoundException("Alarm no encontrada"));

        entidadValidator.configValida(alarm.getAlarmConfig().getId(), user);

        if (!alarm.getAlarmConfig().isActivo()) {
            throw new BadRequestException("La alarm pertenece a una configuración inactiva");
        }

        alarm.setEstado(estado);
        alarm.setNotificada(true);
    }

    @Transactional(readOnly = true)
    public List<AlarmResponseDto> obtenerHistorial(
            String phoneNumber,
            Long pacienteId,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {
        User user = entidadValidator.usuario(phoneNumber);
        Patient patient = entidadValidator.resolverPaciente(user, pacienteId);

        LocalDateTime inicio = (fechaInicio != null
                ? fechaInicio
                : LocalDate.now(ZONA_HORARIA).minusDays(6))
                .atStartOfDay();

        LocalDateTime fin = (fechaFin != null
                ? fechaFin
                : LocalDate.now(ZONA_HORARIA))
                .atTime(23, 59, 59, 999999999);

        List<Alarm> alarms = alarmaRepository.findHistorial(
                patient.getId(),
                inicio,
                fin
        );

        return alarms.stream()
                .map(AlarmMapper::toResponseDto)
                .toList();
    }
}