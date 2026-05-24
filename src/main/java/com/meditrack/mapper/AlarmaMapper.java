package com.meditrack.mapper;


import com.meditrack.dto.alarma.AlarmaResponseDto;
import com.meditrack.model.Alarma;

public class AlarmaMapper {

    private AlarmaMapper() {}

    public static AlarmaResponseDto toResponseDto(Alarma entity) {
        AlarmaResponseDto dto = new AlarmaResponseDto();
        dto.setId(entity.getId());
        dto.setAlarmaConfigId(entity.getAlarmaConfig().getId());
        dto.setMedicinaId(entity.getMedicina().getId());
        dto.setMedicinaNombre(entity.getMedicina().getNombre());
        dto.setDosageForm(entity.getMedicina().getDosageForm());
        dto.setFechaHora(entity.getFechaHora());
        dto.setEstado(entity.getEstado());
        dto.setNotificada(entity.isNotificada());
        return dto;
    }
}