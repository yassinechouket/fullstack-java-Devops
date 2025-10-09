package com.chouket370.smartbaggagereclaimmanagementsystem.dto;

import com.chouket370.smartbaggagereclaimmanagementsystem.FlightStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FlightResponseDTO {
    private Long flightId;
    private String flightNumber;
    private String airline;
    private LocalDateTime scheduledArrival;
    private FlightStatus flightStatus;
}
