package com.meditrack.repository;

import com.meditrack.model.AlarmConfig;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlarmConfigRepository extends JpaRepository<AlarmConfig, Long> {
    @Query("""
    SELECT a FROM AlarmConfig a
    WHERE a.patient.id = :patientId
    AND a.active = true
    AND (a.endDate IS NULL OR a.endDate > CURRENT_TIMESTAMP)
""")
    List<AlarmConfig> findActiveAndCurrent(@Param("patientId") Long patientId);
    @Query("""
    SELECT a FROM AlarmConfig a
    WHERE a.patient.id = :patientId
    AND a.medicine.id = :medicineId
    AND a.active = true
    AND (a.endDate IS NULL OR a.endDate > CURRENT_TIMESTAMP)
""")
    List<AlarmConfig> findActiveAndCurrentByMedicine(
            Long patientId,
            Long medicineId
    );    @Modifying
    @Transactional
    @Query("""
        UPDATE AlarmConfig a
        SET a.active = false,
            a.updatedAt = :now
        WHERE a.active = true
        AND a.endDate IS NOT NULL
        AND a.endDate <= :now
    """)
    int deactivateExpired(@Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("""
    UPDATE Alarm a
    SET a.status = 'OMITTED',
        a.notified = true
    WHERE a.status = 'PENDING'
    AND a.scheduledAt <= :now
""")
    int omitPastPending(@Param("now") LocalDateTime now);
}