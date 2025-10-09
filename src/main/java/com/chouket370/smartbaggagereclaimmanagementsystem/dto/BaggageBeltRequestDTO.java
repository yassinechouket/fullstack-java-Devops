package com.chouket370.smartbaggagereclaimmanagementsystem.dto;

import com.chouket370.smartbaggagereclaimmanagementsystem.BeltCapacity;
import com.chouket370.smartbaggagereclaimmanagementsystem.BeltStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BaggageBeltRequestDTO {
    private String beltNumber;
    private BeltCapacity capacity;
    private BeltStatus status;
    private String location;
    private Integer maxBaggageCapacity;
    private Integer distanceToGate;
    private LocalDateTime expectedAvailableAt;
}