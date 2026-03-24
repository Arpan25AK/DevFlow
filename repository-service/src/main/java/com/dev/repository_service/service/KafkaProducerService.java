package com.dev.repository_service.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@AllArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void projectCreatedEvent(String ownerEmail, String name){

        String message = String.format("{\"ownerEmail\":\"%s\", \"name\":\"%s\"}", ownerEmail, name);

        kafkaTemplate.send("project - lifecycle",message);

        log.info("Alert ! broadcasted new project creation at kafka for {} / {} ", ownerEmail, name);
    }
}
