package com.dev.code_review_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    public static final String REVIEW_TOPIC = "code-review-events";

    @Bean
    public NewTopic codeReviewTopic(){
        return TopicBuilder.name(REVIEW_TOPIC).
                partitions(3).
                replicas(1).
                build();
    }
}
