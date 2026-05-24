package com.meditrack.mapper;

import com.meditrack.dto.alarmaconfig.AlarmaConfigRequestDto;
import com.meditrack.dto.alarmaconfig.AlarmaConfigResponseDto;
import com.meditrack.model.AlarmConfig;
import com.meditrack.model.Medicine;

import java.time.LocalDateTime;

public class AlarmaConfigMapper {

    private AlarmaConfigMapper() {}

    public static AlarmConfig toEntity(
            AlarmaConfigRequestDto dto,
            Medicine medicine
    ) {
        AlarmConfig config = new AlarmConfig();
        config.setMedicine(medicine);
        config.setFechaInicio(dto.getFechaInicio());
        config.setFechaFin(dto.getFechaFin());
        config.setFrecuenciaHoras(dto.getFrecuenciaHoras());
        config.setActivo(true);
        config.setCreado(LocalDateTime.now());
        config.setActualizado(LocalDateTime.now());
        return config;
    }

    public static AlarmaConfigResponseDto toResponseDTO(AlarmConfig entity) {
        AlarmaConfigResponseDto dto = new AlarmaConfigResponseDto();
        dto.setId(entity.getId());
        dto.setPacienteId(entity.getPatient().getId());
        dto.setMedicinaId(entity.getMedicine().getId());
        dto.setMedicinaNombre(entity.getMedicine().getNombre());
        dto.setFechaInicio(entity.getFechaInicio());
        dto.setFechaFin(entity.getFechaFin());
        dto.setFrecuenciaHoras(entity.getFrecuenciaHoras());
        dto.setActivo(entity.isActivo());
        dto.setCreado(entity.getCreado());
        return dto;
    }
}
