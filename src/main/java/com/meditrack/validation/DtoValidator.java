package com.meditrack.validation;

import com.meditrack.dto.alarmconfig.AlarmConfigRequestDto;
import com.meditrack.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class DtoValidator {

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    public void validarDto(AlarmConfigRequestDto dto) {
        validarFechas(dto);
        validarFrecuencia(dto);
        validarFechaInicioNoPasada(dto.getStartDate());
    }

    private void validarFechas(AlarmConfigRequestDto dto) {
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("Fecha fin no puede ser menor a la fecha de inicio");
        }
    }

    private void validarFrecuencia(AlarmConfigRequestDto dto) {
        if (dto.getFrequencyHours() <= 0) {
            throw new BadRequestException("Frecuencia inválida");
        }

        if (dto.getFrequencyHours() > 24) {
            throw new BadRequestException("Frecuencia demasiado alta");
        }

    }

    private void validarFechaInicioNoPasada(LocalDateTime fechaInicio) {
        LocalDateTime ahora = LocalDateTime.now(ZONA)
                .withSecond(0)
                .withNano(0);

        LocalDateTime inicio = fechaInicio
                .withSecond(0)
                .withNano(0);

        if (inicio.isBefore(ahora.minusSeconds(10))) {
            throw new BadRequestException("La fecha y hora de inicio no puede ser menor a la actual");
        }
    }


}
