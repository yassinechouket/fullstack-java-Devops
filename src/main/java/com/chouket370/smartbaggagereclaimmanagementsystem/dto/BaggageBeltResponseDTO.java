package com.chouket370.smartbaggagereclaimmanagementsystem.dto;

import com.chouket370.smartbaggagereclaimmanagementsystem.BeltCapacity;
import com.chouket370.smartbaggagereclaimmanagementsystem.BeltStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaggageBeltResponseDTO {
    private Long id;
    private String beltNumber;
    private String location;
    private int distanceToGate;
}