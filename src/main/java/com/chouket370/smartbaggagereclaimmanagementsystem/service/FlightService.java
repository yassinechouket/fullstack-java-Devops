package com.chouket370.smartbaggagereclaimmanagementsystem.service;


import com.chouket370.smartbaggagereclaimmanagementsystem.Flight;
import com.chouket370.smartbaggagereclaimmanagementsystem.FlightStatus;
import com.chouket370.smartbaggagereclaimmanagementsystem.repository.FlightRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.chouket370.smartbaggagereclaimmanagementsystem.service.FlightUpdateProducer.log;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final FlightUpdateProducer flightUpdateProducer;


    public List<Flight> findByAirline(String airline) {
        return flightRepository.findByAirline(airline);
    }
    public Flight saveFlight(Flight flight) {
        return flightRepository.save(flight);
    }
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }
    public Flight getFlightById(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flight not found with id: " + id));
    }
    public void deleteFlight(Long id){
        if (!flightRepository.existsById(id)) {
            throw new EntityNotFoundException("Flight not found with id: " + id);
        }
        flightRepository.deleteById(id);
    }
    public List<Flight> findByScheduledArrivalAfter(LocalDateTime time) {
        return flightRepository.findByScheduledArrivalAfter(time);
    }
    public List<Flight> getFlightsWithMoreThanXPassengers(int x) {
        return flightRepository.findByPassengerCountGreaterThanEqual(x);
    }
    public List<Flight> getLateArrivals() {
        return flightRepository.findLateArrivals();
    }
    public List<Flight> getLateArrivalsSince(LocalDateTime x) {
        return flightRepository.findLateArrivals(x);
    }
    public List<Flight> getFlightsByStatusOrderedByPassengers(FlightStatus status) {
        return flightRepository.findByStatusOrderByPassengerCountDesc(status);
    }
    public List<Flight> getFlightsArrivedBetween(LocalDateTime start, LocalDateTime end) {
        return flightRepository.findByActualArrivalBetween(start, end);
    }
    public List<Flight> getLandedFlightsWithoutBelt() {
        return flightRepository.findLandedFlightsWithoutBeltAssignment();
    }
    public void updateFlightStatus(Long flightId, FlightStatus newStatus) {
        Flight flight=flightRepository.findById(flightId)
                .orElseThrow(() -> new EntityNotFoundException("Flight not found with id: " + flightId));
        flight.setStatus(newStatus);
        flightRepository.save(flight);
    }
    public boolean isFlightDelayed(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found"));
        return flight.getActualArrival() != null &&
                flight.getActualArrival().isBefore(LocalDateTime.now());
    }
    public Map<FlightStatus, Long> getFlightStatusSummary(){
        return flightRepository.findAll().stream()
                .collect(Collectors.groupingBy(Flight::getStatus, Collectors.counting()));
    }
    public List<Flight> getCompletedFlightsToday() {
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = LocalDateTime.now();
        return flightRepository.findByActualArrivalBetween(start, end);
    }
    public List<Flight> getFlightsWithoutAssignments(){
        return flightRepository.findFlightsWithoutAssignments();
    }
    public Duration getFlightDelayDuration(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new EntityNotFoundException("Flight not found with id: " + flightId));
        if(flight.getActualArrival() == null){
            return Duration.ZERO;
        }
        return Duration.between(flight.getActualArrival(), LocalDateTime.now());
    }


    public void updateStatus(Long id, FlightStatus status) {
        Flight flight = getFlightById(id);
        System.out.println("Updating status for flight: " + flight.getFlightNumber() + " -> " + status);
        flight.setStatus(status);
        flightRepository.save(flight);

        switch (status) {
            case LANDED -> flightUpdateProducer.publishArrival(flight);
            case CANCELLED -> flightUpdateProducer.publishCancellation(flight);
            case DELAYED -> flightUpdateProducer.publishDelay(flight);
            default -> log.info("No Kafka event needed for status: {}", status);
        }
    }



}
