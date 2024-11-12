package com.mylogo.visitors.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class VisitorEventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(VisitorEventConsumer.class);

    @KafkaListener(topics = "visitorTopic", groupId = "visitor-app")
    public void listen(String message) {
        logger.info("Consumed message: {} ", message);
        // Implement your logic here based on the consumed message
    }
}
