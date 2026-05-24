package com.meditrack.service;

import com.meditrack.dto.medicina.RequestMedicinaDto;
import com.meditrack.dto.medicina.ResponseMedicinaDto;
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
    public ResponseMedicinaDto registrarMedicina
            (RequestMedicinaDto dto, String phoneNumber) {

        User registradoPor = entidadValidator.usuario(phoneNumber);

        Paciente paciente = entidadValidator.resolverPaciente(registradoPor, dto.getPacienteId());

        Medicina medicina = MedicinaMapper.toEntity(dto, paciente, registradoPor);
        Medicina guardada = medicinaRepository.save(medicina);

        return MedicinaMapper.toResponse(guardada);
    }

    public List<ResponseMedicinaDto> obtenerMedicinasDelPaciente(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (user.getRol() != Rol.PACIENTE) {
            throw new ForbiddenException("Solo los pacientes pueden usar este método");
        }

        Paciente paciente = user.getPaciente();
        List<Medicina> medicinas = medicinaRepository.findByPacienteAndEstado(paciente, Estado.ACTIVO);

        return medicinas.stream()
                .map(MedicinaMapper::toResponse)
                .toList();
    }


    public List<ResponseMedicinaDto> obtenerMedicinasDePaciente(Long pacienteId, String phonNumberCuidador) {
        User user = userRepository.findByPhoneNumber(phonNumberCuidador)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (user.getRol() != Rol.CUIDADOR) {
            throw new ForbiddenException("Solo los cuidadores pueden ver medicinas de pacientes");
        }

        Cuidador cuidador = user.getCuidador();

        Paciente paciente = cuidador.getPacientes().stream()
                .filter(p -> p.getId().equals(pacienteId))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException(
                        "No puedes ver medicinas de un paciente no vinculado"));

        List<Medicina> medicinas = medicinaRepository.findByPacienteAndEstado(paciente, Estado.ACTIVO);

        return medicinas.stream()
                .map(MedicinaMapper::toResponse)
                .toList();
    }

    public ResponseMedicinaDto obtenerPorId(Long id, String phoneNumber) {
        Medicina medicina = medicinaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Medicina no encontrada"));

        validarAcceso(medicina.getPaciente(), phoneNumber);

        return MedicinaMapper.toResponse(medicina);
    }

    public ResponseMedicinaDto actualizarMedicina(Long id, RequestMedicinaDto dto, String phoneNumber) {
        Medicina medicina = medicinaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Medicina no encontrada"));

        validarAcceso(medicina.getPaciente(), phoneNumber);

        medicina.setNombre(dto.getNombre());
        medicina.setDosageForm(dto.getDosageForm());
        medicina.setExpirationDate(dto.getExpirationDate());

        Medicina guardada = medicinaRepository.save(medicina);
        return MedicinaMapper.toResponse(guardada);
    }

    @Transactional
    public void desactivarMedicina(Long id, String phoneNumber) {
        Medicina medicina = medicinaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Medicina no encontrada"));

        validarAcceso(medicina.getPaciente(), phoneNumber);

        medicina.setEstado(Estado.INACTIVO);
        medicina.getAlarmasConfig().clear();

    }

    private void validarAcceso(Paciente paciente, String phoneNumber) {
        User user = entidadValidator.usuario(phoneNumber);
        entidadValidator.validarAcceso(paciente, user);
    }

}
