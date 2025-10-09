package com.chouket370.smartbaggagereclaimmanagementsystem.repository;

import com.chouket370.smartbaggagereclaimmanagementsystem.BaggageBelt;
import com.chouket370.smartbaggagereclaimmanagementsystem.BeltCapacity;
import com.chouket370.smartbaggagereclaimmanagementsystem.BeltStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BaggageBeltRepository extends CrudRepository<BaggageBelt, Long> {
    @Query("SELECT e FROM BaggageBelt e WHERE e.status = com.chouket370.smartbaggagereclaimmanagementsystem.BeltStatus.AVAILABLE")
    List<BaggageBelt> findAvailable();
    @Query("SELECT e FROM BaggageBelt e WHERE e.expectedAvailableAt <= :targetTime")
    List<BaggageBelt> findBeltsExpectedFreeBy(@Param("targetTime") LocalDateTime time);
    @Query("SELECT e.baggageBelt.beltNumber FROM BeltAssignment e WHERE e.flight.flightNumber = :flightNumber AND e.status = com.chouket370.smartbaggagereclaimmanagementsystem.AssignmentStatus.ACTIVE")
    Optional<String> findBeltNumberByFlightNumber(@Param("flightNumber") String flightNumber);
    @Query("SELECT b FROM BaggageBelt b WHERE b.status = com.chouket370.smartbaggagereclaimmanagementsystem.BeltStatus.AVAILABLE OR b.expectedAvailableAt <= :availableBefore")
    List<BaggageBelt> findBeltsAvailableBefore(@Param("availableBefore") LocalDateTime availableBefore);
    @Query("SELECT e FROM BaggageBelt e WHERE e.status = :status")
    List<BaggageBelt> findBaggageBeltByStatus(@Param("status") BeltStatus status);
    @Query("SELECT DISTINCT e.baggageBelt FROM BeltAssignment e WHERE e.flight.actualArrival > e.flight.scheduledArrival AND e.status = com.chouket370.smartbaggagereclaimmanagementsystem.AssignmentStatus.ACTIVE")
    List<BaggageBelt> findBeltsWithDelayedFlights();
    @Query("SELECT b FROM BaggageBelt b WHERE b.lastMaintenanceAt < :cutoffDate")
    List<BaggageBelt> findBeltsNeedingMaintenance(@Param("cutoffDate") LocalDateTime cutoffDate);


    @Query("SELECT b FROM BaggageBelt b WHERE b.capacity = :capacity AND b.status = :status")
    Optional<BaggageBelt> findAvailableBeltsByCapacity(@Param("capacity") BeltCapacity capacity);

    @Query("SELECT b.beltNumber, COUNT(ba) as activeAssignments FROM BaggageBelt b LEFT JOIN b.beltAssignments ba ON ba.status = com.chouket370.smartbaggagereclaimmanagementsystem.AssignmentStatus.ACTIVE GROUP BY b.beltNumber ORDER BY activeAssignments DESC")
    List<Object[]> getBeltUtilization();
    List<BaggageBelt> findByStatusAndCapacityGreaterThanEqual(
            BeltStatus status,
            BeltCapacity minCapacity);







}
