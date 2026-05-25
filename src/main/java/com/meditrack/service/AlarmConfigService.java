package com.meditrack.service;

import com.meditrack.dto.alarmconfig.AlarmConfigRequestDto;
import com.meditrack.dto.alarmconfig.AlarmConfigResponseDto;
import com.meditrack.exception.BadRequestException;
import com.meditrack.validation.DtoValidator;
import com.meditrack.validation.EntityValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.meditrack.mapper.AlarmConfigMapper;
import com.meditrack.model.*;
import com.meditrack.repository.alarmConfigRepository;
import com.meditrack.repository.AlarmRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmConfigService {

    private final alarmConfigRepository alarmConfigRepository;
    private final AlarmRepository alarmRepository;
    private final EntityValidator entidadValidator;
    private final DtoValidator dtoValidator;

    private static final ZoneId TIME_ZONE = ZoneId.of("America/Mexico_City");
    private static final int MAX_ALARMS = 1000;

    /**
     * Creates an alarm configuration and automatically generates
     * the corresponding alarms.
     */
    @Transactional
    public AlarmConfigResponseDto create(
            AlarmConfigRequestDto dto,
            String phoneNumber
    ) {

        LocalDateTime now = LocalDateTime.now(TIME_ZONE);

        dtoValidator.validate(dto);

        User user = entidadValidator.getUser(phoneNumber);

        Patient patient = entidadValidator.resolvePatient(user, dto.getPatientId());

        Medicine medicine = entidadValidator.validateMedicine(dto.getMedicineId(), patient);

        AlarmConfig config = AlarmConfigMapper.toEntity(dto, medicine);
        config.setPatient(patient);
        config.setActive(true);
        config.setCreatedAt(now);

        alarmConfigRepository.save(config);

        List<Alarm> alarms = generateAlarms(config, now);
        alarmRepository.saveAll(alarms);

        return AlarmConfigMapper.toResponseDto(config);
    }

    /**
     * Generates alarms based on the configuration.
     */
    private List<Alarm> generateAlarms(AlarmConfig config,
                                       LocalDateTime now) {
        List<Alarm> alarms = new ArrayList<>();

        LocalDateTime startDate = config.getStartDate();
        LocalDateTime endDate = config.getEndDate();
        int frequencyHours = config.getFrequencyHours();

        long minutes = Duration.between(startDate, endDate).toMinutes();
        long frequencyInMinutes = frequencyHours * 60L;

        long total = (long) Math.ceil((double) minutes / frequencyInMinutes) + 1;

        if (total > MAX_ALARMS) {
            throw new BadRequestException(
                    "The range generates too many alarms, " +
                            "reduce the period or increase the frequency"
            );
        }
        LocalDateTime currentDate = startDate.isBefore(now) ? now : startDate;
        // Generate future alarms
        while (!currentDate.isAfter(endDate)) {

            Alarm alarm = new Alarm();
            alarm.setAlarmConfig(config);
            alarm.setPatient(config.getPatient());
            alarm.setMedicine(config.getMedicine());
            alarm.setScheduledAt(currentDate);
            alarm.setStatus(AlarmStatus.PENDING);
            alarm.setNotified(false);
            alarm.setCreatedAt(now);

            alarms.add(alarm);

            currentDate = currentDate.plusMinutes(frequencyInMinutes);
        }
        return alarms;
    }

    /**
     * Retrieves all active configurations for a patient.
     */
    @Transactional(readOnly = true)
    public List<AlarmConfigResponseDto> getByPatient(
            String phoneNumber,
            Long patientId
    ) {
        User user = entidadValidator.getUser(phoneNumber);
        Patient patient = entidadValidator.resolvePatient(user, patientId);

        List<AlarmConfig> alarms =
                alarmConfigRepository.findActiveAndCurrent(patient.getId());

        return alarms.stream()
                .map(AlarmConfigMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlarmConfigResponseDto> getByMedicineId(
            Long medicineId,
            String phoneNumber,
            Long patientId
    ) {
        User user = entidadValidator.getUser(phoneNumber);
        Patient patient = entidadValidator.resolvePatient(user, patientId);

        Medicine medicine = entidadValidator.validateMedicine(medicineId, patient);

        List<AlarmConfig> configs =
                alarmConfigRepository.findActiveAndCurrentByMedicine(
                        patient.getId(),
                        medicine.getId()
                );

        return configs.stream()
                .map(AlarmConfigMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public AlarmConfigResponseDto update(
            Long configId,
            AlarmConfigRequestDto dto,
            String phoneNumber
    ) {

        LocalDateTime now = LocalDateTime.now(TIME_ZONE);
        dtoValidator.validate(dto);

        User user = entidadValidator.getUser(phoneNumber);
        AlarmConfig config = entidadValidator.validateConfig(configId, user);

        if (!config.isActive()) {
            throw new BadRequestException("The configuration is inactive");
        }

        Medicine medicine = entidadValidator.validateMedicine(
                dto.getMedicineId(),
                config.getPatient()
        );

        alarmRepository.deleteByAlarmConfigIdAndScheduledAtGreaterThanEqual(configId, now);
        // update the entire config
        config.setMedicine(medicine);
        config.setStartDate(dto.getStartDate());
        config.setEndDate(dto.getEndDate());
        config.setFrequencyHours(dto.getFrequencyHours());
        config.setUpdatedAt(now);

        // regenerate
        List<Alarm> newAlarms = generateAlarms(config, now);

        alarmRepository.saveAll(newAlarms);

        return AlarmConfigMapper.toResponseDto(config);
    }


    @Transactional
    public void delete(Long configId, String phoneNumber) {

        User user = entidadValidator.getUser(phoneNumber);
        AlarmConfig config = entidadValidator.validateConfig(configId, user);

        if (!config.isActive()) {
            throw new BadRequestException("The configuration is already deleted");
        }
        config.setActive(false);
        LocalDateTime now  = LocalDateTime.now(TIME_ZONE);

        config.setUpdatedAt(now);

        // delete future alarms
        alarmRepository.deleteByAlarmConfigIdAndScheduledAtGreaterThanEqual(configId, now);
    }
}