package com.chouket370.smartbaggagereclaimmanagementsystem.repository;

import com.chouket370.smartbaggagereclaimmanagementsystem.Flight;
import com.chouket370.smartbaggagereclaimmanagementsystem.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByAirline(String airline);
    List<Flight> findByScheduledArrivalAfter(java.time.LocalDateTime time);
    List<Flight> findByPassengerCountGreaterThanEqual( Integer passengerCount);
    @Query("SELECT e FROM Flight e WHERE e.actualArrival > e.scheduledArrival")
    List<Flight> findLateArrivals();
    @Query("SELECT e FROM Flight e WHERE e.actualArrival - e.scheduledArrival> :x")
    List<Flight> findLateArrivals(@Param("x") java.time.LocalDateTime x);
    @Query("SELECT e FROM Flight e WHERE e.status = :status ORDER BY e.passengerCount DESC")
    List<Flight> findByStatusOrderByPassengerCountDesc(@Param("status") FlightStatus status);
    List<Flight> findByStatusAndActualArrivalBetween(
            FlightStatus status,
            LocalDateTime start,
            LocalDateTime end
    );
    @Query("SELECT f FROM Flight f WHERE f.actualArrival IS NOT NULL " +
            "AND NOT EXISTS (SELECT ba FROM BeltAssignment ba WHERE ba.flight = f AND ba.status = 'ACTIVE')")
    List<Flight> findLandedFlightsWithoutBeltAssignment();
    @Query("SELECT f FROM Flight f WHERE f.actualArrival BETWEEN :start AND :end")
    List<Flight> findByActualArrivalBetween(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    @Query("SELECT f FROM Flight f WHERE f.beltAssignments IS EMPTY")
    List<Flight> findFlightsWithoutAssignments();
    Optional<Flight> findByFlightNumber(String flightNumber);

}
