package com.mylogo.visitors.service;

import com.mylogo.visitors.convertor.VisitorConverter;
import com.mylogo.visitors.dto.VisitorDTO;
import com.mylogo.visitors.entity.Visitor;
import com.mylogo.visitors.handler.VisitorNotFoundException;
import com.mylogo.visitors.repository.VisitorRepository;
import com.mylogo.visitors.validator.VisitorValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VisitorOperationsServiceTest {

    @Mock
    private VisitorRepository visitorRepository;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private VisitorConverter visitorConverter;

    @Mock
    private VisitorValidator visitorValidator;

    @InjectMocks
    private VisitorService visitorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllVisitors_ReturnsVisitorList() {
        // Arrange
        Visitor visitor = new Visitor();
        visitor.setId(1L);
        when(visitorRepository.findAll()).thenReturn(List.of(visitor));
        when(visitorConverter.toDTOList(any())).thenReturn(List.of(new VisitorDTO()));

        // Act
        List<VisitorDTO> visitors = visitorService.getAllVisitors();

        // Assert
        assertNotNull(visitors);
        assertEquals(1, visitors.size());
        verify(visitorRepository, times(1)).findAll();
    }

    @Test
    void testGetVisitorById_ThrowsException_WhenNotFound() {
        // Arrange
        when(visitorRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(VisitorNotFoundException.class, () -> visitorService.getVisitorById(1L));
        verify(visitorRepository, times(1)).findById(1L);
    }
}
