package com.meditrack.mapper;


import com.meditrack.dto.alarma.AlarmaResponseDto;
import com.meditrack.model.Alarm;

public class AlarmaMapper {

    private AlarmaMapper() {}

    public static AlarmaResponseDto toResponseDto(Alarm entity) {
        AlarmaResponseDto dto = new AlarmaResponseDto();
        dto.setId(entity.getId());
        dto.setAlarmaConfigId(entity.getAlarmConfig().getId());
        dto.setMedicinaId(entity.getMedicine().getId());
        dto.setMedicinaNombre(entity.getMedicine().getNombre());
        dto.setDosageForm(entity.getMedicine().getDosageForm());
        dto.setFechaHora(entity.getFechaHora());
        dto.setEstado(entity.getEstado());
        dto.setNotificada(entity.isNotificada());
        return dto;
    }
}