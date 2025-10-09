package com.chouket370.smartbaggagereclaimmanagementsystem.controller;

import com.chouket370.smartbaggagereclaimmanagementsystem.Flight;
import com.chouket370.smartbaggagereclaimmanagementsystem.FlightStatus;
import com.chouket370.smartbaggagereclaimmanagementsystem.dto.FlightRequestDTO;
import com.chouket370.smartbaggagereclaimmanagementsystem.dto.FlightResponseDTO;
import com.chouket370.smartbaggagereclaimmanagementsystem.repository.FlightRepository;
import com.chouket370.smartbaggagereclaimmanagementsystem.service.FlightService;
import com.chouket370.smartbaggagereclaimmanagementsystem.service.FlightUpdateProducer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.chouket370.smartbaggagereclaimmanagementsystem.service.FlightUpdateProducer.log;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;
    private final FlightRepository flightRepository;
    private final FlightUpdateProducer flightUpdateProducer;
    private static final Logger log = LoggerFactory.getLogger(FlightController.class);

    @PostMapping
    public ResponseEntity<FlightResponseDTO> createFlight(@RequestBody FlightRequestDTO flightRequestDTO) {
        Flight flight = convertToEntity(flightRequestDTO);
        Flight savedFlight = flightService.saveFlight(flight);
        return ResponseEntity.ok(convertToResponseDTO(savedFlight));
    }

    @GetMapping
    public ResponseEntity<List<FlightResponseDTO>> getAllFlights() {
        List<Flight> flights = flightService.getAllFlights();
        return ResponseEntity.ok(flights.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightResponseDTO> getFlightById(@PathVariable Long id) {
        Flight flight = flightService.getFlightById(id);
        return ResponseEntity.ok(convertToResponseDTO(flight));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/airline")
    public ResponseEntity<List<FlightResponseDTO>> getFlightsByAirline(@RequestParam String name) {
        List<Flight> flights = flightService.findByAirline(name);
        return ResponseEntity.ok(flights.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/late")
    public ResponseEntity<List<FlightResponseDTO>> getLateArrivals() {
        List<Flight> flights = flightService.getLateArrivals();
        return ResponseEntity.ok(flights.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/late-since")
    public ResponseEntity<List<FlightResponseDTO>> getLateArrivalsSince(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        List<Flight> flights = flightService.getLateArrivalsSince(since);
        return ResponseEntity.ok(flights.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/status")
    public ResponseEntity<List<FlightResponseDTO>> getFlightsByStatusOrdered(
            @RequestParam FlightStatus status) {
        List<Flight> flights = flightService.getFlightsByStatusOrderedByPassengers(status);
        return ResponseEntity.ok(flights.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/arrived-between")
    public ResponseEntity<List<FlightResponseDTO>> getArrivedBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Flight> flights = flightService.getFlightsArrivedBetween(start, end);
        return ResponseEntity.ok(flights.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/no-belt")
    public ResponseEntity<List<FlightResponseDTO>> getFlightsWithoutBelt() {
        List<Flight> flights = flightService.getLandedFlightsWithoutBelt();
        return ResponseEntity.ok(flights.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList()));
    }



    @GetMapping("/{id}/delayed")
    public ResponseEntity<Boolean> isFlightDelayed(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.isFlightDelayed(id));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<FlightStatus, Long>> getFlightStatusSummary() {
        return ResponseEntity.ok(flightService.getFlightStatusSummary());
    }

    @GetMapping("/completed-today")
    public ResponseEntity<List<FlightResponseDTO>> getCompletedFlightsToday() {
        List<Flight> flights = flightService.getCompletedFlightsToday();
        return ResponseEntity.ok(flights.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/unassigned")
    public ResponseEntity<List<FlightResponseDTO>> getUnassignedFlights() {
        List<Flight> flights = flightService.getFlightsWithoutAssignments();
        return ResponseEntity.ok(flights.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}/delay-duration")
    public ResponseEntity<Duration> getDelayDuration(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getFlightDelayDuration(id));
    }

    @GetMapping("/passengers")
    public ResponseEntity<List<FlightResponseDTO>> getFlightsWithMoreThanXPassengers(@RequestParam int count) {
        List<Flight> flights = flightService.getFlightsWithMoreThanXPassengers(count);
        return ResponseEntity.ok(flights.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/scheduled-after")
    public ResponseEntity<List<FlightResponseDTO>> getScheduledAfter(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time) {
        List<Flight> flights = flightService.findByScheduledArrivalAfter(time);
        return ResponseEntity.ok(flights.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList()));
    }


    private Flight convertToEntity(FlightRequestDTO dto) {
        return Flight.builder()
                .flightId(dto.getFlightId())
                .flightNumber(dto.getFlightNumber())
                .airline(dto.getAirline())
                .scheduledArrival(dto.getScheduledArrival())
                .actualArrival(dto.getActualArrival())
                .gate(dto.getGate())
                .isVip(dto.getIsVip())
                .passengerCount(dto.getPassengerCount())
                .totalBaggageCount(dto.getTotalBaggageCount())
                .status(dto.getStatus())
                .build();
    }

    private FlightResponseDTO convertToResponseDTO(Flight flight) {
        return FlightResponseDTO.builder()
                .flightId(flight.getFlightId())
                .flightNumber(flight.getFlightNumber())
                .airline(flight.getAirline())
                .scheduledArrival(flight.getScheduledArrival())
                .build();
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam FlightStatus status) {
        flightService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }







}