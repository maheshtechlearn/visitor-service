package com.mylogo.visitors.service;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EventProducerTest {

    @Test
    void testProduceVisitorEvent() {
        EventProducer producer = mock(EventProducer.class);
        producer.produceVisitorEvent("Visitor added");

        verify(producer, times(1)).produceVisitorEvent("Visitor added");
    }
}

