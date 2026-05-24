package com.meditrack.service;

import com.meditrack.dto.medicine.RequestMedicineDto;
import com.meditrack.dto.medicine.ResponseMedicineDto;
import com.meditrack.exception.ForbiddenException;
import com.meditrack.exception.NotFoundException;
import com.meditrack.mapper.MedicinaMapper;
import com.meditrack.model.*;
import com.meditrack.repository.MedicinaRepository;
import com.meditrack.repository.UserRepository;
import com.meditrack.validation.EntidadValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicinaService {

    private final MedicinaRepository medicinaRepository;
    private final UserRepository userRepository;
    private final EntidadValidator entidadValidator;

    @Transactional
    public ResponseMedicineDto registrarMedicina
            (RequestMedicineDto dto, String phoneNumber) {

        User registradoPor = entidadValidator.usuario(phoneNumber);

        Patient patient = entidadValidator.resolverPaciente(registradoPor, dto.getPatientId());

        Medicine medicine = MedicinaMapper.toEntity(dto, patient, registradoPor);
        Medicine guardada = medicinaRepository.save(medicine);

        return MedicinaMapper.toResponse(guardada);
    }

    public List<ResponseMedicineDto> obtenerMedicinasDelPaciente(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (user.getRole() != Role.PATIENT) {
            throw new ForbiddenException("Solo los pacientes pueden usar este método");
        }

        Patient patient = user.getPatient();
        List<Medicine> medicines = medicinaRepository.findByPacienteAndEstado(patient, com.meditrack.model.Status.ACTIVE);

        return medicines.stream()
                .map(MedicinaMapper::toResponse)
                .toList();
    }


    public List<ResponseMedicineDto> obtenerMedicinasDePaciente(Long pacienteId, String phonNumberCuidador) {
        User user = userRepository.findByPhoneNumber(phonNumberCuidador)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (user.getRole() != Role.CAREGIVER) {
            throw new ForbiddenException("Solo los cuidadores pueden ver medicines de pacientes");
        }

        Caregiver caregiver = user.getCaregiver();

        Patient patient = caregiver.getPatients().stream()
                .filter(p -> p.getId().equals(pacienteId))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException(
                        "No puedes ver medicines de un patient no vinculado"));

        List<Medicine> medicines = medicinaRepository.findByPacienteAndEstado(patient, com.meditrack.model.Status.ACTIVE);

        return medicines.stream()
                .map(MedicinaMapper::toResponse)
                .toList();
    }

    public ResponseMedicineDto obtenerPorId(Long id, String phoneNumber) {
        Medicine medicine = medicinaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Medicine no encontrada"));

        validarAcceso(medicine.getPatient(), phoneNumber);

        return MedicinaMapper.toResponse(medicine);
    }

    public ResponseMedicineDto actualizarMedicina(Long id, RequestMedicineDto dto, String phoneNumber) {
        Medicine medicine = medicinaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Medicine no encontrada"));

        validarAcceso(medicine.getPatient(), phoneNumber);

        medicine.setName(dto.getName());
        medicine.setDosageForm(dto.getDosageForm());
        medicine.setExpirationDate(dto.getExpirationDate());

        Medicine guardada = medicinaRepository.save(medicine);
        return MedicinaMapper.toResponse(guardada);
    }

    @Transactional
    public void desactivarMedicina(Long id, String phoneNumber) {
        Medicine medicine = medicinaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Medicine no encontrada"));

        validarAcceso(medicine.getPatient(), phoneNumber);

        medicine.setStatus(com.meditrack.model.Status.INACTIVE);
        medicine.getAlarmConfigs().clear();

    }

    private void validarAcceso(Patient patient, String phoneNumber) {
        User user = entidadValidator.usuario(phoneNumber);
        entidadValidator.validarAcceso(patient, user);
    }

}
