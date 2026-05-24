package com.meditrack.repository;

import com.meditrack.model.Medicine;
import com.meditrack.model.Patient;
import com.meditrack.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MedicinaRepository
        extends JpaRepository<Medicine, Long> {
    List<Medicine> findByPacienteAndEstado(Patient patient, Status status);
}

