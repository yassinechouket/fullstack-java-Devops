package com.chouket370.smartbaggagereclaimmanagementsystem.config;


import com.chouket370.smartbaggagereclaimmanagementsystem.dto.BeltMaintenanceEvent;
import com.chouket370.smartbaggagereclaimmanagementsystem.dto.FlightUpdateMessage;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;


    @Bean
    public ProducerFactory<String, FlightUpdateMessage> flightProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, FlightUpdateMessage> kafkaTemplate() {
        return new KafkaTemplate<>(flightProducerFactory());
    }

    @Bean
    public ProducerFactory<String, BeltMaintenanceEvent> beltMaintenanceProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, BeltMaintenanceEvent> beltMaintenanceKafkaTemplate() {
        return new KafkaTemplate<>(beltMaintenanceProducerFactory());
    }

    @Bean
    public NewTopic flightUpdatesTopic() {
        return new NewTopic("flight-updates", 1, (short) 1);
    }

    @Bean
    public NewTopic beltMaintenanceTopic() {
        return new NewTopic("belt-maintenance", 1, (short) 1);
    }
}