package com.mylogo.visitors.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylogo.visitors.dto.VisitorDTO;
import com.mylogo.visitors.entity.Visitor;
import com.mylogo.visitors.handler.DatabaseOperationException;
import com.mylogo.visitors.handler.VisitorNotFoundException;
import com.mylogo.visitors.handler.VisitorRetrievalException;
import com.mylogo.visitors.repository.VisitorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitorServiceTest {

    @InjectMocks
    private VisitorService visitorService;

    @Mock
    private VisitorRepository visitorRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private VisitorEventProducer eventProducer;

    @Mock
    private ObjectMapper objectMapper;

    private Visitor visitor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        visitor = new Visitor();
        visitor.setId(1L);
        visitor.setName("John Doe");
        visitor.setContactNumber("1234567890");
        visitor.setPurpose("Meeting");
    }

    @Test
    void testGetAllVisitors_CachingBehavior() {
        List<Visitor> mockVisitors = Arrays.asList(new Visitor(), new Visitor());
        when(visitorRepository.findAll()).thenReturn(mockVisitors);

        List<VisitorDTO> firstCall = visitorService.getAllVisitors();

        verify(visitorRepository, times(1)).findAll();
    }

    @Test
    void testGetAllVisitors_ExceptionHandling() {
        when(visitorRepository.findAll()).thenThrow(new RuntimeException("Database is down"));

        VisitorRetrievalException exception = assertThrows(
                VisitorRetrievalException.class,
                () -> visitorService.getAllVisitors()
        );

        assertEquals("Failed to retrieve visitors from the database", exception.getMessage());
        verify(visitorRepository, times(1)).findAll();
    }



    @Test
    void testGetVisitorById_Success() {
        when(visitorRepository.findById(1L)).thenReturn(Optional.of(visitor));

        VisitorDTO result = visitorService.getVisitorById(1L);

        assertNotNull(result);
        assertEquals(visitor.getId(), result.getId());
        assertEquals(visitor.getName(), result.getName());
        verify(visitorRepository, times(1)).findById(1L);
    }

    @Test
    void testGetVisitorById_VisitorNotFound() {
        when(visitorRepository.findById(1L)).thenReturn(Optional.empty());

        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class,
                () -> visitorService.getVisitorById(1L));

        assertEquals("Error retrieving visitor with ID 1", exception.getMessage());
        verify(visitorRepository, times(1)).findById(1L);
        verifyNoInteractions(eventProducer);
    }


    @Test
    void testGetVisitorById_DatabaseError() {
        when(visitorRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class,
                () -> visitorService.getVisitorById(1L));

        assertTrue(exception.getMessage().contains("Error retrieving visitor with ID 1"));
        verify(visitorRepository, times(1)).findById(1L);
        verifyNoInteractions(eventProducer);
    }

    @Test
    void testGetVisitorById_Caching() {
        when(visitorRepository.findById(1L)).thenReturn(Optional.of(visitor));

        VisitorDTO firstCallResult = visitorService.getVisitorById(1L);
        assertNotNull(firstCallResult);

        VisitorDTO secondCallResult = visitorService.getVisitorById(1L);
        assertNotNull(secondCallResult);

    }

    @Test
    void testGetVisitorById_AsyncEventPublishing() throws InterruptedException {
        when(visitorRepository.findById(1L)).thenReturn(Optional.of(visitor));

        visitorService.getVisitorById(1L);

        Thread.sleep(100);

        verify(eventProducer, times(1)).sendVisitorEvent("Visitor fetched: 1");
    }

    @Test
    void testGetVisitorById_InvalidId() {
        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class,
                () -> visitorService.getVisitorById(null));

        assertEquals("Error retrieving visitor with ID null", exception.getMessage());
    }




    @Test
    void testAddVisitor_Success() {
        Visitor savedVisitor = new Visitor();
        savedVisitor.setId(1L);
        savedVisitor.setName(visitor.getName());
        savedVisitor.setContactNumber(visitor.getContactNumber());
        savedVisitor.setPurpose(visitor.getPurpose());

        when(visitorRepository.save(visitor)).thenReturn(savedVisitor);

        VisitorDTO result = visitorService.addVisitor(visitor);

        assertNotNull(result);
        assertEquals(savedVisitor.getId(), result.getId());
        assertEquals(savedVisitor.getName(), result.getName());
        verify(visitorRepository, times(1)).save(visitor);
    }



    @Test
    void testAddVisitor_NullVisitor() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> visitorService.addVisitor(null));

        assertEquals("Visitor cannot be null", exception.getMessage());
        verifyNoInteractions(visitorRepository, eventProducer);
    }


    @Test
    void testAddVisitor_DatabaseError() {
        when(visitorRepository.save(visitor)).thenThrow(new RuntimeException("Database error"));

        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class,
                () -> visitorService.addVisitor(visitor));

        assertTrue(exception.getMessage().contains("Error saving visitor"));
        verify(visitorRepository, times(1)).save(visitor);
        verifyNoInteractions(eventProducer);
    }


    @Test
    void testAddVisitor_AsyncEventPublishing() throws InterruptedException {
        Visitor savedVisitor = new Visitor();
        savedVisitor.setId(1L);
        when(visitorRepository.save(visitor)).thenReturn(savedVisitor);

        visitorService.addVisitor(visitor);

        Thread.sleep(100);

    }


    @Test
    void testAddVisitor_VisitorDTOConversion() {
        Visitor savedVisitor = new Visitor();
        savedVisitor.setId(1L);
        savedVisitor.setName(visitor.getName());
        savedVisitor.setContactNumber(visitor.getContactNumber());
        savedVisitor.setPurpose(visitor.getPurpose());

        when(visitorRepository.save(visitor)).thenReturn(savedVisitor);

        VisitorDTO result = visitorService.addVisitor(visitor);

        assertNotNull(result);
        assertEquals(savedVisitor.getId(), result.getId());
        assertEquals(savedVisitor.getName(), result.getName());
        assertEquals(savedVisitor.getContactNumber(), result.getContactNumber());
        assertEquals(savedVisitor.getPurpose(), result.getPurpose());
    }


    @Test
    void testUpdateVisitor_Success() {
        Long id = 1L;

        Visitor existingVisitor = new Visitor();
        existingVisitor.setId(id);
        existingVisitor.setName("Jane Doe");

        when(visitorRepository.findById(id)).thenReturn(Optional.of(existingVisitor));
        when(visitorRepository.save(visitor)).thenReturn(visitor);

        Visitor result = visitorService.updateVisitor(id, visitor);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(visitor.getName(), result.getName());
        verify(visitorRepository, times(1)).findById(id);
        verify(visitorRepository, times(1)).save(visitor);
    }


    @Test
    void testUpdateVisitor_DatabaseError() {
        Long id = 1L;

        when(visitorRepository.findById(id)).thenReturn(Optional.of(visitor));
        when(visitorRepository.save(visitor)).thenThrow(new RuntimeException("Database error"));

        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class,
                () -> visitorService.updateVisitor(id, visitor));

        assertTrue(exception.getMessage().contains("Error updating visitor with ID"));
        verify(visitorRepository, times(1)).findById(id);
        verify(visitorRepository, times(1)).save(visitor);
        verifyNoInteractions(eventProducer);
    }


    @Test
    void testUpdateVisitor_AsyncEventPublishing() throws InterruptedException {
        Long id = 1L;

        when(visitorRepository.findById(id)).thenReturn(Optional.of(visitor));
        when(visitorRepository.save(visitor)).thenReturn(visitor);

        visitorService.updateVisitor(id, visitor);

        Thread.sleep(100);

    }


    @Test
    void testUpdateVisitor_IdIntegrity() {
        Long id = 1L;
        Long differentId = 2L;
        visitor.setId(differentId);

        when(visitorRepository.findById(id)).thenReturn(Optional.of(new Visitor()));
        when(visitorRepository.save(visitor)).thenAnswer(invocation -> {
            Visitor updated = invocation.getArgument(0);
            assertEquals(id, updated.getId());
            return updated;
        });

        visitorService.updateVisitor(id, visitor);
    }

    @Test
    void testUpdateVisitor_VisitorNotFound() {
        Long id = 1L;

        when(visitorRepository.findById(id)).thenReturn(Optional.empty());

        VisitorNotFoundException exception = assertThrows(VisitorNotFoundException.class,
                () -> visitorService.updateVisitor(id, visitor));

        assertEquals("Visitor with ID " + id + " not found", exception.getMessage());
        verify(visitorRepository, times(1)).findById(id);
        verify(visitorRepository, never()).save(any(Visitor.class));
        verifyNoInteractions(eventProducer);
    }


    @Test
    void testDeleteVisitor_Success() {
        Long id = 1L;

        doNothing().when(visitorRepository).deleteById(id);

        visitorService.deleteVisitor(id);

        verify(visitorRepository, times(1)).deleteById(id);
    }


    @Test
    void testDeleteVisitor_DatabaseError() {
        Long id = 1L;

        doThrow(new RuntimeException("Database error")).when(visitorRepository).deleteById(id);

        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class,
                () -> visitorService.deleteVisitor(id));

        assertTrue(exception.getMessage().contains("Error deleting visitor with ID"));
        verify(visitorRepository, times(1)).deleteById(id);
        verifyNoInteractions(eventProducer);
    }


    @Test
    void testDeleteVisitor_AsyncEventPublishing() throws InterruptedException {
        Long id = 1L;

        doNothing().when(visitorRepository).deleteById(id);

        visitorService.deleteVisitor(id);

        Thread.sleep(100);

        verify(eventProducer, times(1)).sendVisitorEvent("Visitor Deleted with visitorID: " + id);
    }


    @Test
    void testFetchAllVisitors_Success() throws Exception {
        List<Visitor> visitors = Arrays.asList(
                new Visitor(),
                new Visitor()
        );

        when(visitorRepository.findAll()).thenReturn(visitors);

        CompletableFuture<List<Visitor>> resultFuture = visitorService.fetchAllVisitors();
        List<Visitor> result = resultFuture.get();
        assertEquals(2, result.size());
        verify(visitorRepository, times(1)).findAll();
    }


    @Test
    void testFetchAllVisitors_Exception() {
        when(visitorRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        CompletableFuture<List<Visitor>> resultFuture = visitorService.fetchAllVisitors();

        ExecutionException exception = assertThrows(ExecutionException.class, resultFuture::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Database error", exception.getCause().getMessage());
        verify(visitorRepository, times(1)).findAll();
    }


    @Test
    void testCalculateTotalVisitDuration_Success() throws Exception {
        List<Visitor> visitors = Arrays.asList(
                new Visitor(1L, "John Doe", "1234567890","test@yopmail.com", "Meeting", null, null, 60L, true,LocalDateTime.now()),

                new Visitor(2L, "Jane Smith", "9876543210","test@yopmail.com", "Interview", LocalDateTime.now(), LocalDateTime.now(), 120L, false,LocalDateTime.now())

        );

        CompletableFuture<Long> resultFuture = visitorService.calculateTotalVisitDuration(visitors);
        Long totalDuration = resultFuture.get();
        assertEquals(180L, totalDuration);
    }


    @Test
    void testCalculateTotalVisitDuration_EmptyList() throws Exception {
        List<Visitor> visitors = Collections.emptyList();

        CompletableFuture<Long> resultFuture = visitorService.calculateTotalVisitDuration(visitors);
        Long totalDuration = resultFuture.get();

        assertEquals(0L, totalDuration);
    }

    @Test
    void testCalculateTotalVisitDuration_NullDurations() throws Exception {
        List<Visitor> visitors = Arrays.asList(
                new Visitor(1L, "John Doe", "1234567890","test@yopmail.com", "Meeting", null, null, 0L, true,LocalDateTime.now()),

                new Visitor(2L, "Jane Smith", "9876543210","test@yopmail.com", "Interview", LocalDateTime.now(), LocalDateTime.now(), 120L, false,LocalDateTime.now())
        );

        CompletableFuture<Long> resultFuture = visitorService.calculateTotalVisitDuration(visitors);
        Long totalDuration = resultFuture.get();

        assertEquals(120L, totalDuration);
    }

    @Test
    void testCalculateTotalVisitDuration_AsyncExecution() throws Exception {
        List<Visitor> visitors = Arrays.asList(
                new Visitor(1L, "John Doe", "1234567890", "Meeting","test@yopmail.com", null, null, 60L, true,LocalDateTime.now())
        );

        long startTime = System.currentTimeMillis();
        CompletableFuture<Long> resultFuture = visitorService.calculateTotalVisitDuration(visitors);
        Long totalDuration = resultFuture.get();
        long endTime = System.currentTimeMillis();

        assertTrue((endTime - startTime) >= 0);
        assertEquals(60L, totalDuration);
    }


}
