package com.chouket370.smartbaggagereclaimmanagementsystem;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "flights")
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flightId;
    private String flightNumber;
    private String airline;
    private LocalDateTime scheduledArrival;
    private LocalDateTime actualArrival;
    private Integer passengerCount;
    @Enumerated(EnumType.STRING)
    private FlightStatus status;
    private String gate;
    private boolean isVip;
    private int totalBaggageCount;
    @Builder.Default
    private int unloadedBaggageCount = 0;


    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL)
    private List<BeltAssignment> beltAssignments;

    public boolean isDelayed() {
        return actualArrival != null &&
                actualArrival.isAfter(scheduledArrival.plusMinutes(15));
    }

    public long getDelayMinutes() {
        if (this.actualArrival == null || this.scheduledArrival == null) {
            return 0;
        }
        return Duration.between(this.scheduledArrival, this.actualArrival).toMinutes();
    }
    public void incrementUnloadedBaggageCount() {
        this.unloadedBaggageCount++;
    }
    @Transient
    public int getRemainingBaggageCount() {
        return this.totalBaggageCount - this.unloadedBaggageCount;
    }


}
