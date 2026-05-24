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
public interface AlarmaConfigRepository extends JpaRepository<AlarmConfig, Long> {
    @Query("""
    SELECT a FROM AlarmConfig a
    WHERE a.patient.id = :pacienteId
    AND a.activo = true
    AND (a.fechaFin IS NULL OR a.fechaFin > CURRENT_TIMESTAMP)
""")
    List<AlarmConfig> findActivasVigentes(@Param("pacienteId") Long pacienteId);
    @Query("""
    SELECT a FROM AlarmConfig a
    WHERE a.patient.id = :pacienteId
    AND a.medicine.id = :medicinaId
    AND a.activo = true
    AND (a.fechaFin IS NULL OR a.fechaFin > CURRENT_TIMESTAMP)
""")
    List<AlarmConfig> findActivasVigentesPorMedicina(
            Long pacienteId,
            Long medicinaId
    );    @Modifying
    @Transactional
    @Query("""
        UPDATE AlarmConfig a
        SET a.activo = false,
            a.actualizado = :ahora
        WHERE a.activo = true
        AND a.fechaFin IS NOT NULL
        AND a.fechaFin <= :ahora
    """)
    int desactivarExpiradas(@Param("ahora") LocalDateTime ahora);

    @Modifying
    @Transactional
    @Query("""
    UPDATE Alarma a
    SET a.status = 'OMITIDA',
        a.notificada = true
    WHERE a.status = 'PENDIENTE'
    AND a.fechaHora <= :ahora
""")
    int omitirPendientesPasadas(@Param("ahora") LocalDateTime ahora);
}
