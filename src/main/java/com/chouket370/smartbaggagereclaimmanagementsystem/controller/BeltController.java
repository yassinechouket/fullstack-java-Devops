package com.chouket370.smartbaggagereclaimmanagementsystem.controller;

import com.chouket370.smartbaggagereclaimmanagementsystem.BaggageBelt;
import com.chouket370.smartbaggagereclaimmanagementsystem.BeltCapacity;
import com.chouket370.smartbaggagereclaimmanagementsystem.BeltStatus;
import com.chouket370.smartbaggagereclaimmanagementsystem.dto.BaggageBeltRequestDTO;
import com.chouket370.smartbaggagereclaimmanagementsystem.dto.BaggageBeltResponseDTO;
import com.chouket370.smartbaggagereclaimmanagementsystem.service.BeltService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/belts")
@RequiredArgsConstructor
public class BeltController {

    private final BeltService beltService;

    @PostMapping
    public ResponseEntity<BaggageBeltResponseDTO> createBelt(@RequestBody BaggageBeltRequestDTO beltRequestDTO) {
        BaggageBelt createdBelt = beltService.saveBelt(convertToEntity(beltRequestDTO));
        return ResponseEntity.ok(convertToResponseDTO(createdBelt));
    }

    @GetMapping("/available")
    public ResponseEntity<List<BaggageBeltResponseDTO>> getAvailableBelts() {
        List<BaggageBelt> availableBelts = beltService.getBeltAvailability();
        List<BaggageBeltResponseDTO> responseDTOs = availableBelts.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaggageBeltResponseDTO> getBeltById(@PathVariable Long id) {
        Optional<BaggageBelt> belt = beltService.findBeltById(id);
        return belt.map(value -> ResponseEntity.ok(convertToResponseDTO(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/predict-availability")
    public ResponseEntity<List<BaggageBeltResponseDTO>> predictAvailability(
            @RequestParam int minutes) {
        List<BaggageBelt> predictedBelts = beltService.predictBeltAvailability(minutes);
        List<BaggageBeltResponseDTO> responseDTOs = predictedBelts.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @PutMapping("/{beltId}/under-maintenance")
    public ResponseEntity<Void> markUnderMaintenance(@PathVariable Long beltId) {
        beltService.markBeltUnderMaintenance2(beltId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status")
    public ResponseEntity<List<BaggageBeltResponseDTO>> findBeltsByStatus(@RequestParam BeltStatus status) {
        List<BaggageBelt> belts = beltService.findBaggageBeltByStatus(status);
        List<BaggageBeltResponseDTO> responseDTOs = belts.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @PutMapping("/{beltId}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long beltId,
                                             @RequestParam BeltStatus status) {
        beltService.updateBeltStatus(beltId, status);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{beltId}/return-to-service")
    public ResponseEntity<Void> returnToService(
            @PathVariable Long beltId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime maintenanceEndTime,
            @RequestParam boolean maintenanceCompleted,
            @RequestParam String maintenanceNotes,
            @RequestParam String maintainedBy) {
        beltService.returnBeltToService(beltId, maintenanceEndTime, maintenanceCompleted, maintenanceNotes, maintainedBy);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available-by-capacity")
    public ResponseEntity<Optional<BaggageBeltResponseDTO>> findAvailableByCapacity(@RequestParam BeltCapacity capacity) {
        Optional<BaggageBelt> belt = beltService.findAvailableBeltsByCapacity(capacity);
        return ResponseEntity.ok(belt.map(this::convertToResponseDTO));
    }

    @GetMapping("/available-before")
    public ResponseEntity<List<BaggageBeltResponseDTO>> findBeltsAvailableBefore(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time) {
        List<BaggageBelt> belts = beltService.findBeltsAvailableBefore(time);
        List<BaggageBeltResponseDTO> responseDTOs = belts.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/assigned-belt")
    public ResponseEntity<Optional<BaggageBeltResponseDTO>> getAssignedBeltForFlight(@RequestParam String flightNumber) {
        Optional<BaggageBelt> belt = beltService.getAssignedBeltForFlight(flightNumber);
        return ResponseEntity.ok(belt.map(this::convertToResponseDTO));
    }

    @GetMapping("/belt-number")
    public ResponseEntity<Optional<String>> getBeltNumberByFlightNumber(@RequestParam String flightNumber) {
        return ResponseEntity.ok(beltService.findBeltNumberByFlightNumber(flightNumber));
    }

    @GetMapping("/need-maintenance")
    public ResponseEntity<List<BaggageBeltResponseDTO>> findBeltsNeedingMaintenance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cutoffDate) {
        List<BaggageBelt> belts = beltService.findBeltsNeedingMaintenance(cutoffDate);
        List<BaggageBeltResponseDTO> responseDTOs = belts.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }


    private BaggageBelt convertToEntity(BaggageBeltRequestDTO requestDTO) {
        return BaggageBelt.builder()
                .beltNumber(requestDTO.getBeltNumber())
                .capacity(requestDTO.getCapacity())
                .status(requestDTO.getStatus())
                .location(requestDTO.getLocation())
                .maxBaggageCapacity(requestDTO.getMaxBaggageCapacity())
                .distanceToGate(requestDTO.getDistanceToGate())
                .expectedAvailableAt(requestDTO.getExpectedAvailableAt())
                .build();
    }

    private BaggageBeltResponseDTO convertToResponseDTO(BaggageBelt belt) {
        BaggageBeltResponseDTO responseDTO = new BaggageBeltResponseDTO();
        responseDTO.setId(belt.getId());
        responseDTO.setBeltNumber(belt.getBeltNumber());
        responseDTO.setLocation(belt.getLocation());
        responseDTO.setDistanceToGate(belt.getDistanceToGate());
        return responseDTO;
    }

    @PutMapping("/{id}/maintenance")
    public ResponseEntity<Void> markBeltForMaintenance(@PathVariable Long id) {
        beltService.markBeltUnderMaintenance(id);  // Calls the service + sends Kafka
        return ResponseEntity.noContent().build();
    }

}