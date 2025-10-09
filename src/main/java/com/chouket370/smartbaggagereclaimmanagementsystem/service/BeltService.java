package com.chouket370.smartbaggagereclaimmanagementsystem.service;


import com.chouket370.smartbaggagereclaimmanagementsystem.*;
import com.chouket370.smartbaggagereclaimmanagementsystem.repository.BaggageBeltRepository;
import com.chouket370.smartbaggagereclaimmanagementsystem.repository.BeltAssignmentRepository;
import com.chouket370.smartbaggagereclaimmanagementsystem.repository.MaintenanceLogRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BeltService {
    private final BaggageBeltRepository baggageBeltRepository;
    private final BeltAssignmentRepository beltAssignmentRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final BeltMaintenanceProducer maintenanceProducer;
    public BeltService(BaggageBeltRepository baggageBeltRepository, BeltAssignmentRepository beltAssignmentRepository
    , MaintenanceLogRepository maintenanceLogRepository,
                       BeltMaintenanceProducer maintenanceProducer) {
        this.baggageBeltRepository = baggageBeltRepository;
        this.beltAssignmentRepository = beltAssignmentRepository;
        this.maintenanceLogRepository = maintenanceLogRepository;
        this.maintenanceProducer = maintenanceProducer;

    }
    public BaggageBelt saveBelt(BaggageBelt belt) {
        return baggageBeltRepository.save(belt);
    }
    public Optional<BaggageBelt> findBeltById(Long id) {
        return baggageBeltRepository.findById(id);
    }

    public List<BaggageBelt> getBeltAvailability() {
        return baggageBeltRepository.findAvailable();

    }
    public List<BaggageBelt> predictBeltAvailability(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("Minutes must be positive");
        }
        LocalDateTime timeNeeded = LocalDateTime.now().plusMinutes(minutes);
        return baggageBeltRepository.findBeltsExpectedFreeBy(timeNeeded);
    }
    public void markBeltUnderMaintenance2(Long beltId) {
        BaggageBelt belt = baggageBeltRepository.findById(beltId)
                .orElseThrow(() -> new RuntimeException("Belt not found: " + beltId));

        if (!beltAssignmentRepository.isBeltCurrentlyAvailable(beltId)) {
            throw new RuntimeException("Belt is currently assigned to a flight");
        }

        belt.setStatus(BeltStatus.MAINTENANCE);
        belt.setLastMaintenanceAt(LocalDateTime.now());
        baggageBeltRepository.save(belt);
    }

    public List<BaggageBelt> findBaggageBeltByStatus(BeltStatus status) {
        return baggageBeltRepository.findBaggageBeltByStatus(status);
    }
    public void updateBeltStatus(Long beltId ,BeltStatus status) {
        BaggageBelt belt = baggageBeltRepository.findById(beltId)
                .orElseThrow(() -> new RuntimeException("Belt not found: " + beltId));
        belt.setStatus(status);
        baggageBeltRepository.save(belt);

    }
    @Transactional
    public void returnBeltToService(Long beltId, LocalDateTime maintenanceEndTime,
                                    boolean maintenanceCompleted, String maintenanceNotes,
                                    String maintainedBy) {

        BaggageBelt belt = baggageBeltRepository.findById(beltId)
                .orElseThrow(() -> new RuntimeException("Belt not found"));

        if (belt.getStatus() != BeltStatus.MAINTENANCE) {
            throw new IllegalStateException("Belt is not under maintenance");
        }

        if (!maintenanceCompleted) {
            belt.setMaintenanceNotes("Maintenance incomplete: " + maintenanceNotes);
            belt.setNextMaintenanceCheck(LocalDateTime.now().plusHours(2));
            baggageBeltRepository.save(belt);
            throw new RuntimeException("Maintenance not completed");
        }

        MaintenanceLog log = new MaintenanceLog();
        log.setBeltId(beltId);
        log.setStartTime(belt.getLastMaintenanceAt());
        log.setEndTime(maintenanceEndTime);
        log.setNotes(maintenanceNotes);
        log.setMaintainedBy(maintainedBy);
        maintenanceLogRepository.save(log);


        belt.setStatus(BeltStatus.AVAILABLE);
        belt.setLastMaintenanceAt(maintenanceEndTime);
        belt.setMaintenanceNotes(maintenanceNotes);
        belt.setNextMaintenanceCheck(calculateNextMaintenanceDate(belt));
        baggageBeltRepository.save(belt);

    }

    private LocalDateTime calculateNextMaintenanceDate(BaggageBelt belt) {

        return LocalDateTime.now().plusMonths(3);
    }
    private void logMaintenanceCompletion(Long beltId, LocalDateTime endTime, String notes) {
        MaintenanceLog log = new MaintenanceLog();
        log.setBeltId(beltId);
        log.setEndTime(endTime);
        log.setNotes(notes);
        maintenanceLogRepository.save(log);
    }
    public Optional<BaggageBelt> findAvailableBeltsByCapacity(BeltCapacity capacity) {
        return baggageBeltRepository.findAvailableBeltsByCapacity(capacity);
    }

    public List<BaggageBelt> findBeltsAvailableBefore(LocalDateTime time) {
        return baggageBeltRepository.findBeltsAvailableBefore(time);
    }
    public Optional<String> findBeltNumberByFlightNumber(String flightNumber) {
        return baggageBeltRepository.findBeltNumberByFlightNumber(flightNumber);
    }
    public List<BaggageBelt> findBeltsNeedingMaintenance(LocalDateTime cutoffDate) {
        return baggageBeltRepository.findBeltsNeedingMaintenance(cutoffDate);
    }
    private boolean isBeltAvailableForMaintenance(Long beltId) {
        return beltAssignmentRepository.isBeltCurrentlyAvailable(beltId) &&
                baggageBeltRepository.findById(beltId)
                        .map(b -> b.getStatus() != BeltStatus.MAINTENANCE)
                        .orElse(false);
    }
    private void rescheduleMaintenanceCheck(Long beltId, LocalDateTime newTime) {
        baggageBeltRepository.findById(beltId).ifPresent(belt -> {
            belt.setNextMaintenanceCheck(newTime);
            baggageBeltRepository.save(belt);
        });


    }
    private BaggageBelt getBeltOrThrow(Long beltId) {
        return baggageBeltRepository.findById(beltId)
                .orElseThrow(() -> new RuntimeException("Belt not found: " + beltId));
    }
    public Optional<BaggageBelt> getAssignedBeltForFlight(String flightNumber) {
        return beltAssignmentRepository
                .findActiveAssignmentByFlightNumber(flightNumber)
                .map(BeltAssignment::getBaggageBelt);
    }



    public void markBeltUnderMaintenance(Long beltId) {
        BaggageBelt belt = baggageBeltRepository.findById(beltId)
                .orElseThrow(() -> new RuntimeException("Belt not found"));

        if (!beltAssignmentRepository.isBeltCurrentlyAvailable(beltId)) {
            throw new RuntimeException("Belt is assigned to a flight.");
        }
        belt.setLastMaintenanceAt(LocalDateTime.now());
        baggageBeltRepository.save(belt);

        maintenanceProducer.sendMaintenanceEvent(belt, "Belt sent for maintenance.");
    }


}
