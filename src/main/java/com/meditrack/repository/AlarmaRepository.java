package com.meditrack.repository;

import com.meditrack.model.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlarmaRepository extends JpaRepository<Alarm, Long> {
    @Query("""
                SELECT a FROM Alarm a
                JOIN FETCH a.medicine m
                JOIN FETCH a.alarmaConfig c
                WHERE a.patient.id = :pacienteId
                AND c.activo = true
                AND (c.fechaFin IS NULL OR c.fechaFin > CURRENT_TIMESTAMP)
                AND a.fechaHora BETWEEN :inicio AND :fin
                ORDER BY a.fechaHora ASC
            """)
    List<Alarm> findAlarmasDelDia(
            @Param("pacienteId") Long pacienteId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    void deleteByAlarmaConfigIdAndFechaHoraGreaterThanEqual(Long configId, LocalDateTime fecha);

    @Query("""
                SELECT a FROM Alarm a
                JOIN FETCH a.medicine m
                JOIN FETCH a.alarmaConfig c
                WHERE a.patient.id = :pacienteId
                AND a.fechaHora BETWEEN :inicio AND :fin
                ORDER BY a.fechaHora DESC
            """)
    List<Alarm> findHistorial(
            @Param("pacienteId") Long pacienteId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

}
