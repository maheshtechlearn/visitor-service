package com.mylogo.visitors.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class VisitorEventProducer implements EventProducer {

    private static final Logger logger = LoggerFactory.getLogger(VisitorEventProducer.class);
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void produceVisitorEvent(String message) {
        kafkaTemplate.send("visitorTopic", message);
        logger.info("Produced message: {} ", message);
    }
}
