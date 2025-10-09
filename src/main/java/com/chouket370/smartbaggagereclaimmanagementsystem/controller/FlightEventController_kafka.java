package com.chouket370.smartbaggagereclaimmanagementsystem.controller;

import com.chouket370.smartbaggagereclaimmanagementsystem.Flight;
import com.chouket370.smartbaggagereclaimmanagementsystem.service.FlightService;
import com.chouket370.smartbaggagereclaimmanagementsystem.service.FlightUpdateProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/flight-events")
public class FlightEventController_kafka {
    private final FlightUpdateProducer flightUpdateProducer;
    private final FlightService flightService;

    public FlightEventController_kafka(FlightUpdateProducer flightUpdateProducer, FlightService flightService) {
        this.flightUpdateProducer = flightUpdateProducer;
        this.flightService = flightService;
    }

    @PostMapping("/{id}/arrival")
    public ResponseEntity<Void> publishArrival(@PathVariable Long id){
        Flight flight = flightService.getFlightById(id);
        flightUpdateProducer.publishArrival(flight);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/delay")
    public ResponseEntity<Void> publishDelay(@PathVariable Long id){
        Flight flight = flightService.getFlightById(id);
        flightUpdateProducer.publishDelay(flight);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> publishCancellationEvent(@PathVariable Long id) {
        Flight flight = flightService.getFlightById(id);
        flightUpdateProducer.publishCancellation(flight);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/GateChange")
    public ResponseEntity<Void> publishGateChange(@PathVariable Long id){
        Flight flight = flightService.getFlightById(id);
        flightUpdateProducer.publishGateChange(flight, flight.getGate());
        return ResponseEntity.ok().build();
    }
}