package com.meditrack.scheduler;

import com.meditrack.repository.AlarmConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmaScheduler {

    private final AlarmConfigRepository alarmaRepository;

    @Scheduled(fixedRate = 60000) // cada 1 minuto
    @Transactional
    public void desactivarAlarmasExpiradas() {

        LocalDateTime ahora = LocalDateTime
                .now(ZoneId.of("America/Mexico_City"));

        int actualizadas = alarmaRepository.deactivateExpired(ahora);

        if (actualizadas > 0) {
            log.info("Alarmas desactivadas automáticamente: {}", actualizadas);
        }
    }

    @Scheduled(fixedRate = 5 * 60 * 1000) // cada 5 minutos
    @Transactional
    public void omitirAlarmasPendientesPasadas() {
        LocalDateTime hace5Min = LocalDateTime
                .now(ZoneId.of("America/Mexico_City"))
                .minusMinutes(5); // ← 5 minutos de gracia

        int omitidas = alarmaRepository.omitPastPending(hace5Min);

        if (omitidas > 0) {
            log.info("Alarmas marcadas como OMITIDAS automáticamente: {}", omitidas);
        }
    }
}