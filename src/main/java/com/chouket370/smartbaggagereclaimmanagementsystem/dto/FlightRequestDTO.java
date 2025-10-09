package com.chouket370.smartbaggagereclaimmanagementsystem.dto;

import com.chouket370.smartbaggagereclaimmanagementsystem.FlightStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlightRequestDTO {
    private Long flightId;
    private String flightNumber;
    private String airline;
    private LocalDateTime scheduledArrival;
    private LocalDateTime actualArrival;
    private String gate;
    private Boolean isVip;
    private Integer passengerCount;
    private Integer totalBaggageCount;
    private FlightStatus status;
}
