package com.meditrack.repository;

import com.meditrack.model.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    @Query("""
                SELECT a FROM Alarm a
                JOIN FETCH a.medicine m
                JOIN FETCH a.alarmConfig c
                WHERE a.patient.id = :patientId
                AND c.active = true
                AND (c.endDate IS NULL OR c.endDate > CURRENT_TIMESTAMP)
                AND a.scheduledAt BETWEEN :start AND :end
                ORDER BY a.scheduledAt ASC
            """)
    List<Alarm> findTodayAlarms(
            @Param("patientId") Long patientId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    void deleteByAlarmConfigIdAndScheduledAtGreaterThanEqual(Long configId, LocalDateTime scheduledAt);

    @Query("""
                SELECT a FROM Alarm a
                JOIN FETCH a.medicine m
                JOIN FETCH a.alarmConfig c
                WHERE a.patient.id = :patientId
                AND a.scheduledAt BETWEEN :start AND :end
                ORDER BY a.scheduledAt DESC
            """)
    List<Alarm> findHistory(
            @Param("patientId") Long patientId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}