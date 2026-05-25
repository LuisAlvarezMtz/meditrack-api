package com.meditrack.validation;

import com.meditrack.dto.alarmconfig.AlarmConfigRequestDto;
import com.meditrack.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class DtoValidator {

    private static final ZoneId TIME_ZONE = ZoneId.of("America/Mexico_City");

    public void validate(AlarmConfigRequestDto dto) {
        validateDates(dto);
        validateFrequency(dto);
        validateStartDateNotInPast(dto.getStartDate());
    }

    private void validateDates(AlarmConfigRequestDto dto) {
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException( "End date cannot be before start date");
        }
    }

    private void validateFrequency(AlarmConfigRequestDto dto) {
        if (dto.getFrequencyHours() <= 0) {
            throw new BadRequestException( "Invalid frequency");
        }

        if (dto.getFrequencyHours() > 24) {
            throw new BadRequestException("Frequency too high");
        }

    }

    private void validateStartDateNotInPast(LocalDateTime startDate) {
        LocalDateTime now = LocalDateTime.now(TIME_ZONE)
                .withSecond(0)
                .withNano(0);

        LocalDateTime start = startDate
                .withSecond(0)
                .withNano(0);

        if (start.isBefore(now.minusSeconds(10))) {
            throw new BadRequestException("Start date and time cannot be in the past");
        }
    }


}
