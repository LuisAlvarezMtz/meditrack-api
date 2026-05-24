package com.meditrack.controller;

import com.meditrack.dto.medicine.RequestMedicineDto;
import com.meditrack.dto.medicine.ResponseMedicineDto;
import com.meditrack.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseMedicineDto> register(
            @Valid @RequestBody RequestMedicineDto dto,
            Principal principal) {

        ResponseMedicineDto response =
                medicineService.registerMedicine(dto, principal.getName());
        return ResponseEntity.ok(response);
    }

    //Patient views their medicines
    @GetMapping("/my-medicines")
    public ResponseEntity<List<ResponseMedicineDto>> getMyMedicines
        (Principal principal) {
            return ResponseEntity.ok
                (medicineService.
                        getPatientMedicines(principal.getName()));
    }

    //Caregiver views those of a linked patient
    @GetMapping("/patient/{id}")
    public ResponseEntity<List<ResponseMedicineDto>> getPatientMedicines(
            @PathVariable Long id,
            Principal principal) {
        return ResponseEntity.ok(medicineService.
                getMedicinesOfPatient(id, principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseMedicineDto> getById(
            @PathVariable Long id,
            Principal principal) {

        return ResponseEntity.ok
                (medicineService.getById(id, principal.getName()));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ResponseMedicineDto> update(
            @PathVariable Long id,
            @RequestBody RequestMedicineDto dto,
            Principal principal) {

        return ResponseEntity.ok
                (medicineService.updateMedicine
                        (id, dto, principal.getName()));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicine(
            @PathVariable Long id,
            Principal principal) {
        medicineService.deactivateMedicine(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

}