package com.chouket370.smartbaggagereclaimmanagementsystem;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "belt_assignments")
public class BeltAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", referencedColumnName = "flightId")
    private Flight flight;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "belt_id", referencedColumnName = "id")
    private BaggageBelt baggageBelt;
    @Column(nullable = false)
    private LocalDateTime assignedAt;
    @Column(nullable = false)
    private LocalDateTime expectedReleaseAt;
    private LocalDateTime actualReleaseAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;
    @Enumerated(EnumType.STRING)
    private AssignmentPriority priority;
    private String assignedBy;
    private String notes;
    private LocalDateTime releasedAt;
}
