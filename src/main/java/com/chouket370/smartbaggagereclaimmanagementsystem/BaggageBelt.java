package com.chouket370.smartbaggagereclaimmanagementsystem;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "baggage_belts")
public class BaggageBelt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String beltNumber;
    @Enumerated(EnumType.STRING)
    private BeltCapacity capacity;
    @Enumerated(EnumType.STRING)
    private BeltStatus status;
    private String currentFlightNumber;
    private LocalDateTime assignedAt;
    private LocalDateTime expectedAvailableAt;
    private String location;
    private LocalDateTime lastMaintenanceAt;
    private Integer maxBaggageCapacity;
    private String MaintenanceNotes;
    private LocalDateTime NextMaintenanceCheck;
    private int distanceToGate;

    @OneToMany(mappedBy = "baggageBelt", cascade = CascadeType.ALL)
    private List<BeltAssignment> beltAssignments;


}
