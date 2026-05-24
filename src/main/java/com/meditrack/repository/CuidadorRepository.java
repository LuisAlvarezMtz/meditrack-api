    package com.meditrack.repository;
    
    import com.meditrack.model.Caregiver;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import java.util.Optional;
    
    @Repository
    public interface CuidadorRepository extends JpaRepository<Caregiver, Long> {
        Optional<Caregiver> findByCodigoVinculacion(String codigoVinculacion);
        Optional<Caregiver> findByUserPhoneNumber(String phoneNumber);
        Optional<Caregiver> findByUserId(Long userId);
    }


