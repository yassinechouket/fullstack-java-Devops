package com.chouket370.smartbaggagereclaimmanagementsystem.service;


import com.chouket370.smartbaggagereclaimmanagementsystem.dto.BeltMaintenanceEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BeltMaintenanceConsumer {
    @KafkaListener(topics = "belt-maintenance", groupId = "belt-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(BeltMaintenanceEvent event) {
        log.info("Received maintenance event: {}", event);

    }
}
