package com.meditrack.service;

import com.meditrack.dto.alarm.AlarmResponseDto;
import com.meditrack.exception.BadRequestException;
import com.meditrack.exception.NotFoundException;
import com.meditrack.validation.EntityValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.meditrack.model.*;
import com.meditrack.repository.AlarmRepository;
import org.springframework.transaction.annotation.Transactional;
import com.meditrack.mapper.AlarmMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final EntityValidator entidadValidator;

    private static final ZoneId TIME_ZONE = ZoneId.of("America/Mexico_City");

    @Transactional(readOnly = true)
    public List<AlarmResponseDto> getTodayAlarms(
            String phoneNumber,
            Long patientId
    ) {
        User user = entidadValidator.getUser(phoneNumber);
        Patient patient = entidadValidator.resolvePatient(user, patientId);

        LocalDateTime start = LocalDate.now(TIME_ZONE).atStartOfDay();
        LocalDateTime end = LocalDate.now(TIME_ZONE).atTime(23, 59, 59, 999999999);

        List<Alarm> alarms = alarmRepository.findTodayAlarms(
                patient.getId(),
                start,
                end
        );

        return alarms.stream()
                .map(AlarmMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public void updateStatus(Long alarmId, AlarmStatus status, String phoneNumber) {
        User user = entidadValidator.getUser(phoneNumber);

        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new NotFoundException("Alarm not found"));

        entidadValidator.validateConfig(alarm.getAlarmConfig().getId(), user);

        if (!alarm.getAlarmConfig().isActive()) {
            throw new BadRequestException("The alarm belongs to an inactive configuration");
        }

        alarm.setStatus(status);
        alarm.setNotified(true);
    }

    @Transactional(readOnly = true)
    public List<AlarmResponseDto> getHistory(
            String phoneNumber,
            Long patientId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        User user = entidadValidator.getUser(phoneNumber);
        Patient patient = entidadValidator.resolvePatient(user, patientId);

        LocalDateTime start = (startDate != null
                ? startDate
                : LocalDate.now(TIME_ZONE).minusDays(6))
                .atStartOfDay();

        LocalDateTime end = (endDate != null
                ? endDate
                : LocalDate.now(TIME_ZONE))
                .atTime(23, 59, 59, 999999999);

        List<Alarm> alarms = alarmRepository.findHistory(
                patient.getId(),
                start,
                end
        );

        return alarms.stream()
                .map(AlarmMapper::toResponseDto)
                .toList();
    }
}