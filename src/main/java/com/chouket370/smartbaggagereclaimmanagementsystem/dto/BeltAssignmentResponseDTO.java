package com.chouket370.smartbaggagereclaimmanagementsystem.dto;

import com.chouket370.smartbaggagereclaimmanagementsystem.AssignmentPriority;
import com.chouket370.smartbaggagereclaimmanagementsystem.AssignmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BeltAssignmentResponseDTO {
    private Long id;
    private Long flightId;
    private String flightNumber;
    private Long baggageBeltId;
    private String beltNumber;
    private LocalDateTime assignedAt;
    private LocalDateTime expectedReleaseAt;
    private LocalDateTime actualReleaseAt;
    private AssignmentStatus status;
    private AssignmentPriority priority;
    private String assignedBy;
    private String notes;
}