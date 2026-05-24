package com.meditrack.repository;

import com.meditrack.model.Medicine;
import com.meditrack.model.Patient;
import com.meditrack.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MedicineRepository
        extends JpaRepository<Medicine, Long> {
    List<Medicine> findByPatientAndStatus(Patient patient, Status status);
}