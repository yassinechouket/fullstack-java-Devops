package com.chouket370.smartbaggagereclaimmanagementsystem.service;

import com.chouket370.smartbaggagereclaimmanagementsystem.*;
import com.chouket370.smartbaggagereclaimmanagementsystem.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BeltAssignmentService {

    private final BeltAssignmentRepository beltAssignmentRepository;
    private final BaggageBeltRepository baggageBeltRepository;
    private final BeltMaintenanceProducer maintenanceProducer;

    private static final int BASE_UNLOADING_MINUTES = 30;
    private static final int RECOMMENDATION_LIMIT = 3;

    public List<BeltAssignment> getActiveBeltAssignments() {
        return beltAssignmentRepository.findByStatus(AssignmentStatus.ACTIVE);
    }

    @Transactional
    public List<BeltAssignment> reassignBeltDueToDelay(String flightNumber) {
        List<BeltAssignment> activeAssignments = beltAssignmentRepository
                .findActiveAssignmentsByFlightNumber(flightNumber);

        if (activeAssignments.isEmpty()) {
            throw new RuntimeException("No active assignments found for flight " + flightNumber);
        }

        return activeAssignments.stream()
                .map(this::processReassignment)
                .toList();
    }

    private BeltAssignment processReassignment(BeltAssignment oldAssignment) {
        Flight flight = oldAssignment.getFlight();
        BaggageBelt currentBelt = oldAssignment.getBaggageBelt();

        boolean stillSuitable = currentBelt.getStatus() == BeltStatus.AVAILABLE &&
                currentBelt.getCapacity().ordinal() >= calculateRequiredCapacity(flight.getPassengerCount()).ordinal();

        if (stillSuitable) {
            oldAssignment.setExpectedReleaseAt(calculateNewReleaseTime(oldAssignment));
            oldAssignment.setNotes("Extended on same belt due to delay");
            return beltAssignmentRepository.save(oldAssignment);
        }

        BaggageBelt newBelt = findOptimalBelt(flight)
                .orElseThrow(() -> new RuntimeException("No suitable belt available"));

        BeltAssignment newAssignment = createNewAssignment(oldAssignment, newBelt);
        completeOldAssignment(oldAssignment);

        beltAssignmentRepository.save(oldAssignment);
        return beltAssignmentRepository.save(newAssignment);
    }

    private BeltAssignment createNewAssignment(BeltAssignment oldAssignment, BaggageBelt newBelt) {
        return BeltAssignment.builder()
                .flight(oldAssignment.getFlight())
                .baggageBelt(newBelt)
                .assignedAt(LocalDateTime.now())
                .expectedReleaseAt(calculateNewReleaseTime(oldAssignment))
                .status(AssignmentStatus.ACTIVE)
                .priority(determinePriority(oldAssignment.getFlight()))
                .assignedBy("SYSTEM")
                .notes("Reassigned from belt " + oldAssignment.getBaggageBelt().getBeltNumber() +
                        " due to delay (" + oldAssignment.getFlight().getDelayMinutes() + " mins)")
                .build();
    }

    public Optional<BaggageBelt> findOptimalBelt(Flight flight) {
        if (flight == null || flight.getPassengerCount() <= 0) {
            return Optional.empty();
        }

        BeltCapacity requiredCapacity = calculateRequiredCapacity(flight.getPassengerCount());

        List<BaggageBelt> suitableBelts = baggageBeltRepository
                .findByStatusAndCapacityGreaterThanEqual(BeltStatus.AVAILABLE, requiredCapacity);

        return suitableBelts.stream()
                .min(Comparator.comparing((BaggageBelt b) ->
                                Math.abs(b.getCapacity().getMaxPassengers() - flight.getPassengerCount()))
                        .thenComparing(b -> Optional.ofNullable(b.getLastMaintenanceAt()).orElse(LocalDateTime.MIN))
                        .thenComparing(BaggageBelt::getId));

    }

    private AssignmentPriority determinePriority(Flight flight) {
        int delay = (int) flight.getDelayMinutes();
        if (delay > 120) return AssignmentPriority.URGENT;
        if (delay > 60) return AssignmentPriority.HIGH;
        if (delay > 30) return AssignmentPriority.NORMAL;
        return AssignmentPriority.LOW;
    }

    private void completeOldAssignment(BeltAssignment assignment) {
        assignment.setStatus(AssignmentStatus.COMPLETED);
        assignment.setActualReleaseAt(LocalDateTime.now());
        assignment.setNotes("Reassigned due to flight delay");

        BaggageBelt oldBelt = assignment.getBaggageBelt();
        oldBelt.setStatus(BeltStatus.AVAILABLE);
        oldBelt.setCurrentFlightNumber(null);
        oldBelt.setExpectedAvailableAt(null);

        baggageBeltRepository.save(oldBelt);
    }

    private LocalDateTime calculateNewReleaseTime(BeltAssignment assignment) {
        Duration remaining = Duration.between(LocalDateTime.now(), assignment.getExpectedReleaseAt());
        if (remaining.isNegative()) remaining = Duration.ofMinutes(10);

        long extra = assignment.getFlight().getDelayMinutes() / 2L;
        return LocalDateTime.now().plus(remaining).plusMinutes(extra);
    }

    private BeltCapacity calculateRequiredCapacity(int passengerCount) {
        if (passengerCount <= 50) return BeltCapacity.SMALL;
        if (passengerCount <= 150) return BeltCapacity.MEDIUM;
        return BeltCapacity.LARGE;
    }

    @Transactional
    public void releaseCompletedAssignments() {
        List<BeltAssignment> completedAssignments = beltAssignmentRepository
                .findByStatusAndExpectedReleaseAtBefore(AssignmentStatus.ACTIVE, LocalDateTime.now());

        completedAssignments.forEach(assignment -> {
            assignment.setStatus(AssignmentStatus.COMPLETED);
            assignment.setActualReleaseAt(LocalDateTime.now());
            assignment.setNotes("Automatically released at " + LocalDateTime.now());

            BaggageBelt belt = assignment.getBaggageBelt();
            belt.setStatus(BeltStatus.AVAILABLE);
            belt.setCurrentFlightNumber(null);
            belt.setExpectedAvailableAt(null);
            baggageBeltRepository.save(belt);
        });

        beltAssignmentRepository.saveAll(completedAssignments);
    }

    private Optional<BaggageBelt> findEmergencyReplacementBelt(Flight flight, Long excludedBeltId) {
        return baggageBeltRepository
                .findAvailableBeltsByCapacity(calculateRequiredCapacity(flight.getPassengerCount()))
                .stream()
                .filter(belt -> !belt.getId().equals(excludedBeltId))
                .min(Comparator.comparing(BaggageBelt::getDistanceToGate));
    }

    @Transactional
    public BeltAssignment handleEmergencyReassignment(Long brokenBeltId, String reason, String operatorId) {
        BeltAssignment current = beltAssignmentRepository.findActiveByBaggageBelt_Id(brokenBeltId)
                .orElseThrow(() -> new RuntimeException("No active assignment found for belt ID: " + brokenBeltId));

        BaggageBelt replacement = findEmergencyReplacementBelt(current.getFlight(), brokenBeltId)
                .orElseThrow(() -> new RuntimeException("No available emergency belt found"));

        current.getBaggageBelt().setStatus(BeltStatus.MAINTENANCE);
        current.setStatus(AssignmentStatus.COMPLETED);
        current.setActualReleaseAt(LocalDateTime.now());
        current.setNotes("Emergency reassignment by " + operatorId + ": " + reason);

        BeltAssignment newAssignment = BeltAssignment.builder()
                .flight(current.getFlight())
                .baggageBelt(replacement)
                .status(AssignmentStatus.ACTIVE)
                .assignedAt(LocalDateTime.now())
                .expectedReleaseAt(calculateEmergencyReleaseTime(current))
                .assignedBy(operatorId)
                .notes("Emergency reassignment due to: " + reason)
                .build();

        beltAssignmentRepository.save(current);
        return beltAssignmentRepository.save(newAssignment);
    }

    private LocalDateTime calculateEmergencyReleaseTime(BeltAssignment assignment) {
        int bagsPerMinute = 15;
        int remainingBags = assignment.getFlight().getRemainingBaggageCount();
        return LocalDateTime.now().plusMinutes((long) Math.ceil(remainingBags / (double) bagsPerMinute));
    }

    @Transactional
    public BeltAssignment assignBeltToFlight(Flight flight) {
        if (flight == null) throw new IllegalArgumentException("Flight cannot be null");
        if (flight.getScheduledArrival() == null) throw new IllegalArgumentException("Flight arrival time cannot be null");

        BaggageBelt optimalBelt = findOptimalBelt(flight)
                .orElseThrow(() -> new IllegalArgumentException("No suitable belt available for flight " + flight.getFlightNumber()));

        BeltAssignment assignment = createAssignment(flight, optimalBelt);
        updateBeltStatus(optimalBelt, flight, assignment.getExpectedReleaseAt());

        baggageBeltRepository.save(optimalBelt);
        return beltAssignmentRepository.save(assignment);
    }

    private BeltAssignment createAssignment(Flight flight, BaggageBelt belt) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime releaseTime = calculateExpectedReleaseTime(flight);

        return BeltAssignment.builder()
                .flight(flight)
                .baggageBelt(belt)
                .assignedAt(now)
                .expectedReleaseAt(releaseTime)
                .status(AssignmentStatus.ACTIVE)
                .priority(determinePriority(flight))
                .assignedBy("SYSTEM_AUTO")
                .notes(String.format("Auto-assigned for %s with %d passengers", flight.getFlightNumber(), flight.getPassengerCount()))
                .build();
    }

    private LocalDateTime calculateExpectedReleaseTime(Flight flight) {
        int bagsProcessingMinutes = (int) Math.ceil(flight.getPassengerCount() * 1.2 / 20);
        int delayMinutes = (int) flight.getDelayMinutes();
        return flight.getActualArrival()
                .plusMinutes(BASE_UNLOADING_MINUTES + bagsProcessingMinutes + delayMinutes);
    }

    private void updateBeltStatus(BaggageBelt belt, Flight flight, LocalDateTime expectedAvailableAt) {
        belt.setStatus(BeltStatus.OCCUPIED);
        belt.setCurrentFlightNumber(flight.getFlightNumber());
        belt.setExpectedAvailableAt(expectedAvailableAt);
    }

    public boolean validateAssignment(Flight flight, BaggageBelt belt) {
        if (belt.getStatus() != BeltStatus.AVAILABLE) return false;

        if (flight.getStatus() != FlightStatus.LANDED && flight.getStatus() != FlightStatus.APPROACHING)
            return false;

        if (flight.getActualArrival() == null) return false;

        if (beltAssignmentRepository.existsByFlightAndStatus(flight, AssignmentStatus.ACTIVE))
            return false;

        LocalDateTime arrival = flight.getActualArrival();
        LocalDateTime release = arrival.plusMinutes(BASE_UNLOADING_MINUTES);

        return !beltAssignmentRepository.existsByBaggageBeltAndExpectedReleaseAtAfterAndAssignedAtBefore(
                belt, arrival, release);
    }

    public List<BaggageBelt> getAssignmentRecommendations(Flight flight) {
        return baggageBeltRepository
                .findAvailableBeltsByCapacity(calculateRequiredCapacity(flight.getPassengerCount()))
                .stream()
                .filter(belt -> validateAssignment(flight, belt))
                .sorted(Comparator.comparingInt(belt ->
                        calculateDistance(belt.getLocation(), flight.getGate())))
                .limit(RECOMMENDATION_LIMIT)
                .collect(Collectors.toList());
    }

    private int calculateDistance(String beltLocation, String gate) {
        return Math.abs(beltLocation.charAt(0) - gate.charAt(0));
    }



}
