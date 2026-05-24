package com.meditrack.service;

import com.meditrack.dto.alarmconfig.AlarmConfigRequestDto;
import com.meditrack.dto.alarmconfig.AlarmConfigResponseDto;
import com.meditrack.exception.BadRequestException;
import com.meditrack.validation.DtoValidator;
import com.meditrack.validation.EntidadValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.meditrack.mapper.AlarmaConfigMapper;
import com.meditrack.model.*;
import com.meditrack.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmaConfigService {

    private final AlarmaConfigRepository alarmaConfigRepository;
    private final AlarmaRepository alarmaRepository;
    private final EntidadValidator entidadValidator;
    private final DtoValidator dtoValidator;

    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Mexico_City");
    private static final int MAX_ALARMAS = 1000;

    /**
     * Crea una configuración de alarma y genera automáticamente
     * las alarmas correspondientes.
     */
    @Transactional
    public AlarmConfigResponseDto crear(
            AlarmConfigRequestDto dto,
            String phoneNumber
    ) {

        LocalDateTime ahora = LocalDateTime.now(ZONA_HORARIA);

        dtoValidator.validarDto(dto);

        User user = entidadValidator.usuario(phoneNumber);

        Patient patient = entidadValidator.resolverPaciente(user, dto.getPacienteId());

        Medicine medicine = entidadValidator.medicinaValida(dto.getMedicinaId(), patient);

        AlarmConfig config = AlarmaConfigMapper.toEntity(dto, medicine);
        config.setPatient(patient);
        config.setActivo(true);
        config.setCreado(ahora);

        alarmaConfigRepository.save(config);

        List<Alarm> alarms = generarAlarmas(config, ahora);
        alarmaRepository.saveAll(alarms);

        return AlarmaConfigMapper.toResponseDTO(config);
    }

    /**
     * Genera las alarmas con base en la configuración.
     */
    private List<Alarm> generarAlarmas(AlarmConfig config,
                                       LocalDateTime ahora) {
        List<Alarm> alarms = new ArrayList<>();

        LocalDateTime fechaInicio = config.getFechaInicio();
        LocalDateTime fechaFin = config.getFechaFin();
        int frecuenciaHoras = config.getFrecuenciaHoras();

            long minutos = Duration.between(fechaInicio, fechaFin).toMinutes();
        long frecuenciaEnMinutos = frecuenciaHoras * 60L;

        long total = (long) Math.ceil((double) minutos / frecuenciaEnMinutos) + 1;

        if (total > MAX_ALARMAS) {
            throw new BadRequestException(
                    "El rango genera demasiadas alarms, " +
                            "reduce el periodo o aumenta la frecuencia"
            );
        }
        LocalDateTime fecha = fechaInicio.isBefore(ahora) ? ahora : fechaInicio;
        // Generar alarms futuras
        while (!fecha.isAfter(fechaFin)) {

            Alarm alarm = new Alarm();
            alarm.setAlarmConfig(config);
            alarm.setPatient(config.getPatient());
            alarm.setMedicine(config.getMedicine());
            alarm.setFechaHora(fecha);
            alarm.setEstado(AlarmStatus.PENDING);
            alarm.setNotificada(false);
            alarm.setCreado(ahora);

            alarms.add(alarm);

            fecha = fecha.plusMinutes(frecuenciaEnMinutos);
        }
        return alarms;
    }

    /**
     * Obtiene todas las configuraciones activas de un paciente.
     */
    @Transactional(readOnly = true)
    public List<AlarmConfigResponseDto> obtenerPorPaciente(
            String phoneNumber,
            Long pacienteId
    ) {
        User user = entidadValidator.usuario(phoneNumber);
        Patient patient = entidadValidator.resolverPaciente(user, pacienteId);

        List<AlarmConfig> alarmas =
                alarmaConfigRepository.findActivasVigentes(patient.getId());

        return alarmas.stream()
                .map(AlarmaConfigMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlarmConfigResponseDto> obtenerPorMedicinaId(
            Long medicinaId,
            String phoneNumber,
            Long pacienteId
    ) {
        User user = entidadValidator.usuario(phoneNumber);
        Patient patient = entidadValidator.resolverPaciente(user, pacienteId);

        Medicine medicine = entidadValidator.medicinaValida(medicinaId, patient);

        List<AlarmConfig> configs =
                alarmaConfigRepository.findActivasVigentesPorMedicina(
                        patient.getId(),
                        medicine.getId()
                );

        return configs.stream()
                .map(AlarmaConfigMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public AlarmConfigResponseDto actualizar(
            Long configId,
            AlarmConfigRequestDto dto,
            String phoneNumber
    ) {

        LocalDateTime ahora = LocalDateTime.now(ZONA_HORARIA);
        dtoValidator.validarDto(dto);

        User user = entidadValidator.usuario(phoneNumber);
        AlarmConfig config = entidadValidator.configValida(configId, user);

        if (!config.isActivo()) {
            throw new BadRequestException("La configuración está inactiva");
        }

        Medicine medicine = entidadValidator.medicinaValida(
                dto.getMedicinaId(),
                config.getPatient()
        );

        alarmaRepository.deleteByAlarmaConfigIdAndFechaHoraGreaterThanEqual(configId, ahora);
        // actualizar TODA la config
        config.setMedicine(medicine);
        config.setFechaInicio(dto.getFechaInicio());
        config.setFechaFin(dto.getFechaFin());
        config.setFrecuenciaHoras(dto.getFrecuenciaHoras());
        config.setActualizado(ahora);

        //regenerar
        List<Alarm> nuevas = generarAlarmas(config, ahora);

        alarmaRepository.saveAll(nuevas);

        return AlarmaConfigMapper.toResponseDTO(config);
    }


    @Transactional
    public void eliminar(Long configId, String phoneNumber) {

        User user = entidadValidator.usuario(phoneNumber);
        AlarmConfig config = entidadValidator.configValida(configId, user);

        if (!config.isActivo()) {
            throw new BadRequestException("La configuración ya está eliminada");
        }
        config.setActivo(false);
        LocalDateTime ahora = LocalDateTime.now(ZONA_HORARIA);

        config.setActualizado(ahora);

        // eliminar futuras alarmas
        alarmaRepository.deleteByAlarmaConfigIdAndFechaHoraGreaterThanEqual(configId, ahora);
    }
}