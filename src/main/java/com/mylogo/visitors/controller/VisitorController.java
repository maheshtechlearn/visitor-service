package com.mylogo.visitors.controller;

import com.mylogo.visitors.model.Visitor;
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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/visitors")
public class VisitorController {

    private static final Logger logger = LoggerFactory.getLogger(VisitorController.class);

    private final VisitorService visitorService;

    //private final VisitorDataSourceConfig visitorDataSourceConfig;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @GetMapping
    public List<Visitor> getAllVisitors() {
        logger.info("Fetching all visitors");
        return visitorService.getAllVisitors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Visitor> getVisitorById(@PathVariable Long id) {
        logger.info("Fetching visitor by ID: {}", id);
        Optional<Visitor> visitor = visitorService.getVisitorById(id);
        if (visitor.isPresent()) {
            return new ResponseEntity<>(visitor.get(), HttpStatus.OK);
        }
        logger.warn("Visitor not found with ID: {}", id);
        return ResponseEntity.notFound().build();

    }

    @PostMapping
    public Visitor addVisitor(@RequestBody Visitor visitor) {
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

    @GetMapping("/visitors/analyze")
    public CompletableFuture<ResponseEntity<String>> analyzeVisitors() {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        CompletableFuture<List<Visitor>> visitorsFuture = visitorService.fetchAllVisitors().thenApplyAsync(visitors -> visitors, executor);
        CompletableFuture<Long> totalDurationFuture = visitorsFuture.thenComposeAsync(visitors -> visitorService.calculateTotalVisitDuration(visitors), executor);

        return totalDurationFuture.thenApply(totalDuration ->
                ResponseEntity.ok("Total visit duration: " + totalDuration + " minutes")
        ).exceptionally(e -> {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error calculating total visit duration");
        }).whenComplete((res, ex) -> executor.shutdown());
    }
}
