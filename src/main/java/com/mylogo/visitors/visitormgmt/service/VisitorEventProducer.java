package com.mylogo.visitors.visitormgmt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class VisitorEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(VisitorEventProducer.class);
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendVisitorEvent(String message) {
        kafkaTemplate.send("visitorTopic", message);
        logger.info("Produced message: {} ", message);
    }
}