package com.meditrack.scheduler;

import com.meditrack.repository.alarmConfigRepository;
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
public class AlarmScheduler {

    private final alarmConfigRepository alarmaRepository;
    private static final ZoneId TIME_ZONE = ZoneId.of("America/Mexico_City");

    @Scheduled(fixedRate = 60000) // every 1 minute
    @Transactional
    public void deactivateExpiredAlarms() {

        LocalDateTime now = LocalDateTime
                .now(TIME_ZONE);

        int updated = alarmaRepository.deactivateExpired(now);

        if (updated > 0) {
            log.info("Alarms automatically deactivated: {}", updated);
        }
    }

    @Scheduled(fixedRate = 5 * 60 * 1000) // every 5 minutes
    @Transactional
    public void omitPastPendingAlarms() {
        LocalDateTime fiveMinutesAgo = LocalDateTime
                .now(TIME_ZONE)
                .minusMinutes(5); // ← 5-minute grace period

        int omitted = alarmaRepository.omitPastPending(fiveMinutesAgo);

        if (omitted > 0) {
            log.info("Alarms automatically marked as OMITTED: {}", omitted);
        }
    }
}