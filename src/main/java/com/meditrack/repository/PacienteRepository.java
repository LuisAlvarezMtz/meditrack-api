package com.meditrack.repository;

import com.meditrack.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Patient, Long> {
    @Query("""
    SELECT p FROM Patient p JOIN FETCH p.user LEFT JOIN FETCH p.caregiver c  LEFT JOIN FETCH c.user WHERE p.user.id = :userId""")
    Optional<Patient> findByUserId(@Param("userId") Long userId);

    Optional<Patient> findByUserPhoneNumber(String phoneNumber);

}

