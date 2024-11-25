package com.mylogo.visitors.controller;

import com.mylogo.visitors.dto.VisitorDTO;
import com.mylogo.visitors.entity.Visitor;
import com.mylogo.visitors.service.VisitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/visitors")
public class VisitorController {

    private static final Logger logger = LoggerFactory.getLogger(VisitorController.class);

    private final VisitorService visitorService;
    private final ExecutorService executor;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
        this.executor = Executors.newFixedThreadPool(2);
    }

    @GetMapping
    public List<VisitorDTO> getAllVisitors() {
        logger.info("Fetching all visitors");
        return visitorService.getAllVisitors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VisitorDTO> getVisitorById(@PathVariable Long id) {
        logger.info("Fetching visitor by ID: {}", id);
        VisitorDTO visitor = visitorService.getVisitorById(id);
        if (null!=visitor) {
            return new ResponseEntity<>(visitor, HttpStatus.OK);
        }
        logger.warn("Visitor not found with ID: {}", id);
        return ResponseEntity.notFound().build();

    }

    @PostMapping
    public VisitorDTO addVisitor(@RequestBody Visitor visitor) {
        logger.info("Adding a new visitor");
        return visitorService.addVisitor(visitor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Visitor> updateVisitor(@PathVariable Long id, @RequestBody Visitor visitor) {
        logger.info("Updating visitor with ID: {}", id);
        Visitor updatedVisitor = visitorService.updateVisitor(id, visitor);
        if (updatedVisitor != null) {
            return ResponseEntity.ok(updatedVisitor);
        } else {
            logger.warn("Failed to update visitor with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVisitor(@PathVariable Long id) {
        logger.info("Deleting visitor with ID: {}", id);
        visitorService.deleteVisitor(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to analyze visitors and calculate total visit duration.
     *
     * @return a CompletableFuture with the response entity containing the total visit duration or an error message
     */
    @GetMapping("/visitors/analyze")
    public CompletableFuture<ResponseEntity<String>> analyzeVisitors() {
        logger.info("Received request to analyze visitors");

        return visitorService.fetchAllVisitors()
                .thenComposeAsync(visitors -> {
                    logger.debug("Fetched {} visitors", visitors.size());
                    return visitorService.calculateTotalVisitDuration(visitors);
                }, executor)
                .thenApply(totalDuration -> {
                    logger.info("Calculated total visit duration: {} minutes", totalDuration);
                    return ResponseEntity.ok("Total visit duration: " + totalDuration + " minutes");
                })
                .exceptionally(e -> {
                    logger.error("Error calculating total visit duration", e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error calculating total visit duration: " + e.getMessage());
                });
    }

    /**
     * Shutdown the executor service when the application context is closed.
     */
    @PreDestroy
    public void shutdownExecutor() {
        logger.info("Shutting down executor service");
        executor.shutdown();
    }

}
