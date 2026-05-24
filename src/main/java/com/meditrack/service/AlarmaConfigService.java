package com.meditrack.service;

import com.meditrack.dto.alarmaconfig.AlarmaConfigRequestDto;
import com.meditrack.dto.alarmaconfig.AlarmaConfigResponseDto;
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
    public AlarmaConfigResponseDto crear(
            AlarmaConfigRequestDto dto,
            String phoneNumber
    ) {

        LocalDateTime ahora = LocalDateTime.now(ZONA_HORARIA);

        dtoValidator.validarDto(dto);

        User user = entidadValidator.usuario(phoneNumber);

        Paciente paciente = entidadValidator.resolverPaciente(user, dto.getPacienteId());

        Medicina medicina = entidadValidator.medicinaValida(dto.getMedicinaId(), paciente);

        AlarmaConfig config = AlarmaConfigMapper.toEntity(dto, medicina);
        config.setPaciente(paciente);
        config.setActivo(true);
        config.setCreado(ahora);

        alarmaConfigRepository.save(config);

        List<Alarma> alarmas = generarAlarmas(config, ahora);
        alarmaRepository.saveAll(alarmas);

        return AlarmaConfigMapper.toResponseDTO(config);
    }

    /**
     * Genera las alarmas con base en la configuración.
     */
    private List<Alarma> generarAlarmas(AlarmaConfig config,
                                        LocalDateTime ahora) {
        List<Alarma> alarmas = new ArrayList<>();

        LocalDateTime fechaInicio = config.getFechaInicio();
        LocalDateTime fechaFin = config.getFechaFin();
        int frecuenciaHoras = config.getFrecuenciaHoras();

            long minutos = Duration.between(fechaInicio, fechaFin).toMinutes();
        long frecuenciaEnMinutos = frecuenciaHoras * 60L;

        long total = (long) Math.ceil((double) minutos / frecuenciaEnMinutos) + 1;

        if (total > MAX_ALARMAS) {
            throw new BadRequestException(
                    "El rango genera demasiadas alarmas, " +
                            "reduce el periodo o aumenta la frecuencia"
            );
        }
        LocalDateTime fecha = fechaInicio.isBefore(ahora) ? ahora : fechaInicio;
        // Generar alarmas futuras
        while (!fecha.isAfter(fechaFin)) {

            Alarma alarma = new Alarma();
            alarma.setAlarmaConfig(config);
            alarma.setPaciente(config.getPaciente());
            alarma.setMedicina(config.getMedicina());
            alarma.setFechaHora(fecha);
            alarma.setEstado(EstadoAlarma.PENDIENTE);
            alarma.setNotificada(false);
            alarma.setCreado(ahora);

            alarmas.add(alarma);

            fecha = fecha.plusMinutes(frecuenciaEnMinutos);
        }
        return alarmas;
    }

    /**
     * Obtiene todas las configuraciones activas de un paciente.
     */
    @Transactional(readOnly = true)
    public List<AlarmaConfigResponseDto> obtenerPorPaciente(
            String phoneNumber,
            Long pacienteId
    ) {
        User user = entidadValidator.usuario(phoneNumber);
        Paciente paciente= entidadValidator.resolverPaciente(user, pacienteId);

        List<AlarmaConfig> alarmas =
                alarmaConfigRepository.findActivasVigentes(paciente.getId());

        return alarmas.stream()
                .map(AlarmaConfigMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlarmaConfigResponseDto> obtenerPorMedicinaId(
            Long medicinaId,
            String phoneNumber,
            Long pacienteId
    ) {
        User user = entidadValidator.usuario(phoneNumber);
        Paciente paciente = entidadValidator.resolverPaciente(user, pacienteId);

        Medicina medicina = entidadValidator.medicinaValida(medicinaId, paciente);

        List<AlarmaConfig> configs =
                alarmaConfigRepository.findActivasVigentesPorMedicina(
                        paciente.getId(),
                        medicina.getId()
                );

        return configs.stream()
                .map(AlarmaConfigMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public AlarmaConfigResponseDto actualizar(
            Long configId,
            AlarmaConfigRequestDto dto,
            String phoneNumber
    ) {

        LocalDateTime ahora = LocalDateTime.now(ZONA_HORARIA);
        dtoValidator.validarDto(dto);

        User user = entidadValidator.usuario(phoneNumber);
        AlarmaConfig config = entidadValidator.configValida(configId, user);

        if (!config.isActivo()) {
            throw new BadRequestException("La configuración está inactiva");
        }

        Medicina medicina = entidadValidator.medicinaValida(
                dto.getMedicinaId(),
                config.getPaciente()
        );

        alarmaRepository.deleteByAlarmaConfigIdAndFechaHoraGreaterThanEqual(configId, ahora);
        // actualizar TODA la config
        config.setMedicina(medicina);
        config.setFechaInicio(dto.getFechaInicio());
        config.setFechaFin(dto.getFechaFin());
        config.setFrecuenciaHoras(dto.getFrecuenciaHoras());
        config.setActualizado(ahora);

        //regenerar
        List<Alarma> nuevas = generarAlarmas(config, ahora);

        alarmaRepository.saveAll(nuevas);

        return AlarmaConfigMapper.toResponseDTO(config);
    }


    @Transactional
    public void eliminar(Long configId, String phoneNumber) {

        User user = entidadValidator.usuario(phoneNumber);
        AlarmaConfig config = entidadValidator.configValida(configId, user);

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