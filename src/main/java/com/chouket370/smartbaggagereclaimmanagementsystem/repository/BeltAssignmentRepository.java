package com.chouket370.smartbaggagereclaimmanagementsystem.repository;


import com.chouket370.smartbaggagereclaimmanagementsystem.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BeltAssignmentRepository extends JpaRepository<BeltAssignment,Long> {
    @Query("select e from BeltAssignment e where e.status= com.chouket370.smartbaggagereclaimmanagementsystem.AssignmentStatus.ACTIVE and e.expectedReleaseAt < :currentTime")
    List<BeltAssignment> findOverdueAssignments(@Param("currentTime") LocalDateTime currentTime);
    @Query("SELECT e FROM BeltAssignment e WHERE e.assignedAt >= :startOfDay AND e.assignedAt < :endOfDay")
    List<BeltAssignment> findTodayAssignments(@Param("startOfDay") LocalDateTime startOfDay,
                                              @Param("endOfDay") LocalDateTime endOfDay);
    @Query("SELECT e FROM BeltAssignment e WHERE e.baggageBelt.id = :beltId AND e.assignedAt >= :startOfDay AND e.assignedAt < :endOfDay")
    List<BeltAssignment> findBeltHistoryForToday(@Param("beltId") Long beltId,
                                                 @Param("startOfDay") LocalDateTime startOfDay,
                                                 @Param("endOfDay") LocalDateTime endOfDay);
    @Query("SELECT e FROM BeltAssignment e WHERE e.flight.flightNumber = :flightNumber AND e.status = com.chouket370.smartbaggagereclaimmanagementsystem.AssignmentStatus.ACTIVE")
    Optional<BeltAssignment> findCurrentBeltAssignmentByFlight(@Param("flightNumber") String flightNumber);
    @Query("SELECT ba FROM BeltAssignment ba WHERE ba.status = com.chouket370.smartbaggagereclaimmanagementsystem.AssignmentStatus.ACTIVE ORDER BY ba.assignedAt")
    List<BeltAssignment> findAllActiveAssignments();
    @Query("SELECT ba FROM BeltAssignment ba WHERE ba.priority = :priority AND ba.status = com.chouket370.smartbaggagereclaimmanagementsystem.AssignmentStatus.ACTIVE")
    List<BeltAssignment> findByPriorityAndActive(@Param("priority") AssignmentPriority priority);
    @Query("SELECT ba FROM BeltAssignment ba WHERE ba.flight.flightNumber = :flightNumber ORDER BY ba.assignedAt DESC")
    List<BeltAssignment> findAllAssignmentsByFlight(@Param("flightNumber") String flightNumber);
    @Query("SELECT DISTINCT ba.flight FROM BeltAssignment ba WHERE ba.baggageBelt.id = :beltId AND ba.status = com.chouket370.smartbaggagereclaimmanagementsystem.AssignmentStatus.ACTIVE")
    List<Flight> findActiveFlightsByBelt(@Param("beltId") Long beltId);
    @Query("SELECT ba.baggageBelt.beltNumber, COUNT(ba) as assignmentCount FROM BeltAssignment ba WHERE ba.assignedAt BETWEEN :startDate AND :endDate GROUP BY ba.baggageBelt.beltNumber ORDER BY assignmentCount DESC")
    List<Object[]> getBeltUtilizationStats(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    @Query("SELECT ba FROM BeltAssignment ba WHERE ba.status = com.chouket370.smartbaggagereclaimmanagementsystem.AssignmentStatus.ACTIVE AND ba.expectedReleaseAt BETWEEN :now AND :thirtyMinutesLater")
    List<BeltAssignment> findAssignmentsEndingSoon(@Param("now") LocalDateTime now, @Param("thirtyMinutesLater") LocalDateTime thirtyMinutesLater);
    @Query("SELECT COUNT(ba) = 0 FROM BeltAssignment ba WHERE ba.baggageBelt.id = :beltId AND ba.status = com.chouket370.smartbaggagereclaimmanagementsystem.AssignmentStatus.ACTIVE")
    boolean isBeltCurrentlyAvailable(@Param("beltId") Long beltId);
    @Query("SELECT ba FROM BeltAssignment ba WHERE ba.actualReleaseAt IS NOT NULL AND ba.actualReleaseAt > ba.expectedReleaseAt")
    List<BeltAssignment> findDelayedAssignments();

    List<BeltAssignment> findByBaggageBelt_IdAndStatus(Long beltId, AssignmentStatus status);

    @Query("SELECT a FROM BeltAssignment a WHERE a.flight.flightNumber = :flightNumber AND a.status = com.chouket370.smartbaggagereclaimmanagementsystem.AssignmentStatus.ACTIVE")
    Optional<BeltAssignment> findActiveAssignmentByFlightNumber(@Param("flightNumber") String flightNumber);
    List<BeltAssignment> findByStatus(AssignmentStatus status);
    @Query("SELECT ba FROM BeltAssignment ba " +
            "WHERE ba.flight.flightNumber = :flightNumber " +
            "AND ba.status = 'ACTIVE'")
    List<BeltAssignment> findActiveAssignmentsByFlightNumber(@Param("flightNumber") String flightNumber);


    List<BeltAssignment> findByStatusAndExpectedReleaseAtBefore(AssignmentStatus status, LocalDateTime expectedReleaseAt);

    Optional<BeltAssignment> findActiveByBaggageBelt_Id(Long beltId);

    boolean existsByFlightAndStatus(Flight flight, AssignmentStatus status);


    boolean existsByBaggageBeltAndExpectedReleaseAtAfterAndAssignedAtBefore(
            BaggageBelt belt,
            LocalDateTime arrivalTime,
            LocalDateTime releaseTime);


    List<BeltAssignment> findByFlightAndStatus(Flight flight, AssignmentStatus status);

}
