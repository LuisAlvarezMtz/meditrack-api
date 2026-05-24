package com.meditrack.controller;

import com.meditrack.dto.alarm.AlarmResponseDto;
import com.meditrack.dto.alarmconfig.AlarmConfigRequestDto;
import com.meditrack.dto.alarmconfig.AlarmConfigResponseDto;
import com.meditrack.model.AlarmStatus;
import com.meditrack.service.AlarmConfigService;
import com.meditrack.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alarms")
public class AlarmConfigController {

    private final AlarmConfigService alarmConfigService;
    private final AlarmService alarmService;
    
    @PostMapping("/create")
    public ResponseEntity<AlarmConfigResponseDto> createAlarmConfiguration(
            @RequestBody AlarmConfigRequestDto alarmConfig,
            Principal principal
    ) {
        AlarmConfigResponseDto alarm =
                alarmConfigService.create(alarmConfig, principal.getName());

        return ResponseEntity.ok(alarm);
    }


    @GetMapping("/my-configurations")
    public ResponseEntity<List<AlarmConfigResponseDto>> getAlarmConfigurations(
            @RequestParam(required = false) Long patientId,
            Principal principal
    ) {
        return ResponseEntity.ok(
                alarmConfigService.getByPatient(
                        principal.getName(),
                        patientId
                )
        );
    }

    @GetMapping("/today")
    public ResponseEntity<List<AlarmResponseDto>> getTodayAlarms(
            @RequestParam(required = false) Long patientId,
            Principal principal
    ) {
        return ResponseEntity.ok(
                alarmService.getTodayAlarms(
                        principal.getName(),
                        patientId
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlarmConfigResponseDto> update(
            @PathVariable Long id,
            @RequestBody AlarmConfigRequestDto dto,
            Principal principal
    ) {
        return ResponseEntity.ok(
                alarmConfigService.update(id, dto, principal.getName())
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam AlarmStatus status,
            Principal principal
    ) {
        alarmService.updateStatus(id, status, principal.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Principal principal
    ) {
        alarmConfigService.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/medicine/{medicineId}")
    public ResponseEntity<List<AlarmConfigResponseDto>> getByMedicine(
            @PathVariable Long medicineId,
            @RequestParam(required = false) Long patientId,
            Principal principal
    ) {
        return ResponseEntity.ok(
                alarmConfigService.getByMedicineId(
                        medicineId,
                        principal.getName(),
                        patientId
                )
        );
    }

    @GetMapping("/history")
    public ResponseEntity<List<AlarmResponseDto>> getHistory(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Principal principal
    ) {
        return ResponseEntity.ok(
                alarmService.getHistory(
                        principal.getName(),
                        patientId,
                        startDate,
                        endDate
                )
        );
    }
}