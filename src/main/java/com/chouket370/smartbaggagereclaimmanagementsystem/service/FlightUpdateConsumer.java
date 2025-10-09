package com.chouket370.smartbaggagereclaimmanagementsystem.service;

import com.chouket370.smartbaggagereclaimmanagementsystem.dto.FlightUpdateMessage;
import com.chouket370.smartbaggagereclaimmanagementsystem.repository.FlightRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class FlightUpdateConsumer {
    private final BeltAssignmentService beltAssignmentService;
    private final FlightRepository flightRepository;

    @KafkaListener(topics = "flight-updates", groupId = "flight-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(FlightUpdateMessage message) {
        System.out.println("Received flight event: " + message);

        switch(message.getUpdateType()){
            case ARRIVAL -> handleFlightArrival(message);
            case DELAY -> handleFlightdelay(message);
            case CANCELLATION -> handleFlightCancellation(message);
            case GATE_CHANGE -> handleGateChange(message);
            default -> System.out.println("Unhandled event: " + message.getUpdateType());
        }
    }

    private void handleFlightArrival(FlightUpdateMessage message) {
        flightRepository.findByFlightNumber(message.getFlightNumber()).ifPresent(flight -> {
            beltAssignmentService.assignBeltToFlight(flight);
        });
    }
    private void handleFlightdelay(FlightUpdateMessage message) {
        beltAssignmentService.reassignBeltDueToDelay(message.getFlightNumber());
    }
    private void handleFlightCancellation(FlightUpdateMessage message) {
        System.out.println("Not yet");
    }
    private void handleGateChange(FlightUpdateMessage message) {
        flightRepository.findByFlightNumber(message.getFlightNumber()).ifPresent(flight -> {
            String oldGate = flight.getGate();
            flight.setGate(message.getDetails());
            flightRepository.save(flight);

            log.info("Gate changed for flight {} from {} to {}", flight.getFlightNumber(), oldGate, flight.getGate());

        });
    }
}
