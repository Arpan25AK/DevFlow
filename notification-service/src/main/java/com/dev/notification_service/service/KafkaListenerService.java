package com.dev.notification_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaListenerService {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    //inbuilt dto
    public record projectEvent(String ownerEmail, String name){}

    @KafkaListener(topics = "project-lifecycle", groupId = "notification-group")
    public void consumeProjectEventCreated(String message){
        try{
            log.info("kafka intercepted message: {}",message);

            projectEvent event = objectMapper.readValue(message, projectEvent.class);

            emailService.sendProjectCreateMail(event.ownerEmail, event.name);
        }catch(Exception e){
            log.error("error occurred during kafkaService" + e.getMessage());
        }
    }
}
