package com.chouket370.smartbaggagereclaimmanagementsystem.controller;

import com.chouket370.smartbaggagereclaimmanagementsystem.BeltAssignment;
import com.chouket370.smartbaggagereclaimmanagementsystem.Flight;
import com.chouket370.smartbaggagereclaimmanagementsystem.dto.BeltAssignmentRequestDTO;
import com.chouket370.smartbaggagereclaimmanagementsystem.dto.BeltAssignmentResponseDTO;
import com.chouket370.smartbaggagereclaimmanagementsystem.repository.FlightRepository;
import com.chouket370.smartbaggagereclaimmanagementsystem.service.BeltAssignmentService;
import com.chouket370.smartbaggagereclaimmanagementsystem.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/belt-assignments")
@RequiredArgsConstructor
public class BeltAssignmentController {

    private final BeltAssignmentService beltAssignmentService;
    private final FlightRepository flightRepository;

    @GetMapping("/active")
    public ResponseEntity<List<BeltAssignmentResponseDTO>> getActiveAssignments() {
        List<BeltAssignment> assignments = beltAssignmentService.getActiveBeltAssignments();
        List<BeltAssignmentResponseDTO> dtos = assignments.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }



    @PostMapping("/reassign-due-to-delay/{flightNumber}")
    public ResponseEntity<List<BeltAssignmentResponseDTO>> reassignDueToDelay(@PathVariable String flightNumber) {
        List<BeltAssignment> reassigned = beltAssignmentService.reassignBeltDueToDelay(flightNumber);
        List<BeltAssignmentResponseDTO> dtos = reassigned.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/emergency-reassign")
    public ResponseEntity<BeltAssignmentResponseDTO> handleEmergencyReassignment(
            @RequestParam Long brokenBeltId,
            @RequestParam String reason,
            @RequestParam String operatorId
    ) {
        BeltAssignment reassigned = beltAssignmentService.handleEmergencyReassignment(brokenBeltId, reason, operatorId);
        return ResponseEntity.ok(mapToResponseDTO(reassigned));
    }
    @PostMapping("/assign")
    public ResponseEntity<BeltAssignmentResponseDTO> assignBelt(@RequestBody BeltAssignmentRequestDTO requestDTO) {
        Flight flight = flightRepository.findById(requestDTO.getFlightId())
                .orElseThrow(() -> new RuntimeException("Flight not found with id: " + requestDTO.getFlightId()));

        BeltAssignment assignment = beltAssignmentService.assignBeltToFlight(flight);
        return ResponseEntity.ok(mapToResponseDTO(assignment));
    }

    private BeltAssignmentResponseDTO mapToResponseDTO(BeltAssignment assignment) {
        return BeltAssignmentResponseDTO.builder()
                .id(assignment.getId())
                .flightId(assignment.getFlight() != null ? assignment.getFlight().getFlightId() : null)
                .flightNumber(assignment.getFlight() != null ? assignment.getFlight().getFlightNumber() : null)
                .baggageBeltId(assignment.getBaggageBelt() != null ? assignment.getBaggageBelt().getId() : null)
                .beltNumber(assignment.getBaggageBelt() != null ? assignment.getBaggageBelt().getBeltNumber() : null)
                .assignedAt(assignment.getAssignedAt())
                .expectedReleaseAt(assignment.getExpectedReleaseAt())
                .actualReleaseAt(assignment.getActualReleaseAt())
                .status(assignment.getStatus())
                .priority(assignment.getPriority())
                .assignedBy(assignment.getAssignedBy())
                .notes(assignment.getNotes())
                .build();
    }
}