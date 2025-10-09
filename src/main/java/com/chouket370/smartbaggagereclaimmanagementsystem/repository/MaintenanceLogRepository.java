package com.chouket370.smartbaggagereclaimmanagementsystem.repository;

import com.chouket370.smartbaggagereclaimmanagementsystem.MaintenanceLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long> {
    List<MaintenanceLog> findByBeltIdOrderByEndTimeDesc(Long beltId);
}