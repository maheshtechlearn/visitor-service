package com.mylogo.visitors.controller;


import com.mylogo.visitors.dto.VisitorDTO;
import com.mylogo.visitors.entity.Visitor;
import com.mylogo.visitors.service.VisitorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VisitorControllerTest {

    @Mock
    private VisitorService visitorService;

    @InjectMocks
    private VisitorController visitorController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllVisitors() {
        List<VisitorDTO> visitors = Arrays.asList(new VisitorDTO(), new VisitorDTO());
        when(visitorService.getAllVisitors()).thenReturn(visitors);

        List<VisitorDTO> result = visitorController.getAllVisitors();

        assertEquals(2, result.size());
        verify(visitorService, times(1)).getAllVisitors();
    }

    @Test
    void testGetVisitorById_Found() {
        VisitorDTO visitor = new VisitorDTO();
        when(visitorService.getVisitorById(1L)).thenReturn(visitor);

        ResponseEntity<VisitorDTO> response = visitorController.getVisitorById(1L);

        assertEquals(ResponseEntity.ok(visitor), response);
        verify(visitorService, times(1)).getVisitorById(1L);
    }

    @Test
    void testGetVisitorById_NotFound() {
        when(visitorService.getVisitorById(1L)).thenReturn(null);

        ResponseEntity<VisitorDTO> response = visitorController.getVisitorById(1L);

        assertEquals(ResponseEntity.notFound().build(), response);
        verify(visitorService, times(1)).getVisitorById(1L);
    }

    @Test
    void testAddVisitor() {
        Visitor visitor = new Visitor();
        when(visitorService.addVisitor(visitor)).thenReturn(new VisitorDTO());

        assertNotNull(visitorController.addVisitor(visitor));

    }

    @Test
    void testUpdateVisitor_Success() {
        Visitor visitor = new Visitor();
        when(visitorService.updateVisitor(1L, visitor)).thenReturn(visitor);

        ResponseEntity<Visitor> response = visitorController.updateVisitor(1L, visitor);

        assertEquals(ResponseEntity.ok(visitor), response);
        verify(visitorService, times(1)).updateVisitor(1L, visitor);
    }

    @Test
    void testAnalyzeVisitors_Success() throws ExecutionException, InterruptedException {
        // Arrange
        List<Visitor> visitors = Arrays.asList(new Visitor(), new Visitor());
        when(visitorService.fetchAllVisitors()).thenReturn(CompletableFuture.completedFuture(visitors));
        when(visitorService.calculateTotalVisitDuration(visitors)).thenReturn(CompletableFuture.completedFuture(120L));

        // Act
        CompletableFuture<ResponseEntity<String>> responseFuture = visitorController.analyzeVisitors();
        ResponseEntity<String> response = responseFuture.get();

        // Assert
        assertEquals(ResponseEntity.ok("Total visit duration: 120 minutes"), response);
        verify(visitorService, times(1)).fetchAllVisitors();
        verify(visitorService, times(1)).calculateTotalVisitDuration(visitors);
    }

    @Test
    void testAnalyzeVisitors_Exception() throws ExecutionException, InterruptedException {
        // Arrange
        when(visitorService.fetchAllVisitors()).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Service error")));

        // Act
        CompletableFuture<ResponseEntity<String>> responseFuture = visitorController.analyzeVisitors();
        ResponseEntity<String> response = responseFuture.get();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error calculating total visit duration", response.getBody());
        verify(visitorService, times(1)).fetchAllVisitors();
        verify(visitorService, times(0)).calculateTotalVisitDuration(anyList());
    }

    @Test
    void testDeleteVisitor_Success() {
        // Arrange
        Long visitorId = 1L;

        // Act
        ResponseEntity<Void> response = visitorController.deleteVisitor(visitorId);

        // Assert
        assertEquals(ResponseEntity.noContent().build(), response);
        verify(visitorService, times(1)).deleteVisitor(visitorId);
    }

    @Test
    void testDeleteVisitor_NonExistentId() {
        // Arrange
        Long nonExistentVisitorId = 999L;
        doThrow(new RuntimeException("Visitor not found")).when(visitorService).deleteVisitor(nonExistentVisitorId);

        // Act & Assert
        try {
            visitorController.deleteVisitor(nonExistentVisitorId);
        } catch (RuntimeException e) {
            assertEquals("Visitor not found", e.getMessage());
        }
        verify(visitorService, times(1)).deleteVisitor(nonExistentVisitorId);
    }

}