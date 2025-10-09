package com.chouket370.smartbaggagereclaimmanagementsystem.dto;

import com.chouket370.smartbaggagereclaimmanagementsystem.AssignmentPriority;
import com.chouket370.smartbaggagereclaimmanagementsystem.Flight;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BeltAssignmentRequestDTO {
    private Long flightId;
}
