package com.chouket370.smartbaggagereclaimmanagementsystem.service;

import com.chouket370.smartbaggagereclaimmanagementsystem.Flight;
import com.chouket370.smartbaggagereclaimmanagementsystem.dto.FlightUpdateMessage;
import com.chouket370.smartbaggagereclaimmanagementsystem.dto.FlightUpdateType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FlightUpdateProducer {

    private final KafkaTemplate<String, FlightUpdateMessage> kafkaTemplate;

    public static final Logger log = LoggerFactory.getLogger(FlightUpdateProducer.class);
    private static final String TOPIC = "flight-updates";

    public void sendFlightUpdate(Flight flight, FlightUpdateType updateType, String details) {
        FlightUpdateMessage message = FlightUpdateMessage.builder()
                .flightNumber(flight.getFlightNumber())
                .airline(flight.getAirline())
                .updateType(updateType)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();

        try {
            kafkaTemplate.send(TOPIC, message.getFlightNumber(), message);
            log.info("Flight update message sent: {}", message);
        } catch (Exception e) {
            log.error("Failed to send flight update message", e);
        }
    }


    public void publishArrival(Flight flight) {
        sendFlightUpdate(flight, FlightUpdateType.ARRIVAL,
                "Flight has landed at gate " + flight.getGate());
    }

    public void publishDelay(Flight flight) {
        sendFlightUpdate(flight, FlightUpdateType.DELAY,
                "Flight delayed by " + flight.getDelayMinutes() + " minutes.");
    }

    public void publishGateChange(Flight flight, String oldGate) {
        sendFlightUpdate(flight, FlightUpdateType.GATE_CHANGE,
                "Gate changed from " + oldGate + " to " + flight.getGate());
    }

    public void publishCancellation(Flight flight) {
        sendFlightUpdate(flight, FlightUpdateType.CANCELLATION,
                "Flight has been cancelled.");
    }
}
