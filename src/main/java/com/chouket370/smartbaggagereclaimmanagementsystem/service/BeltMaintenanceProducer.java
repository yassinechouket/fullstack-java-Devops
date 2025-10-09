package com.chouket370.smartbaggagereclaimmanagementsystem.service;


import com.chouket370.smartbaggagereclaimmanagementsystem.BaggageBelt;
import com.chouket370.smartbaggagereclaimmanagementsystem.BeltStatus;
import com.chouket370.smartbaggagereclaimmanagementsystem.dto.BeltMaintenanceEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


import static com.chouket370.smartbaggagereclaimmanagementsystem.BeltStatus.MAINTENANCE;
import static org.springframework.kafka.support.KafkaHeaders.TOPIC;

@Service
@RequiredArgsConstructor
public class BeltMaintenanceProducer {

    private final KafkaTemplate<String, BeltMaintenanceEvent>kafkaTemplate;

    private static final String BELT_MAINTENANCE_TOPIC = "belt-maintenance";
    private static final Logger log =  LoggerFactory.getLogger(BeltMaintenanceProducer.class);

    public void sendMaintenanceEvent (BaggageBelt belt, String msg) {
        BeltMaintenanceEvent message= BeltMaintenanceEvent.builder()
                .beltId(belt.getId())
                .beltNumber(belt.getBeltNumber())
                .status(BeltStatus.MAINTENANCE)
                .message(msg)
                .timestamp(LocalDateTime.now())
                .build();

        try {
            kafkaTemplate.send(BELT_MAINTENANCE_TOPIC, belt.getId().toString(), message);
            log.info("Maintenance event published: {}", message);
        } catch (Exception e) {
            log.error("Failed to send maintenance event", e);
        }

    }
}
