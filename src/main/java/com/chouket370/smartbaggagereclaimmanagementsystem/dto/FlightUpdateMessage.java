package com.chouket370.smartbaggagereclaimmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Topic_Kafka
public class FlightUpdateMessage {
    private String flightNumber;
    private String airline;
    private FlightUpdateType updateType;
    private LocalDateTime timestamp;
    private String details;
}
